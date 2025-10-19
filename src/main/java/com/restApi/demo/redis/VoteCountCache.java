package com.restApi.demo.redis;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import redis.clients.jedis.JedisPooled;

public class VoteCountCache {

    private static final String REDIS_HOST = System.getenv().getOrDefault("REDIS_HOST", "localhost");
    private static final int REDIS_PORT = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));

    private static final redis.clients.jedis.JedisPooled jedis = new redis.clients.jedis.JedisPooled(REDIS_HOST, REDIS_PORT);

    private static final int TTL_SECONDS = 3600;

    private static String key(String pollId) {
        return "poll:" + pollId + ":votes";
    }

    public static Map<String, Long> get(String pollId) {
        String k = key(pollId);
        if (!Boolean.TRUE.equals(jedis.exists(k))) return null;
        Map<String, String> raw = jedis.hgetAll(k);
        Map<String, Long> out = new LinkedHashMap<>();
        raw.forEach((f, v) -> out.put(f, Long.parseLong(v)));
        return out;
    }

    public static void put(String pollId, Map<String, Long> counts) {
        String k = key(pollId);
        if (counts == null) counts = Map.of();
        Map<String, String> s = new HashMap<>();
        counts.forEach((f, c) -> s.put(f, String.valueOf(c)));
        if (!s.isEmpty()) jedis.hset(k, s);
        else jedis.hset(k, Map.of());
        jedis.expire(k, TTL_SECONDS);
    }

    public static void invalidate(String pollId) {
        jedis.del(key(pollId));
    }

}
