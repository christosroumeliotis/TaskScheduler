package com.task.scheduler.filter;

import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class SlidingWindowCounterLimiter {

    /**
     Estimated Count = (Previous Window Requests x (1 - current window elapsed time)) + Current Window Requests
     Then compared with the limit. If the estimated count is greater than or equal to the limit, the request is denied.
     */
    private static final String SCRIPT = """
            local base   = KEYS[1]
            local limit  = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])

            local t   = redis.call('TIME')
            local now = tonumber(t[1]) + tonumber(t[2]) / 1e6

            local window_num = math.floor(now / window)
            local elapsed     = (now % window) / window

            local curr_key = base .. ':' .. window_num
            local prev_key = base .. ':' .. (window_num - 1)

            local prev = tonumber(redis.call('GET', prev_key) or 0)
            local curr = tonumber(redis.call('GET', curr_key) or 0)

            local estimate = prev * (1 - elapsed) + curr

            if estimate >= limit then
                return {0, 0}
            end

            local new_count = redis.call('INCR', curr_key)
            if new_count == 1 then
                redis.call('EXPIRE', curr_key, window * 2)
            end

            return {1, 0}
            """;

    @SuppressWarnings("unchecked")
    public static boolean allow(JedisPool pool, String key, int limit, int windowSeconds) {
        try (Jedis jedis = pool.getResource()) {
            List<Long> result = (List<Long>) jedis.eval(
                    SCRIPT,
                    List.of("{" + key + "}"),
                    List.of(String.valueOf(limit), String.valueOf(windowSeconds)));

            return result.get(0) == 1L;
        }
    }
}
