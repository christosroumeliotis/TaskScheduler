package com.task.scheduler.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;

import java.io.IOException;

@Component
@Order(-1)
public class RateLimitFilter implements Filter {

    private final JedisPool jedisPool;

    public RateLimitFilter(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;

        String key = "ip:" + httpReq.getRemoteAddr();
        boolean result = SlidingWindowCounterLimiter.allow(jedisPool, key, 5, 60);

        if (!result) {
            System.err.println("Rate limit exceeded for IP: " + httpReq.getRemoteAddr());
            httpRes.setStatus(429);
            httpRes.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
            return;
        }
        chain.doFilter(req, res);
    }
}
