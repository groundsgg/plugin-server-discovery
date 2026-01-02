package gg.grounds.discovery

import redis.clients.jedis.ConnectionPoolConfig
import redis.clients.jedis.RedisClient

class ValkeyClient(config: ValkeyConfig) : AutoCloseable {

    private val client: RedisClient =
        RedisClient.builder()
            .hostAndPort(config.host, config.port)
            .poolConfig(
                ConnectionPoolConfig().apply {
                    maxTotal = 8
                    maxIdle = 8
                    minIdle = 1
                }
            )
            .build()

    fun hset(key: String, field: String, value: String) {
        client.hset(key, field, value)
    }

    fun hgetAll(key: String): Map<String, String> {
        return client.hgetAll(key)
    }

    fun hdel(key: String, field: String) {
        client.hdel(key, field)
    }

    override fun close() {
        client.close()
    }
}
