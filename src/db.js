const { Pool } = require("pg");

if (!process.env.DATABASE_URL) {
  throw new Error("DATABASE_URL is required");
}

const isRailwayPrivate =
  process.env.DATABASE_URL.includes("railway.internal") ||
  process.env.DATABASE_URL.includes("postgres.railway.internal");

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: isRailwayPrivate ? false : { rejectUnauthorized: false },
});

async function initDb() {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS users (
      id BIGSERIAL PRIMARY KEY,
      username VARCHAR(64) UNIQUE NOT NULL,
      display_name VARCHAR(128) NOT NULL,
      password_hash TEXT NOT NULL,
      role VARCHAR(16) NOT NULL DEFAULT 'USER'
        CHECK (role IN ('OWNER','ADMIN','USER')),
      status VARCHAR(16) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','ACTIVE','BLOCKED','REJECTED')),
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS devices (
      id UUID PRIMARY KEY,
      user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      device_name VARCHAR(128) NOT NULL,
      public_key TEXT,
      revoked_at TIMESTAMPTZ,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      last_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS sessions (
      id UUID PRIMARY KEY,
      user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
      refresh_token_hash TEXT NOT NULL,
      expires_at TIMESTAMPTZ NOT NULL,
      revoked_at TIMESTAMPTZ,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS chats (
      id UUID PRIMARY KEY,
      type VARCHAR(16) NOT NULL CHECK (type IN ('DIRECT','GROUP')),
      title VARCHAR(128),
      created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS chat_members (
      chat_id UUID NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
      user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      member_role VARCHAR(16) NOT NULL DEFAULT 'MEMBER'
        CHECK (member_role IN ('OWNER','MEMBER')),
      joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      left_at TIMESTAMPTZ,
      PRIMARY KEY(chat_id,user_id)
    );

    CREATE TABLE IF NOT EXISTS messages (
      id UUID PRIMARY KEY,
      chat_id UUID NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
      sender_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
      sender_device_id UUID NOT NULL REFERENCES devices(id) ON DELETE RESTRICT,
      client_message_id UUID NOT NULL,
      algorithm VARCHAR(64) NOT NULL,
      nonce TEXT NOT NULL,
      ciphertext TEXT NOT NULL,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      UNIQUE(sender_device_id,client_message_id)
    );

    CREATE INDEX IF NOT EXISTS sessions_user_id_idx ON sessions(user_id);
    CREATE INDEX IF NOT EXISTS devices_user_id_idx ON devices(user_id);
    CREATE INDEX IF NOT EXISTS chat_members_user_id_idx
      ON chat_members(user_id) WHERE left_at IS NULL;
    CREATE INDEX IF NOT EXISTS messages_chat_created_idx
      ON messages(chat_id,created_at DESC);
  `);
}

module.exports = { pool, initDb };
