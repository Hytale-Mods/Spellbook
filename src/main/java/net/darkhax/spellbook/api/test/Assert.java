package net.darkhax.spellbook.api.test;

import java.util.Arrays;
import java.util.Objects;

public class Assert {
    public static void assertEquals(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            assertEqualsError(expected.toString(), actual.toString());
        }
    }

    public static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            assertEqualsError(Integer.toString(expected), Integer.toString(actual));
        }
    }

    public static void assertEquals(long expected, long actual) {
        if (expected != actual) {
            assertEqualsError(Long.toString(expected), Long.toString(actual));
        }
    }

    public static void assertEquals(double expected, double actual, double delta) {
        if (Math.abs(expected - actual) > delta) {
            assertEqualsError(Double.toString(expected), Double.toString(actual));

        }
    }

    public static void assertEquals(boolean expected, boolean actual) {
        if (expected != actual) {
            assertEqualsError(Boolean.toString(expected), Boolean.toString(actual));
        }
    }

    public static <T> T assertType(Class<T> expected, Object actual) {
        if (!expected.isAssignableFrom(actual.getClass())) {
            assertEqualsError(expected.getCanonicalName(), actual.getClass().getCanonicalName());
        }
        return (T) actual;
    }

    public static <T> void assertEquals(T[] expected, T[] actual) {
        if (!Arrays.equals(expected, actual)) {
            assertEqualsError(Arrays.toString(expected), Arrays.toString(actual));
        }
    }

    public static void assertEqualsError(String expected, String actual) {
        throw new AssertionError("Assertion failed: expected '" + expected + "' but was '" + actual + "'");
    }
}
