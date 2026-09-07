require("dotenv").config();

const http = require("http");
const crypto = require("crypto");
const express = require("express");
const helmet = require("helmet");
const cors = require("cors");
const rateLimit = require("express-rate-limit");
const { z } = require("zod");
const { WebSocketServer } = require("ws");

const { pool, initDb } = require("./db");
const {
  auth, requireRole, issueSession, hashPassword, verifyPassword,
  hashRefreshToken, signAccessToken
} = require("./auth");

const app = express();
app.disable("x-powered-by");
app.set("trust proxy", 1);
app.use(helmet());
app.use(cors({ origin: false }));
app.use(express.json({ limit: "256kb" }));

const authLimiter = rateLimit({
  windowMs: 60_000,
  limit: 20,
  standardHeaders: "draft-7",
  legacyHeaders: false
});
app.use("/auth", authLimiter);

const registerSchema = z.object({
  username: z.string().trim().min(3).max(64).regex(/^[a-zA-Z0-9._-]+$/),
  displayName: z.string().trim().min(2).max(128),
  password: z.string().min(10).max(128),
  deviceId: z.string().uuid(),
  deviceName: z.string().trim().min(1).max(128),
  publicKey: z.string().max(8192).optional()
});

const loginSchema = z.object({
  username: z.string().trim().min(3).max(64),
  password: z.string().min(1).max(128),
  deviceId: z.string().uuid(),
  deviceName: z.string().trim().min(1).max(128),
  publicKey: z.string().max(8192).optional()
});

app.get("/", (_, res) => res.json({ name: "Leviathan API", version: "0.3.1" }));

app.get("/health", async (_, res) => {
  try {
    await pool.query("SELECT 1");
    res.json({ status: "ok", database: "ok" });
  } catch {
    res.status(503).json({ status: "error", database: "unavailable" });
  }
});

app.post("/auth/bootstrap-owner", async (req, res) => {
  const token = process.env.OWNER_BOOTSTRAP_TOKEN;
  if (!token) return res.status(404).json({ error: "bootstrap disabled" });
  if (req.headers["x-bootstrap-token"] !== token) {
    return res.status(403).json({ error: "invalid bootstrap token" });
  }

  const parsed = registerSchema.safeParse(req.body);
  if (!parsed.success) return res.status(400).json({ error: "invalid input" });

  const count = await pool.query("SELECT COUNT(*)::int AS n FROM users");
  if (count.rows[0].n !== 0) {
    return res.status(409).json({ error: "owner already initialized" });
  }

  const d = parsed.data;
  const passwordHash = await hashPassword(d.password);
  const client = await pool.connect();

  try {
    await client.query("BEGIN");
    const userResult = await client.query(
      `INSERT INTO users(username,display_name,password_hash,role,status)
       VALUES($1,$2,$3,'OWNER','ACTIVE')
       RETURNING id,username,display_name,role,status`,
      [d.username.toLowerCase(), d.displayName, passwordHash]
    );
    const user = userResult.rows[0];

    await client.query(
      `INSERT INTO devices(id,user_id,device_name,public_key)
       VALUES($1,$2,$3,$4)`,
      [d.deviceId, user.id, d.deviceName, d.publicKey || null]
    );
    await client.query("COMMIT");

    const session = await issueSession(user, d.deviceId);
    res.status(201).json({ user, ...session });
  } catch (e) {
    await client.query("ROLLBACK");
    res.status(409).json({ error: "bootstrap failed" });
  } finally {
    client.release();
  }
});

app.post("/auth/register", async (req, res) => {
  const parsed = registerSchema.safeParse(req.body);
  if (!parsed.success) return res.status(400).json({ error: "invalid input" });

  const d = parsed.data;
  const passwordHash = await hashPassword(d.password);
  const client = await pool.connect();

  try {
    await client.query("BEGIN");
    const userResult = await client.query(
      `INSERT INTO users(username,display_name,password_hash)
       VALUES($1,$2,$3)
       RETURNING id,username,display_name,role,status,created_at`,
      [d.username.toLowerCase(), d.displayName, passwordHash]
    );
    const user = userResult.rows[0];

    await client.query(
      `INSERT INTO devices(id,user_id,device_name,public_key)
       VALUES($1,$2,$3,$4)`,
      [d.deviceId, user.id, d.deviceName, d.publicKey || null]
    );
    await client.query("COMMIT");

    res.status(201).json({ user, message: "awaiting admin approval" });
  } catch (e) {
    await client.query("ROLLBACK");
    if (e.code === "23505") return res.status(409).json({ error: "username already exists" });
    res.status(500).json({ error: "registration failed" });
  } finally {
    client.release();
  }
});

