package com.badminimum.utilities.datasize;

import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.math.BigInteger;

@SuppressWarnings("unused")
@NullMarked
public enum DataUnit {
    KIBIBYTE("KiB", Base.BINARY.pow(1)),
    KILOBYTE("KB", Base.DECIMAL.pow(1)),
    MEBIBYTE("MiB", Base.BINARY.pow(2)),
    MEGABYTE("MB", Base.DECIMAL.pow(2)),
    GIBIBYTE("GiB", Base.BINARY.pow(3)),
    GIGABYTE("GB", Base.DECIMAL.pow(3)),
    TEBIBYTE("TiB", Base.BINARY.pow(4)),
    TERABYTE("TB", Base.DECIMAL.pow(4)),
    PEBIBYTE("PiB", Base.BINARY.pow(5)),
    PETABYTE("PB", Base.DECIMAL.pow(5)),
    EXBIBYTE("EiB", Base.BINARY.pow(6)),
    EXABYTE("EB", Base.DECIMAL.pow(6)),
    ZEBIBYTE("ZiB", Base.BINARY.pow(7)),
    ZETTABYTE("ZB", Base.DECIMAL.pow(7)),
    YOBIBYTE("YiB", Base.BINARY.pow(8)),
    YOTTABYTE("YB", Base.DECIMAL.pow(8));

    private final String abbreviation;
    private final BigInteger bytesInt;
    private final BigDecimal bytesDec;

    DataUnit(String abbreviation, BigInteger bytesInt) {
        this.abbreviation = abbreviation;
        this.bytesInt = bytesInt;
        this.bytesDec = new BigDecimal(this.bytesInt);
    }

    public String abbreviation() {
        return this.abbreviation;
    }

    public BigInteger bytes() {
        return this.bytesInt;
    }

    public BigDecimal bytesDecimal() {
        return this.bytesDec;
    }

    public DataSize of(long amount) {
        return DataSize.of(amount, this);
    }

    public DataSize of(BigInteger amount) {
        return DataSize.of(amount, this);
    }

    public DataSize of(double amount) {
        return DataSize.of(amount, this);
    }

    public DataSize of(BigDecimal amount) {
        return DataSize.of(amount, this);
    }

    private enum Base {
        BINARY(1024),
        DECIMAL(1000);

        private final BigInteger base;

        Base(int base) {
            this.base = BigInteger.valueOf(base);
        }

        public BigInteger pow(int exponent) {
            return this.base.pow(exponent);
        }
    }
}