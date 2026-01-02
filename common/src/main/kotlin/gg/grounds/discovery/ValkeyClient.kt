package gg.grounds.discovery

import redis.clients.jedis.ConnectionPoolConfig
import redis.clients.jedis.RedisClient
import redis.clients.jedis.params.ScanParams
import redis.clients.jedis.resps.ScanResult

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

    fun setWithTtl(key: String, value: String, ttlSeconds: Long) {
        client.setex(key, ttlSeconds, value)
    }

    fun delete(key: String) {
        client.del(key)
    }

    fun scanValues(pattern: String): Map<String, String> {
        val params = ScanParams().match(pattern)
        val values = mutableMapOf<String, String>()

        var cursor = ScanParams.SCAN_POINTER_START
        do {
            val page: ScanResult<String> = client.scan(cursor, params)
            cursor = page.cursor

            val keys = page.result
            if (keys.isEmpty()) continue

            val fetchedValues = client.mget(*keys.toTypedArray())
            for (i in keys.indices) {
                val value = fetchedValues[i]
                if (value != null) values[keys[i]] = value
            }
        } while (cursor != ScanParams.SCAN_POINTER_START)

        return values
    }

    override fun close() {
        client.close()
    }
}
