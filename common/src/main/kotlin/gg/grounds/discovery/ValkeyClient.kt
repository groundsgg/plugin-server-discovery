package gg.grounds.discovery

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.ScanParams
import redis.clients.jedis.ScanResult

class ValkeyClient(config: ValkeyConfig) : AutoCloseable {
    private val pool = JedisPool(config.host, config.port)

    fun setWithTtl(key: String, value: String, ttlSeconds: Long) {
        withJedis { jedis -> jedis.setex(key, ttlSeconds, value) }
    }

    fun delete(key: String) {
        withJedis { jedis -> jedis.del(key) }
    }

    fun scanValues(pattern: String): Map<String, String> {
        /* Redis SCAN logic as documented here:
         * https://redis.io/docs/latest/commands/scan/
         */
        return withJedis { jedis ->
            val params = ScanParams().match(pattern)
            val values = mutableMapOf<String, String>()

            var cursor = ScanParams.SCAN_POINTER_START
            do {
                val page: ScanResult<String> = jedis.scan(cursor, params)
                cursor = page.cursor

                val keys = page.result
                if (keys.isEmpty()) {
                    continue
                }
                val fetchedValues = jedis.mget(*keys.toTypedArray())
                for (i in keys.indices) {
                    val value = fetchedValues[i]
                    if (value != null) {
                        values[keys[i]] = value
                    }
                }
            } while (cursor != ScanParams.SCAN_POINTER_START)

            values
        }
    }

    override fun close() {
        pool.close()
    }

    private inline fun <T> withJedis(block: (Jedis) -> T): T = pool.resource.use(block)
}
