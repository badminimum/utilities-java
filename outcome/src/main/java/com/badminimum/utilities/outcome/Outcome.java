package com.badminimum.utilities.outcome;

import com.badminimum.utilities.outcome.exception.OutcomeFailureExtractionException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;

@SuppressWarnings("unused")
@NullMarked
public sealed interface Outcome<Value> permits Outcome.Failure, Outcome.Success {
    private static <T> @Nullable T orElse(@Nullable T nullable, @Nullable T fallback) {
        return (nullable == null) ? fallback : nullable;
    }

    static <Value> Success<Value> success(Value value) {
        return new Success<>(value);
    }

    static <Value> Failure<Value> failure(
            @Nullable String message,
            @Nullable Exception exception,
            @Nullable Outcome<?> outcome
    ) {
        return new Failure<>(message, exception, outcome);
    }

    static <Value> Failure<Value> failure(@Nullable String message) {
        return new Failure<>(message);
    }

    static <Value> Failure<Value> failure(@Nullable Exception exception) {
        return new Failure<>(exception);
    }

    static <Value> Failure<Value> failure(@Nullable Outcome<?> outcome) {
        return new Failure<>(outcome);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <Value> Outcome<Value> fromOptional(
            Optional<? extends Value> optional,
            Supplier<Failure<Value>> failureSupplier
    ) {
        return optional.<Outcome<Value>>map(Outcome::success).orElseGet(failureSupplier);
    }

    static <Value> Outcome<Value> fromNullable(
            @Nullable Value value,
            Supplier<Failure<Value>> failureSupplier
    ) {
        return (value != null) ? success(value) : failureSupplier.get();
    }

    static <Value> Outcome<Value> runCatching(Supplier<? extends Value> valueSupplier,
            Function<? super Exception, Failure<Value>> failureMethod) {
        try {
            return new Success<>(valueSupplier.get());
        } catch (Exception exception) {
            return failureMethod.apply(exception);
        }
    }

    static <Value> Outcome<Value> runCatching(Supplier<? extends Value> valueSupplier) {
        return runCatching(valueSupplier,
                (Exception exception) -> failure("Exception in runCatching").with(exception));
    }

    static <Value> Outcome<Value> runCatchingOutcome(Supplier<? extends Outcome<Value>> outcomeSupplier,
            Function<? super Exception, Failure<Value>> failureMethod) {
        try {
            return outcomeSupplier.get();
        } catch (Exception exception) {
            return failureMethod.apply(exception);
        }
    }

    static <Value> Outcome<Value> runCatchingOutcome(Supplier<? extends Outcome<Value>> outcomeSupplier) {
        return runCatchingOutcome(outcomeSupplier,
                (Exception exception) -> failure("Exception in runCatchingOutcome").with(exception));
    }

    default <Return> Outcome<Return> map(Function<? super Value, ? extends Return> transformer) {
        return switch (this) {
            case Outcome.Failure<Value> failure -> failure.coerce();
            case Success<Value>(var value) -> new Success<>(transformer.apply(value));
        };
    }

    default <Return> Outcome<Return> mapCatching(Function<? super Value, ? extends Return> transformer) {
        return switch (this) {
            case Outcome.Failure<Value> failure -> failure.coerce();
            case Success<Value>(var value) -> {
                try {
                    yield new Success<>(transformer.apply(value));
                } catch (Exception exception) {
                    yield failure("Exception in mapCatching").with(exception);
                }
            }
        };
    }

    default Outcome<Value> mapFailure(Function<? super Failure<Value>, Failure<Value>> transformer) {
        return switch (this) {
            case Outcome.Failure<Value> failure -> transformer.apply(failure);
            case Outcome.Success<Value> v -> this;
        };
    }

    default <Return> Outcome<Return> flatMap(Function<? super Value, ? extends Outcome<Return>> transformer) {
        return switch (this) {
            case Outcome.Failure<Value> failure -> failure.coerce();
            case Success<Value>(var value) -> transformer.apply(value);
        };
    }

    default <Return> Outcome<Return> flatMapCatching(Function<? super Value, ? extends Outcome<Return>> transformer) {
        return switch (this) {
            case Outcome.Failure<Value> failure -> failure.coerce();
            case Success<Value>(var value) -> {
                try {
                    yield transformer.apply(value);
                } catch (Exception exception) {
                    yield failure("Exception in flatMapCatching").with(exception);
                }
            }
        };
    }

    default <Return> Return fold(
            Function<? super Value, ? extends Return> onSuccess,
            Function<? super Failure<Value>, ? extends Return> onFailure
    ) {
        return switch (this) {
            case Failure<Value> failure -> onFailure.apply(failure);
            case Success<Value>(var value) -> onSuccess.apply(value);
        };
    }

    default Outcome<Value> recover(
            Function<? super Failure<Value>, ? extends Value> recover) {
        return switch (this) {
            case Outcome.Failure<Value> failure -> new Success<>(recover.apply(failure));
            case Outcome.Success<Value> success -> this;
        };
    }

    default Outcome<Value> recoverCatching(
            Function<? super Failure<Value>, ? extends Value> recover) {
        return switch (this) {
            case Outcome.Failure<Value> failure -> {
                try {
                    yield new Success<>(recover.apply(failure));
                } catch (Exception exception) {
                    yield failure("Exception in recoverCatching").with(exception);
                }
            }
            case Outcome.Success<Value> success -> this;
        };
    }

    default Outcome<Value> filter(
            Predicate<? super Value> predicate,
            Function<? super Value, Failure<Value>> failureProvider
    ) {
        return switch (this) {
            case Outcome.Failure<Value> failure -> failure.coerce();
            case Success<Value>(var value) -> predicate.test(value) ? this : failureProvider.apply(value);
        };
    }

    default Outcome<Value> or(Supplier<? extends Outcome<Value>> fallbackSupplier) {
        return switch (this) {
            case Outcome.Failure<Value> ignored -> fallbackSupplier.get();
            case Outcome.Success<Value> ignored -> this;
        };
    }

    default <Other, Combined> Outcome<Combined> combine(
            Outcome<Other> other,
            BiFunction<? super Value, ? super Other, ? extends Combined> combineFunction
    ) {
        return this.flatMap((Value val1) -> other.map((Other val2) -> combineFunction.apply(val1, val2)));
    }

    default <Other, Combined> Outcome<Combined> combineCatching(
            Outcome<Other> other,
            BiFunction<? super Value, ? super Other, ? extends Combined> combineFunction
    ) {
        return this.flatMapCatching((Value val1) -> other.mapCatching((Other val2) ->
                combineFunction.apply(val1, val2)));
    }

    default Outcome<Value> onSuccess(Consumer<? super Value> action) {
        if (this instanceof Success<Value>(var value)) {
            action.accept(value);
        }
        return this;
    }

    default Outcome<Value> onFailure(Consumer<? super Failure<Value>> action) {
        if (this instanceof Failure<Value> failure) {
            action.accept(failure);
        }
        return this;
    }

    Value getOrThrow();

    Optional<Value> getOrEmpty();

    Value getOrDefault(Value fallback);

    Value getOrElse(Supplier<? extends Value> fallbackSupplier);

    @NullMarked
    record Success<Value>(Value value)
            implements Outcome<Value> {
        public Success {
            Objects.requireNonNull(value, "Success Outcome value cannot be null");
        }

        @Override
        public Value getOrThrow() {
            return this.value;
        }

        @Override
        public Optional<Value> getOrEmpty() {
            return Optional.of(this.value);
        }

        @Override
        public Value getOrDefault(Value fallback) {
            return this.value;
        }

        @Override
        public Value getOrElse(Supplier<? extends Value> fallbackSupplier) {
            return this.value;
        }
    }

    @NullMarked
    @SuppressWarnings({"PublicMethodNotExposedInInterface", "unused"})
    record Failure<Value>(
            // Annotated as Nullable, but is set to DEFAULT_MESSAGE when null
            @Nullable String message,
            @Nullable Exception exception,
            @Nullable Outcome<?> outcome
    ) implements Outcome<Value> {
        private static final String DEFAULT_MESSAGE = "No message provided.";

        public Failure {
            if (message == null) {
                message = DEFAULT_MESSAGE;
            }
        }

        public Failure(@Nullable String message) {
            this(message, null, null);
        }

        public Failure(@Nullable Exception exception) {
            this((exception != null) ? exception.getMessage() : DEFAULT_MESSAGE, exception, null);
        }

        public Failure(@Nullable Outcome<?> outcome) {
            this(DEFAULT_MESSAGE, null, outcome);
        }

        @SuppressWarnings("DataFlowIssue")
        @Override
        public String message() {
            return this.message;
        }

        public <NewValue> Failure<NewValue> with(@Nullable String message) {
            return new Failure<>(message, this.exception, this.outcome);
        }

        public <NewValue> Failure<NewValue> with(@Nullable Exception exception) {
            return (exception == null)
                    ? new Failure<>(this.message, null, this.outcome)
                    : new Failure<>(
                            Outcome.orElse(this.message, exception.getMessage()),
                            exception,
                            this.outcome
                    );
        }

        public <NewValue> Failure<NewValue> with(@Nullable Outcome<?> outcome) {
            return new Failure<>(this.message, this.exception, outcome);
        }

        public Optional<Exception> exceptionOrEmpty() {
            return Optional.ofNullable(this.exceptionOrNull());
        }

        public @Nullable Exception exceptionOrNull() {
            return this.exception;
        }

        @SuppressWarnings("unchecked")
        public <NewValue> Outcome<NewValue> coerce() {
            return (Outcome<NewValue>) this;
        }

        @SuppressWarnings("DataFlowIssue")
        @Override
        public Value getOrThrow() {
            throw new OutcomeFailureExtractionException(this.message, this.exception);
        }

        @Override
        public Optional<Value> getOrEmpty() {
            return Optional.empty();
        }

        @Override
        public Value getOrDefault(Value fallback) {
            return fallback;
        }

        @Override
        public Value getOrElse(Supplier<? extends Value> fallbackSupplier) {
            return fallbackSupplier.get();
        }
    }
}
