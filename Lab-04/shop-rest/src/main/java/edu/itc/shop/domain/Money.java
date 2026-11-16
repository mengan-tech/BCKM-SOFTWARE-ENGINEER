package edu.itc.shop.domain;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Money(BigDecimal amount, String currency) {
    private static final Pattern FORMAT = Pattern.compile(
            "([+-]?(?:\\d+(?:\\.\\d+)?|\\.\\d+))\\s+([A-Z]{3})");

    public static Money parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Invalid Money value");
        }

        Matcher matcher = FORMAT.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid Money value");
        }

        try {
            return new Money(new BigDecimal(matcher.group(1)), matcher.group(2));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid Money value", ex);
        }
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency;
    }
}
