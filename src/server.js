require("dotenv").config();

const http = require("http");
const express = require("express");
const helmet = require("helmet");
const cors = require("cors");
const { WebSocketServer } = require("ws");

const app = express();
app.disable("x-powered-by");
app.use(helmet());
app.use(cors({ origin: process.env.CORS_ORIGIN || true }));
app.use(express.json({ limit: "1mb" }));

app.get("/", (_req, res) => {
  res.json({
    name: "Leviathan API",
    status: "ok",
    version: "0.1.0"
  });
});

app.get("/health", (_req, res) => {
  res.status(200).json({
    status: "ok",
    uptime: Math.round(process.uptime()),
    timestamp: new Date().toISOString()
  });
});

app.get("/api/v1/status", (_req, res) => {
  res.json({
    api: "online",
    websocket: "online",
    databaseConfigured: Boolean(process.env.DATABASE_URL),
    redisConfigured: Boolean(process.env.REDIS_URL)
  });
});

const server = http.createServer(app);
const wss = new WebSocketServer({ server, path: "/ws" });

wss.on("connection", (socket) => {
  socket.send(JSON.stringify({
    type: "system",
    event: "connected",
    message: "Leviathan WebSocket connected"
  }));

  socket.on("message", (raw) => {
    // Temporary echo for deployment verification.
    // Real message routing + E2EE envelopes will be added in the next stage.
    socket.send(JSON.stringify({
      type: "echo",
      payload: raw.toString()
    }));
  });
});

const port = Number(process.env.PORT || 3000);
server.listen(port, "0.0.0.0", () => {
  console.log(`Leviathan API listening on port ${port}`);
});
