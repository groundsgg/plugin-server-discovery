package gg.grounds.discovery;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ValkeyClient implements AutoCloseable {
    private final JedisPool pool;

    public ValkeyClient(ValkeyConfig config) {
        this.pool = new JedisPool(config.host(), config.port());
    }

    public void setWithTtl(String key, String value, long ttlSeconds) {
        try (Jedis jedis = open()) {
            jedis.setex(key, ttlSeconds, value);
        }
    }

    public void delete(String key) {
        try (Jedis jedis = open()) {
            jedis.del(key);
        }
    }

    public Map<String, String> scanValues(String pattern) {
        /* Redis SCAN logic as documented here:
         * https://redis.io/docs/latest/commands/scan/
         */
        try (Jedis jedis = open()) {
            ScanParams params = new ScanParams().match(pattern);
            Map<String, String> values = new HashMap<>();

            String cursor = ScanParams.SCAN_POINTER_START;
            do {
                ScanResult<String> page = jedis.scan(cursor, params);
                cursor = page.getCursor();

                List<String> keys = page.getResult();
                if (keys.isEmpty()) {
                    continue;
                }
                List<String> fetchedValues = jedis.mget(keys.toArray(new String[0]));
                for (int i = 0; i < keys.size(); i++) {
                    String value = fetchedValues.get(i);
                    if (value == null) {
                        continue;
                    }
                    values.put(keys.get(i), value);
                }
            } while (!ScanParams.SCAN_POINTER_START.equals(cursor));

            return values;
        }
    }

    @Override
    public void close() {
        pool.close();
    }

    private Jedis open() {
        return pool.getResource();
    }
}
