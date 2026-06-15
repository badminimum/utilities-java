package com.badminimum.utilities.datasize;

import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

@SuppressWarnings("unused")
@NullMarked
public final class DataSize implements Comparable<DataSize> {
    private final BigInteger bytesInt;
    private final BigDecimal bytesDec;

    private DataSize(BigInteger bytesInt, BigDecimal bytesDec) {
        this.bytesInt = bytesInt;
        this.bytesDec = bytesDec;
    }

    public static DataSize of(long amount, DataUnit unit) {
        return of(BigInteger.valueOf(amount), unit);
    }

    public static DataSize of(BigInteger amount, DataUnit unit) {
        final var bytes = amount.multiply(unit.bytes());
        return new DataSize(bytes, new BigDecimal(bytes));
    }

    public static DataSize of(double amount, DataUnit unit) {
        return of(BigDecimal.valueOf(amount), unit);
    }

    public static DataSize of(BigDecimal amount, DataUnit unit) {
        final var unitBytesDec = unit.bytesDecimal();
        final var bytesDec = amount.multiply(unitBytesDec);
        return new DataSize(bytesDec.toBigInteger(), bytesDec);
    }

    public BigInteger bytes() {
        return this.bytesInt;
    }

    public BigDecimal bytesDecimal() {
        return this.bytesDec;
    }

    public double inUnit(DataUnit unit) {
        final var unitBytesDec = unit.bytesDecimal();
        return this.bytesDec.divide(unitBytesDec, MathContext.DECIMAL128).doubleValue();
    }

    public String format(DataUnit unit, Format format) {
        final var unitBytesDec = unit.bytesDecimal();
        final var decimals = format.decimals();

        final var valueInUnit = this.bytesDec.divide(unitBytesDec, decimals, RoundingMode.HALF_UP);

        var plainString = valueInUnit.toPlainString();

        final var delimiter = format.delimiter();
        if (delimiter != '.') {
            plainString = plainString.replace('.', delimiter);
        }

        final var abbr = unit.abbreviation();
        return switch (format.unitPosition()) {
            case START -> abbr + " " + plainString;
            case END -> plainString + " " + abbr;
        };
    }

    public DataSize plus(DataSize other) {
        final var newBytesInt = this.bytesInt.add(other.bytesInt);
        final var newBytesDec = new BigDecimal(newBytesInt);
        return new DataSize(newBytesInt, newBytesDec);
    }

    public DataSize minus(DataSize other) {
        final var newBytesInt = this.bytesInt.subtract(other.bytesInt);
        final var newBytesDec = new BigDecimal(newBytesInt);
        return new DataSize(newBytesInt, newBytesDec);
    }

    public DataSize multiply(DataSize other) {
        final var newBytesInt = this.bytesInt.multiply(other.bytesInt);
        final var newBytesDec = new BigDecimal(newBytesInt);
        return new DataSize(newBytesInt, newBytesDec);
    }

    public DataSize divide(DataSize other) {
        final var newBytesInt = this.bytesInt.divide(other.bytesInt);
        final var newBytesDec = new BigDecimal(newBytesInt);
        return new DataSize(newBytesInt, newBytesDec);
    }

    @Override
    public int compareTo(DataSize other) {
        return this.bytesInt.compareTo(other.bytesInt);
    }

    @Override
    public int hashCode() {
        return this.bytesInt.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof DataSize other) && this.bytesInt.equals(other.bytesInt);
    }
}