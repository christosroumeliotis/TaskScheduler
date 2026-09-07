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
            local current_key = KEYS[1]
                         local previous_key = KEYS[2]
                         local max_requests = tonumber(ARGV[1])
                         local window_seconds = tonumber(ARGV[2])
                         local elapsed = tonumber(ARGV[3])
            
                         local prev_count = tonumber(redis.call('GET', previous_key) or '0') or 0
                         local current_count = tonumber(redis.call('GET', current_key) or '0') or 0
            
                         local weighted_prev = prev_count * (1 - elapsed)
                         local estimated = weighted_prev + current_count
            
                         if estimated >= max_requests then
                           return { 0, 0, math.floor(current_count) }
                         end
            
                         local new_count = redis.call('INCR', current_key)
            
                         if new_count == 1 then
                           redis.call('EXPIRE', current_key, window_seconds * 2)
                         end
            
                         local new_estimate = weighted_prev + new_count
                         local remaining = math.max(0, math.floor(max_requests - new_estimate))
            
                         return { 1, remaining, new_count }
            """;

    @SuppressWarnings("unchecked")
    public static boolean allow(JedisPool pool, String key, int limit, int windowSeconds) {
        try (Jedis jedis = pool.getResource()) {
            long now = System.currentTimeMillis() / 1000L;
            long currentWindow = now / windowSeconds;
            long previousWindow = currentWindow - 1;

            String currentKey = "{" + key + "}:" + currentWindow;
            String previousKey = "{" + key + "}:" + previousWindow;

            double elapsed = (double) (now % windowSeconds) / (double) windowSeconds;

            List<Long> result = (List<Long>) jedis.eval(
                    SCRIPT,
                    List.of(currentKey, previousKey),
                    List.of(String.valueOf(limit), String.valueOf(windowSeconds), String.valueOf(elapsed)));

            return result.get(0) == 1L;
        }
    }
}
