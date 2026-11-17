package com.mixi.manager;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Storage remains whole seconds; changing display units never writes it. */
public final class StandbyTimeValue {
    public static String display(int seconds, boolean minutes) {
        BigDecimal value = BigDecimal.valueOf(seconds);
        if (minutes) value = value.divide(BigDecimal.valueOf(60), 6, RoundingMode.HALF_UP);
        return value.stripTrailingZeros().toPlainString();
    }

    public static Integer parse(String text, boolean minutes, int maximum) {
        if (text == null || text.length() > 16 || !text.matches("[0-9]+(?:[.,][0-9]+)?")) return null;
        try {
            BigDecimal seconds = new BigDecimal(text.replace(',', '.'));
            if (minutes) seconds = seconds.multiply(BigDecimal.valueOf(60));
            if (seconds.signum() < 0 || seconds.compareTo(BigDecimal.valueOf(maximum)) > 0) return null;
            int result = seconds.setScale(0, minutes ? RoundingMode.HALF_UP : RoundingMode.UNNECESSARY).intValueExact();
            return result == 0 || result >= 10 ? result : null;
        } catch (ArithmeticException | NumberFormatException invalid) {
            return null;
        }
    }
}
