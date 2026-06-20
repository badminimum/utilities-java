package com.badminimum.utilities.outcome;


import com.badminimum.utilities.outcome.exception.OutcomeFailureExtractionException;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

import static com.badminimum.utilities.outcome.internal.Helper.orElse;

@NullMarked
@SuppressWarnings({"PublicMethodNotExposedInInterface", "unused"})
public record FailureOutcome<Value>(
        // Annotated as Nullable, but is set to DEFAULT_MESSAGE when null
        @Nullable String message,
        @Nullable Exception exception,
        @Nullable Outcome<?> outcome
) implements Outcome<Value> {
    private static final String DEFAULT_MESSAGE = "No message provided.";

    public FailureOutcome {
        if (message == null) {
            message = DEFAULT_MESSAGE;
        }
    }

    public FailureOutcome(@Nullable String message) {
        this(message, null, null);
    }

    public FailureOutcome(@Nullable Exception exception) {
        this((exception != null) ? exception.getMessage() : DEFAULT_MESSAGE, exception, null);
    }

    public FailureOutcome(@Nullable Outcome<?> outcome) {
        this(DEFAULT_MESSAGE, null, outcome);
    }

    @SuppressWarnings("DataFlowIssue")
    @Contract(pure = true)
    @Override
    public String message() {
        return this.message;
    }

    @Contract(pure = true)
    public <NewValue> FailureOutcome<NewValue> with(@Nullable String message) {
        return new FailureOutcome<>(message, this.exception, this.outcome);
    }

    @Contract(pure = true)
    public <NewValue> FailureOutcome<NewValue> with(@Nullable Exception exception) {
        return (exception == null)
                ? new FailureOutcome<>(this.message, null, this.outcome)
                : new FailureOutcome<>(
                        orElse(this.message, exception.getMessage()),
                        exception,
                        this.outcome
                );
    }

    @Contract(pure = true)
    public <NewValue> FailureOutcome<NewValue> with(@Nullable Outcome<?> outcome) {
        return new FailureOutcome<>(this.message, this.exception, outcome);
    }

    @Contract(pure = true)
    public Optional<Exception> exceptionOrEmpty() {
        return Optional.ofNullable(this.exceptionOrNull());
    }

    @Contract(pure = true)
    public @Nullable Exception exceptionOrNull() {
        return this.exception;
    }

    public void rethrow() {
        if (this.exception == null) {
            return;
        }
        if (!(this.exception instanceof RuntimeException)) {
            return;
        }
        throw (RuntimeException) this.exception;
    }

    public <E extends Exception> void rethrowChecked(Class<E> exceptionClass) throws E {
        if (this.exception == null) {
            return;
        }
        if (!exceptionClass.isInstance(this.exception)) {
            return;
        }
        throw exceptionClass.cast(this.exception);
    }

    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    public <NewValue> Outcome<NewValue> coerce() {
        return (Outcome<NewValue>) this;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public Value getOrThrow() {
        throw new OutcomeFailureExtractionException(this.message, this.exception);
    }

    @Contract(pure = true)
    @Override
    public Optional<Value> getOrEmpty() {
        return Optional.empty();
    }

    @Contract(pure = true)
    @Override
    public Value getOrDefault(Value fallback) {
        return fallback;
    }

    @Override
    public Value getOrElse(Supplier<? extends Value> fallbackSupplier) {
        return fallbackSupplier.get();
    }
}