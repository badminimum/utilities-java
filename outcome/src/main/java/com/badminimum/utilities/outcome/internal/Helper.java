package com.badminimum.utilities.outcome.internal;

import com.badminimum.utilities.outcome.FailureOutcome;
import com.badminimum.utilities.outcome.Outcome;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Helper {
    Set<Class<? extends Exception>> EXCEPTIONS_ILLEGAL_TO_CATCH = Set.of(
            NullPointerException.class,
            IndexOutOfBoundsException.class,
            IllegalMonitorStateException.class,
            ConcurrentModificationException.class
    );

    @Contract(pure = true)
    static <T> @Nullable T orElse(@Nullable T nullable, @Nullable T fallback) {
        return (nullable == null) ? fallback : nullable;
    }

    static <Value> Outcome<Value> tryCatch(Supplier<? extends Outcome<Value>> successSupplier,
            Function<? super Exception, FailureOutcome<Value>> failureFunction) {
        try {
            return successSupplier.get();
        } catch (Exception exception) {
            if (Helper.EXCEPTIONS_ILLEGAL_TO_CATCH.contains(exception.getClass())) {
                throw exception;
            }
            return failureFunction.apply(exception);
        }
    }
}