app.post("/auth/login", async (req, res) => {
  const parsed = loginSchema.safeParse(req.body);
  if (!parsed.success) return res.status(400).json({ error: "invalid input" });
  const d = parsed.data;

  const result = await pool.query(
    `SELECT id,username,display_name,password_hash,role,status
     FROM users WHERE username=$1`,
    [d.username.toLowerCase()]
  );

  if (!result.rowCount || !(await verifyPassword(d.password, result.rows[0].password_hash))) {
    return res.status(401).json({ error: "invalid credentials" });
  }

  const user = result.rows[0];
  if (user.status !== "ACTIVE") {
    return res.status(403).json({ error: "account not active", status: user.status });
  }

  const existingDevice = await pool.query(
    "SELECT user_id,revoked_at FROM devices WHERE id=$1",
    [d.deviceId]
  );

  if (existingDevice.rowCount && String(existingDevice.rows[0].user_id) !== String(user.id)) {
    return res.status(409).json({ error: "device belongs to another user" });
  }

  if (existingDevice.rows[0]?.revoked_at) {
    return res.status(403).json({ error: "device revoked" });
  }

  await pool.query(
    `INSERT INTO devices(id,user_id,device_name,public_key,last_seen_at)
     VALUES($1,$2,$3,$4,NOW())
     ON CONFLICT(id) DO UPDATE SET
       device_name=EXCLUDED.device_name,
       public_key=COALESCE(EXCLUDED.public_key,devices.public_key),
       last_seen_at=NOW()`,
    [d.deviceId, user.id, d.deviceName, d.publicKey || null]
  );

  const session = await issueSession(user, d.deviceId);
  res.json({
    user: {
      id: user.id, username: user.username, displayName: user.display_name,
      role: user.role, status: user.status
    },
    ...session
  });
});

app.post("/auth/refresh", async (req, res) => {
  const refreshToken = String(req.body?.refreshToken || "");
  if (!refreshToken) return res.status(400).json({ error: "refresh token required" });

  const hash = hashRefreshToken(refreshToken);
  const result = await pool.query(
    `SELECT s.id AS session_id,s.user_id,s.device_id,s.expires_at,s.revoked_at,
            u.username,u.display_name,u.role,u.status,d.revoked_at AS device_revoked_at
     FROM sessions s
     JOIN users u ON u.id=s.user_id
     JOIN devices d ON d.id=s.device_id
     WHERE s.refresh_token_hash=$1`,
    [hash]
  );

  if (!result.rowCount) return res.status(401).json({ error: "invalid refresh token" });
  const row = result.rows[0];
  if (row.revoked_at || row.device_revoked_at || row.status !== "ACTIVE" ||
      new Date(row.expires_at) <= new Date()) {
    return res.status(401).json({ error: "session unavailable" });
  }

  const newRefresh = crypto.randomBytes(48).toString("base64url");
  const newHash = hashRefreshToken(newRefresh);
  const newExpiry = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000);

  await pool.query(
    `UPDATE sessions SET refresh_token_hash=$1,expires_at=$2 WHERE id=$3`,
    [newHash, newExpiry, row.session_id]
  );

  res.json({
    accessToken: signAccessToken({ id: row.user_id, role: row.role }, row.device_id),
    refreshToken: newRefresh,
    refreshExpiresAt: newExpiry.toISOString()
  });
});

app.post("/auth/logout", auth, async (req, res) => {
  await pool.query(
    `UPDATE sessions SET revoked_at=NOW()
     WHERE user_id=$1 AND device_id=$2 AND revoked_at IS NULL`,
    [req.auth.sub, req.auth.deviceId]
  );
  res.status(204).end();
});

app.get("/me", auth, async (req, res) => {
  const result = await pool.query(
    `SELECT id,username,display_name,role,status,created_at
     FROM users WHERE id=$1`,
    [req.auth.sub]
  );
  if (!result.rowCount) return res.sendStatus(404);
  res.json(result.rows[0]);
});

app.get("/admin/users", auth, requireRole("OWNER", "ADMIN"), async (req, res) => {
  const result = await pool.query(
    `SELECT id,username,display_name,role,status,created_at
     FROM users ORDER BY created_at DESC`
  );
  res.json(result.rows);
});

app.post("/admin/users/:id/approve", auth, requireRole("OWNER", "ADMIN"), async (req, res) => {
  const result = await pool.query(
    `UPDATE users SET status='ACTIVE',updated_at=NOW()
     WHERE id=$1 AND status='PENDING'
     RETURNING id,username,status`,
    [req.params.id]
  );
  if (!result.rowCount) return res.status(404).json({ error: "pending user not found" });
  res.json(result.rows[0]);
});

app.post("/admin/users/:id/reject", auth, requireRole("OWNER", "ADMIN"), async (req, res) => {
  const result = await pool.query(
    `UPDATE users SET status='REJECTED',updated_at=NOW()
     WHERE id=$1 AND status='PENDING'
     RETURNING id,username,status`,
    [req.params.id]
  );
  if (!result.rowCount) return res.status(404).json({ error: "pending user not found" });
  res.json(result.rows[0]);
});

