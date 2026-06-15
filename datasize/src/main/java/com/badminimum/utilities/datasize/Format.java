package com.badminimum.utilities.datasize;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@SuppressWarnings("unused")
@NullMarked
public record Format(
        int decimals,
        char delimiter,
        UnitPosition unitPosition
) {
    public Format {
        Objects.requireNonNull(unitPosition);
    }

    public Format() {
        this(2, '.', UnitPosition.END);
    }

    public Format(int decimals) {
        this(decimals, ',', UnitPosition.END);
    }

    public Format(char delimiter) {
        this(2, delimiter, UnitPosition.END);
    }

    public Format(UnitPosition unitPosition) {
        this(2, ',', unitPosition);
    }

    public Format decimals(int decimals) {
        return new Format(decimals, this.delimiter, this.unitPosition);
    }

    public Format delimiter(char delimiter) {
        return new Format(this.decimals, delimiter, this.unitPosition);
    }

    public Format unitPosition(UnitPosition unitPosition) {
        return new Format(this.decimals, this.delimiter, unitPosition);
    }

    public enum UnitPosition {
        START,
        END
    }
}
