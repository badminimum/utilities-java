package com.badminimum.utilities.outcome.exception;

public final class OutcomeSuccessNonEmptyMarkedAsEmptyException extends RuntimeException {
    public OutcomeSuccessNonEmptyMarkedAsEmptyException() {
        super("Cannot mark a Success Outcome as empty when it's not empty.");
    }
}