app.post("/admin/users/:id/block", auth, requireRole("OWNER", "ADMIN"), async (req, res) => {
  if (String(req.params.id) === String(req.auth.sub)) {
    return res.status(400).json({ error: "cannot block yourself" });
  }

  const target = await pool.query("SELECT role FROM users WHERE id=$1", [req.params.id]);
  if (!target.rowCount) return res.sendStatus(404);

  if (req.auth.role === "ADMIN" && target.rows[0].role !== "USER") {
    return res.status(403).json({ error: "admin cannot block privileged user" });
  }

  await pool.query("UPDATE users SET status='BLOCKED',updated_at=NOW() WHERE id=$1", [req.params.id]);
  await pool.query("UPDATE sessions SET revoked_at=NOW() WHERE user_id=$1 AND revoked_at IS NULL", [req.params.id]);
  res.json({ status: "blocked" });
});

app.post("/admin/users/:id/unblock", auth, requireRole("OWNER", "ADMIN"), async (req, res) => {
  const target = await pool.query("SELECT role FROM users WHERE id=$1", [req.params.id]);
  if (!target.rowCount) return res.sendStatus(404);

  if (req.auth.role === "ADMIN" && target.rows[0].role !== "USER") {
    return res.status(403).json({ error: "admin cannot unblock privileged user" });
  }

  await pool.query("UPDATE users SET status='ACTIVE',updated_at=NOW() WHERE id=$1", [req.params.id]);
  res.json({ status: "active" });
});

app.patch("/admin/users/:id/role", auth, requireRole("OWNER"), async (req, res) => {
  const role = req.body?.role;
  if (!["ADMIN", "USER"].includes(role)) return res.status(400).json({ error: "invalid role" });
  if (String(req.params.id) === String(req.auth.sub)) {
    return res.status(400).json({ error: "cannot change own owner role" });
  }

  const result = await pool.query(
    `UPDATE users SET role=$1,updated_at=NOW() WHERE id=$2
     RETURNING id,username,role`,
    [role, req.params.id]
  );
  if (!result.rowCount) return res.sendStatus(404);
  res.json(result.rows[0]);
});

app.get("/devices", auth, async (req, res) => {
  const result = await pool.query(
    `SELECT id,device_name,revoked_at,created_at,last_seen_at
     FROM devices WHERE user_id=$1 ORDER BY last_seen_at DESC`,
    [req.auth.sub]
  );
  res.json(result.rows);
});

app.post("/devices/:id/revoke", auth, async (req, res) => {
  const result = await pool.query(
    `UPDATE devices SET revoked_at=NOW()
     WHERE id=$1 AND user_id=$2
     RETURNING id,revoked_at`,
    [req.params.id, req.auth.sub]
  );
  if (!result.rowCount) return res.sendStatus(404);

  await pool.query(
    "UPDATE sessions SET revoked_at=NOW() WHERE device_id=$1 AND revoked_at IS NULL",
    [req.params.id]
  );

  res.json(result.rows[0]);
});

const server = http.createServer(app);
const wss = new WebSocketServer({ server, path: "/ws" });

wss.on("connection", ws => {
  ws.send(JSON.stringify({ type: "system", event: "connected" }));
});

async function recoverOwnerPasswordIfRequested() {
  const password = process.env.OWNER_RESET_PASSWORD;
  if (!password) return;

  if (password.length < 10 || password.length > 128) {
    throw new Error("OWNER_RESET_PASSWORD must be 10-128 characters");
  }

  const username = String(process.env.OWNER_RESET_USERNAME || "wzrd0us")
    .trim()
    .toLowerCase();

  const passwordHash = await hashPassword(password);

  const result = await pool.query(
    `UPDATE users
     SET password_hash=$1,status='ACTIVE',updated_at=NOW()
     WHERE username=$2 AND role='OWNER'
     RETURNING id,username`,
    [passwordHash, username]
  );

  if (result.rowCount !== 1) {
    throw new Error("OWNER password recovery target not found or ambiguous");
  }

  await pool.query(
    "UPDATE sessions SET revoked_at=NOW() WHERE user_id=$1 AND revoked_at IS NULL",
    [result.rows[0].id]
  );

  console.log(`OWNER password recovery applied for ${result.rows[0].username}`);
}

initDb()
  .then(async () => {
    await recoverOwnerPasswordIfRequested();
    const port = Number(process.env.PORT || 3000);
    server.listen(port, "0.0.0.0", () =>
      console.log(`Leviathan API v0.3.1 listening on ${port}`)
    );
  })
  .catch(err => {
    console.error("Database initialization failed:", err.message);
    process.exit(1);
  });
