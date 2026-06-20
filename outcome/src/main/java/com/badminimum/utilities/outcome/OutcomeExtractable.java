package com.badminimum.utilities.outcome;

import java.util.Optional;
import java.util.function.Supplier;

public interface OutcomeExtractable<Value> {
    Value getOrThrow();

    Optional<Value> getOrEmpty();

    Value getOrDefault(Value fallback);

    Value getOrElse(Supplier<? extends Value> fallbackSupplier);
}
