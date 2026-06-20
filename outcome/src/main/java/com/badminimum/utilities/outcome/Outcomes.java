package com.badminimum.utilities.outcome;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.badminimum.utilities.outcome.Outcome.*;
import static com.badminimum.utilities.outcome.internal.Helper.tryCatch;

@SuppressWarnings("unused")
public interface Outcomes {
    static <Value> Outcome<Value> run(Supplier<? extends Value> valueSupplier) {
        return success(valueSupplier.get());
    }

    static Outcome<Outcome.Empty> run(Runnable runnable) {
        runnable.run();
        return empty();
    }

    static <Value> Outcome<Value> runCatching(Supplier<? extends Value> valueSupplier,
            Function<? super Exception, FailureOutcome<Value>> failureMethod) {
        return tryCatch(
                () -> run(valueSupplier),
                failureMethod
        );
    }

    static <Value> Outcome<Value> runCatching(Supplier<? extends Value> valueSupplier) {
        return runCatching(valueSupplier,
                (var exception) -> failure("Exception in runCatching").with(exception));
    }

    static Outcome<Outcome.Empty> runCatching(Runnable runnable,
            Function<? super Exception, FailureOutcome<Outcome.Empty>> failureMethod) {
        return tryCatch(
                () -> run(runnable),
                failureMethod
        );
    }

    static Outcome<Outcome.Empty> runCatching(Runnable runnable) {
        return runCatching(runnable,
                (var exception) -> failure("Exception in runCatching").with(exception));
    }

    static <Value> Outcome<Value> runOutcome(Supplier<? extends Outcome<Value>> outcomeSupplier) {
        return outcomeSupplier.get();
    }

    static <Value> Outcome<Value> runCatchingOutcome(Supplier<? extends Outcome<Value>> outcomeSupplier,
            Function<? super Exception, FailureOutcome<Value>> failureMethod) {
        return tryCatch(
                () -> runOutcome(outcomeSupplier),
                failureMethod
        );
    }

    static <Value> Outcome<Value> runCatchingOutcome(Supplier<? extends Outcome<Value>> outcomeSupplier) {
        return runCatchingOutcome(outcomeSupplier,
                (var exception) -> failure("Exception in runCatchingOutcome").with(exception));
    }

    static <Type> Outcome<List<Type>> sequence(Iterable<? extends Outcome<Type>> outcomes) {
        final List<Type> results = new ArrayList<>();
        for (var outcome : outcomes) {
            if (outcome instanceof FailureOutcome<Type> failure) {
                return failure.coerce();
            }

            results.add(outcome.getOrThrow());
        }
        return success(results);
    }

    static <Type, Return> Outcome<List<Return>> traverse(
            Iterable<Type> items,
            Function<? super Type, ? extends Outcome<Return>> mapper
    ) {
        final List<Return> results = new ArrayList<>();
        for (var item : items) {
            final var outcome = mapper.apply(item);
            if (outcome instanceof FailureOutcome<Return> failure) {
                return failure.coerce();
            }
            results.add(outcome.getOrThrow());
        }
        return success(results);
    }
}
