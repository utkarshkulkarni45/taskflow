package com.example.taskflow.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private static final int API_REQUESTS_PER_MINUTE = 60;
    private static final int AUTH_REQUESTS_PER_MINUTE = 10;
    private static final Duration BUCKET_IDLE_TTL = Duration.ofMinutes(10);
    private static final Set<String> STRICT_PATHS = Set.of("/api/auth/login", "/api/auth/register");

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(BUCKET_IDLE_TTL)
            .maximumSize(100_000)
            .build();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Rate limit only API calls
        if (path.startsWith("/api/")) {
            // Never trust forwarding headers here: with server.forward-headers-strategy=native
            // the container rewrites getRemoteAddr() from the proxy's headers instead.
            String clientIp = request.getRemoteAddr();
            boolean strict = STRICT_PATHS.contains(path);
            String bucketKey = (strict ? "auth:" : "api:") + clientIp;

            Bucket bucket = buckets.get(bucketKey, key -> strict
                    ? createBucket(AUTH_REQUESTS_PER_MINUTE)
                    : createBucket(API_REQUESTS_PER_MINUTE));

            if (!bucket.tryConsume(1)) {
                log.debug("Rate limit exhausted for {} on {}", bucketKey, path);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Too many requests. Please slow down.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Bucket createBucket(int requestsPerMinute) {
        Bandwidth limit = Bandwidth.classic(
                requestsPerMinute,
                Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
