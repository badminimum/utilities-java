package com.badminimum.utilities.outcome.internal;

import java.util.ConcurrentModificationException;
import java.util.Set;

public final class Helper {
    public static final Set<Class<? extends Exception>> EXCEPTIONS_ILLEGAL_TO_CATCH = Set.of(
            NullPointerException.class,
            IndexOutOfBoundsException.class,
            IllegalMonitorStateException.class,
            ConcurrentModificationException.class
    );

    private Helper() {
    }
}
