const Redis = require("ioredis");

let redis = null;

function getRedis() {
  if (!process.env.REDIS_URL) return null;
  if (!redis) {
    redis = new Redis(process.env.REDIS_URL, {
      maxRetriesPerRequest: 2
    });
  }
  return redis;
}

module.exports = { getRedis };
