package com.badminimum.utilities.outcome.exception;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class OutcomeFailureExtractionException extends RuntimeException {
    public OutcomeFailureExtractionException(@NonNull String failureMessage, @Nullable Exception failureException) {
        super("Attempted to retrieve a value from a Failure outcome: " + failureMessage);
        if (failureException != null) {
            this.initCause(failureException);
        }
    }
}
