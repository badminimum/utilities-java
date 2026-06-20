package com.badminimum.utilities.outcome;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.stream.Stream;

import static com.badminimum.utilities.outcome.internal.Helper.tryCatch;

@SuppressWarnings("unused")
@NullMarked
public sealed interface Outcome<Value>
        extends OutcomeExtractable<Value>
        permits FailureOutcome, SuccessOutcome {
    // =========================================
    // Factory & Creation Methods
    // =========================================
    @Contract(pure = true)
    static SuccessOutcome<Empty> empty() {
        return new SuccessOutcome<>(Empty.INSTANCE);
    }

    @Contract(pure = true)
    static <Value> SuccessOutcome<Value> success(Value value) {
        return new SuccessOutcome<>(value);
    }

    @Contract(pure = true)
    static SuccessOutcome<Empty> success() {
        return empty();
    }

    @Contract(pure = true)
    static <Value> FailureOutcome<Value> failure(@Nullable String message) {
        return new FailureOutcome<>(message);
    }

    @Contract(pure = true)
    static <Value> FailureOutcome<Value> failure(@Nullable Exception exception) {
        return new FailureOutcome<>(exception);
    }

    @Contract(pure = true)
    static <Value> FailureOutcome<Value> failure(@Nullable Outcome<?> outcome) {
        return new FailureOutcome<>(outcome);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Contract(pure = true)
    static <Value> Outcome<Value> fromOptional(
            Optional<? extends Value> optional,
            Supplier<FailureOutcome<Value>> failureSupplier
    ) {
        return optional.<Outcome<Value>>map(Outcome::success).orElseGet(failureSupplier);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Contract(pure = true)
    static <Value> Outcome<Value> fromOptional(
            Optional<? extends Value> optional
    ) {
        return optional.<Outcome<Value>>map(Outcome::success).orElseGet(() -> failure("Optional value was empty"));
    }

    @Contract(pure = true)
    static <Value> Outcome<Value> ofNullable(
            @Nullable Value value,
            Supplier<FailureOutcome<Value>> failureSupplier
    ) {
        return (value != null) ? success(value) : failureSupplier.get();
    }

    // =========================================
    // Transformation & Mapping
    // =========================================
    default <Return> Outcome<Return> map(Function<? super Value, ? extends Return> transformer) {
        return switch (this) {
            case FailureOutcome<Value> failure -> failure.coerce();
            case SuccessOutcome<Value>(var value, var ignored) -> success(transformer.apply(value));
        };
    }

    default <Return> Outcome<Return> mapCatching(Function<? super Value, ? extends Return> transformer) {
        return switch (this) {
            case FailureOutcome<Value> failure -> failure.coerce();
            case SuccessOutcome<Value>(var value, var ignored) -> tryCatch(
                    () -> success(transformer.apply(value)),
                    (var exception) -> failure("Exception in mapCatching").with(exception)
            );
        };
    }

    default Outcome<Value> mapFailure(Function<? super FailureOutcome<Value>, FailureOutcome<Value>> transformer) {
        return switch (this) {
            case FailureOutcome<Value> failure -> transformer.apply(failure);
            case SuccessOutcome<Value> v -> this;
        };
    }

    default <Return> Outcome<Return> flatMap(Function<? super Value, ? extends Outcome<Return>> transformer) {
        return switch (this) {
            case FailureOutcome<Value> failure -> failure.coerce();
            case SuccessOutcome<Value>(var value, var ignored) -> transformer.apply(value);
        };
    }

    default <Return> Outcome<Return> flatMapCatching(Function<? super Value, ? extends Outcome<Return>> transformer) {
        return switch (this) {
            case FailureOutcome<Value> failure -> failure.coerce();
            case SuccessOutcome<Value>(var value, var ignored) -> tryCatch(
                    () -> transformer.apply(value),
                    (var exception) -> failure("Exception in flatMapCatching").with(exception)
            );
        };
    }

    @SuppressWarnings("unchecked")
    default Outcome<Value> flatten() {
        return switch (this) {
            case FailureOutcome<Value> failure -> failure.coerce();
            case SuccessOutcome<Value>(var value, var ignored) ->
                    (value instanceof Outcome<?> inner) ? (Outcome<Value>) inner : this;
        };
    }

    default Outcome<Empty> ignoreElement() {
        return this.map((var ignored) -> Empty.INSTANCE);
    }

    // =========================================
    // Recovery & Fallbacks
    // =========================================
    default Outcome<Value> recover(
            Function<? super FailureOutcome<Value>, ? extends Value> recover) {
        return switch (this) {
            case FailureOutcome<Value> failure -> success(recover.apply(failure));
            case SuccessOutcome<Value> success -> this;
        };
    }

    default Outcome<Value> recoverWith(
            Function<? super FailureOutcome<Value>, ? extends Outcome<Value>> fallbackTransformer) {
        return switch (this) {
            case FailureOutcome<Value> failure -> fallbackTransformer.apply(failure);
            case SuccessOutcome<Value> success -> this;
        };
    }

    default Outcome<Value> recoverCatching(
            Function<? super FailureOutcome<Value>, ? extends Value> recover) {
        return switch (this) {
            case FailureOutcome<Value> failure -> tryCatch(
                    () -> success(recover.apply(failure)),
                    (var exception) -> failure("Exception in recoverCatching").with(exception)
            );
            case SuccessOutcome<Value> success -> this;
        };
    }

    default Outcome<Value> recoverWithCatching(
            Function<? super FailureOutcome<Value>, ? extends Outcome<Value>> fallbackTransformer) {
        return switch (this) {
            case FailureOutcome<Value> failure -> tryCatch(
                    () -> fallbackTransformer.apply(failure),
                    (var exception) -> failure("Exception in recoverCatching").with(exception)
            );
            case SuccessOutcome<Value> success -> this;
        };
    }

    default Outcome<Value> or(Supplier<? extends Outcome<Value>> fallbackSupplier) {
        return switch (this) {
            case FailureOutcome<Value> ignored -> fallbackSupplier.get();
            case SuccessOutcome<Value> ignored -> this;
        };
    }

    // =========================================
    // Combination & Filtering
    // =========================================
    default Outcome<Value> filter(
            Predicate<? super Value> predicate,
            Function<? super Value, FailureOutcome<Value>> failureProvider
    ) {
        return switch (this) {
            case FailureOutcome<Value> failure -> failure.coerce();
            case SuccessOutcome<Value>(var value, var ignored) ->
                    predicate.test(value) ? this : failureProvider.apply(value);
        };
    }

    default Outcome<Value> filter(
            Predicate<? super Value> predicate,
            String failureMessage
    ) {
        return this.filter(predicate, (var ignored) -> failure(failureMessage));
    }

    default <Other, Combined> Outcome<Combined> combine(
            Outcome<Other> other,
            BiFunction<? super Value, ? super Other, ? extends Combined> combineFunction
    ) {
        return this.flatMap((var val1) -> other.map((var val2) -> combineFunction.apply(val1, val2)));
    }

    default <Other, Combined> Outcome<Combined> combineCatching(
            Outcome<Other> other,
            BiFunction<? super Value, ? super Other, ? extends Combined> combineFunction
    ) {
        return this.flatMapCatching((var val1) -> other.mapCatching((var val2) ->
                combineFunction.apply(val1, val2)));
    }

    // =========================================
    // Terminal Operations & Execution
    // =========================================
    default <Return> Return fold(
            Function<? super Value, ? extends Return> onSuccess,
            Function<? super FailureOutcome<Value>, ? extends Return> onFailure
    ) {
        return switch (this) {
            case FailureOutcome<Value> failure -> onFailure.apply(failure);
            case SuccessOutcome<Value>(var value, var ignored) -> onSuccess.apply(value);
        };
    }

    default void consumer(
            Consumer<? super Value> onSuccess,
            Consumer<? super FailureOutcome<Value>> onFailure
    ) {
        switch (this) {
            case FailureOutcome<Value> failure -> onFailure.accept(failure);
            case SuccessOutcome<Value>(var value, var ignored) -> onSuccess.accept(value);
        }
    }

    default Outcome<Value> onSuccess(Consumer<? super Value> action) {
        if (this instanceof SuccessOutcome<Value>(var value, var ignored)) {
            action.accept(value);
        }
        return this;
    }

    default Outcome<Value> onFailure(Consumer<? super FailureOutcome<Value>> action) {
        if (this instanceof FailureOutcome<Value> failure) {
            action.accept(failure);
        }
        return this;
    }

    default Outcome<Value> peek(Consumer<? super Outcome<Value>> consumer) {
        consumer.accept(this);
        return this;
    }

    default Outcome<Value> finallyDo(Runnable action) {
        action.run();
        return this;
    }

    // =========================================
    // Type Interoperability & Conversion
    // =========================================
    default Stream<Value> stream() {
        return this.fold(Stream::of, (var failure) -> Stream.empty());
    }

    default CompletableFuture<Value> toCompletableFuture() {
        return this.fold(
                CompletableFuture::completedFuture,
                (var failure) -> CompletableFuture.failedFuture(
                        (failure.exceptionOrNull() != null)
                                ? failure.exceptionOrNull()
                                : new RuntimeException(failure.message())
                )
        );
    }

    // =========================================
    // Inspection & Status Queries
    // =========================================
    default boolean isSuccess() {
        return this instanceof SuccessOutcome;
    }

    default boolean isFailure() {
        return this instanceof FailureOutcome;
    }

    default Optional<SuccessOutcome<Value>> asSuccess() {
        return (this instanceof SuccessOutcome<Value> success) ? Optional.of(success) : Optional.empty();
    }

    default Optional<FailureOutcome<Value>> asFailure() {
        return (this instanceof FailureOutcome<Value> failure) ? Optional.of(failure) : Optional.empty();
    }

    enum Empty {
        INSTANCE
    }
}