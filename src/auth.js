const crypto = require("crypto");
const jwt = require("jsonwebtoken");
const bcrypt = require("bcryptjs");
const { pool } = require("./db");

const JWT_SECRET = process.env.JWT_SECRET;
if (!JWT_SECRET || JWT_SECRET.length < 32) {
  throw new Error("JWT_SECRET must be at least 32 characters");
}

function signAccessToken(user, deviceId) {
  return jwt.sign(
    { sub: String(user.id), role: user.role, deviceId },
    JWT_SECRET,
    { expiresIn: "15m", issuer: "leviathan-api", audience: "leviathan-android" }
  );
}

function randomRefreshToken() {
  return crypto.randomBytes(48).toString("base64url");
}

function hashRefreshToken(token) {
  return crypto.createHash("sha256").update(token).digest("hex");
}

async function issueSession(user, deviceId) {
  const refreshToken = randomRefreshToken();
  const hash = hashRefreshToken(refreshToken);
  const sessionId = crypto.randomUUID();
  const expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000);

  await pool.query(
    `INSERT INTO sessions(id,user_id,device_id,refresh_token_hash,expires_at)
     VALUES($1,$2,$3,$4,$5)`,
    [sessionId, user.id, deviceId, hash, expiresAt]
  );

  return {
    accessToken: signAccessToken(user, deviceId),
    refreshToken,
    refreshExpiresAt: expiresAt.toISOString()
  };
}

async function hashPassword(password) {
  return bcrypt.hash(password, 12);
}

async function verifyPassword(password, hash) {
  return bcrypt.compare(password, hash);
}

function auth(req, res, next) {
  const header = req.headers.authorization || "";
  const token = header.startsWith("Bearer ") ? header.slice(7) : null;
  if (!token) return res.status(401).json({ error: "unauthorized" });

  try {
    req.auth = jwt.verify(token, JWT_SECRET, {
      issuer: "leviathan-api",
      audience: "leviathan-android"
    });
    next();
  } catch {
    return res.status(401).json({ error: "unauthorized" });
  }
}

function requireRole(...roles) {
  return (req, res, next) => {
    if (!req.auth || !roles.includes(req.auth.role)) {
      return res.status(403).json({ error: "forbidden" });
    }
    next();
  };
}

module.exports = {
  auth, requireRole, issueSession, hashPassword, verifyPassword,
  hashRefreshToken, signAccessToken
};
