package com.badminimum.utilities.outcome;

import com.badminimum.utilities.outcome.exception.OutcomeFailureExtractionException;
import com.badminimum.utilities.outcome.internal.Helper;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;

@SuppressWarnings("unused")
@NullMarked
public sealed interface Outcome<Value> permits Outcome.Failure, Outcome.Success {
    @Contract(pure = true)
    private static <T> @Nullable T orElse(@Nullable T nullable, @Nullable T fallback) {
        return (nullable == null) ? fallback : nullable;
    }

    @Contract(pure = true)
    static <Value> Success<Value> success(Value value) {
        return new Success<>(value);
    }

    @Contract(pure = true)
    static <Value> Failure<Value> failure(
            @Nullable String message,
            @Nullable Exception exception,
            @Nullable Outcome<?> outcome
    ) {
        return new Failure<>(message, exception, outcome);
    }

    @Contract(pure = true)
    static <Value> Failure<Value> failure(@Nullable String message) {
        return new Failure<>(message);
    }

    @Contract(pure = true)
    static <Value> Failure<Value> failure(@Nullable Exception exception) {
        return new Failure<>(exception);
    }

    @Contract(pure = true)
    static <Value> Failure<Value> failure(@Nullable Outcome<?> outcome) {
        return new Failure<>(outcome);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Contract(pure = true)
    static <Value> Outcome<Value> fromOptional(
            Optional<? extends Value> optional,
            Supplier<Failure<Value>> failureSupplier
    ) {
        return optional.<Outcome<Value>>map(Outcome::success).orElseGet(failureSupplier);
    }

    @Contract(pure = true)
    static <Value> Outcome<Value> fromNullable(
            @Nullable Value value,
            Supplier<Failure<Value>> failureSupplier
    ) {
        return (value != null) ? success(value) : failureSupplier.get();
    }

    static <Value> Outcome<Value> runCatching(Supplier<? extends Value> valueSupplier,
            Function<? super Exception, Failure<Value>> failureMethod) {
        return tryCatch(
                () -> success(valueSupplier.get()),
                failureMethod
        );
    }

    static <Value> Outcome<Value> runCatching(Supplier<? extends Value> valueSupplier) {
        return runCatching(valueSupplier,
                (Exception exception) -> failure("Exception in runCatching").with(exception));
    }

    static <Value> Outcome<Value> runCatchingOutcome(Supplier<? extends Outcome<Value>> outcomeSupplier,
            Function<? super Exception, Failure<Value>> failureMethod) {
        return tryCatch(
                outcomeSupplier,
                failureMethod
        );
    }

    static <Value> Outcome<Value> runCatchingOutcome(Supplier<? extends Outcome<Value>> outcomeSupplier) {
        return runCatchingOutcome(outcomeSupplier,
                (Exception exception) -> failure("Exception in runCatchingOutcome").with(exception));
    }

    private static <Value> Outcome<Value> tryCatch(Supplier<? extends Outcome<Value>> successSupplier,
            Function<? super Exception, Failure<Value>> failureFunction) {
        try {
            return successSupplier.get();
        } catch (Exception exception) {
            if (Helper.EXCEPTIONS_ILLEGAL_TO_CATCH.contains(exception.getClass())) {
                throw exception;
            }
            return failureFunction.apply(exception);
        }
    }

    default <Return> Outcome<Return> map(Function<? super Value, ? extends Return> transformer) {
        return switch (this) {
            case Outcome.Failure<Value> failure -> failure.coerce();
            case Success<Value>(var value) -> success(transformer.apply(value));
        };
    }

    default <Return> Outcome<Return> mapCatching(Function<? super Value, ? extends Return> transformer) {
        return switch (this) {
            case Outcome.Failure<Value> failure -> failure.coerce();
            case Success<Value>(var value) -> tryCatch(
                    () -> success(transformer.apply(value)),
                    (Exception exception) -> failure("Exception in mapCatching").with(exception)
            );
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
            case Success<Value>(var value) -> Outcome.tryCatch(
                    () -> transformer.apply(value),
                    (Exception exception) -> failure("Exception in flatMapCatching").with(exception)
            );
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
            case Outcome.Failure<Value> failure -> success(recover.apply(failure));
            case Outcome.Success<Value> success -> this;
        };
    }

    default Outcome<Value> recoverCatching(
            Function<? super Failure<Value>, ? extends Value> recover) {
        return switch (this) {
            case Outcome.Failure<Value> failure -> tryCatch(
                    () -> success(recover.apply(failure)),
                    (Exception exception) -> failure("Exception in recoverCatching").with(exception)
            );
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
        @Contract(pure = true)
        @Override
        public String message() {
            return this.message;
        }

        @Contract(pure = true)
        public <NewValue> Failure<NewValue> with(@Nullable String message) {
            return new Failure<>(message, this.exception, this.outcome);
        }

        @Contract(pure = true)
        public <NewValue> Failure<NewValue> with(@Nullable Exception exception) {
            return (exception == null)
                    ? new Failure<>(this.message, null, this.outcome)
                    : new Failure<>(
                            Outcome.orElse(this.message, exception.getMessage()),
                            exception,
                            this.outcome
                    );
        }

        @Contract(pure = true)
        public <NewValue> Failure<NewValue> with(@Nullable Outcome<?> outcome) {
            return new Failure<>(this.message, this.exception, outcome);
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
}