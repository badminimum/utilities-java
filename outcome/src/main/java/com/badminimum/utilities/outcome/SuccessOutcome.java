package com.badminimum.utilities.outcome;

import com.badminimum.utilities.outcome.exception.OutcomeSuccessNonEmptyMarkedAsEmptyException;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@NullMarked
public record SuccessOutcome<Value>(Value value, boolean isEmpty)
        implements Outcome<Value> {
    public SuccessOutcome {
        Objects.requireNonNull(value, "Success Outcome value cannot be null");

        if ((value != Empty.INSTANCE) && isEmpty) {
            throw new OutcomeSuccessNonEmptyMarkedAsEmptyException();
        }
    }

    public SuccessOutcome(Value value) {
        this(value, true);
    }

    @Contract(pure = true)
    @Override
    public Value getOrThrow() {
        return this.value;
    }

    @Contract(pure = true)
    @Override
    public Optional<Value> getOrEmpty() {
        return Optional.of(this.value);
    }

    @Contract(pure = true)
    @Override
    public Value getOrDefault(Value fallback) {
        return this.value;
    }

    @Contract(pure = true)
    @Override
    public Value getOrElse(Supplier<? extends Value> fallbackSupplier) {
        return this.value;
    }
}
