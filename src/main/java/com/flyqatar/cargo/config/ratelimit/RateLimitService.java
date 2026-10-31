package com.flyqatar.cargo.config.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private static final int LIMIT_FOR_PERIOD = 100;

    private static final Duration LIMIT_REFRESH_PERIOD =
            Duration.ofMinutes(1);

    private final Cache<String, RateLimiter> rateLimiterCache;

    public RateLimitService() {

        // Each authenticated user receives an independent rate limiter.
        this.rateLimiterCache = Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterAccess(Duration.ofMinutes(30))
                .build();
    }

    public boolean isAllowed(String userId) {

        // Create the user's limiter only when it is needed.
        RateLimiter rateLimiter = rateLimiterCache.get(
                userId,
                this::createRateLimiter
        );

        // true means a permission was acquired.
        // false means the user has exhausted the quota.
        return rateLimiter.acquirePermission();
    }

    private RateLimiter createRateLimiter(String userId) {

        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(LIMIT_FOR_PERIOD)
                .limitRefreshPeriod(LIMIT_REFRESH_PERIOD)
                .timeoutDuration(Duration.ZERO)
                .build();

        return RateLimiter.of("user-" + userId, config);
    }
}