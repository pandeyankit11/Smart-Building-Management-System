package com.smartbuilding.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates process-wide unique, readable identifiers for domain objects.
 */
public final class IdGenerator {
    private static final AtomicLong SEQUENCE = new AtomicLong(System.currentTimeMillis());

    private IdGenerator() {
    }

    public static String next(String prefix) {
        return prefix + SEQUENCE.incrementAndGet();
    }
}
