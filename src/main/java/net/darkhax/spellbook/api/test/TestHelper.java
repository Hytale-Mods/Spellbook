package net.darkhax.spellbook.api.test;

import com.hypixel.hytale.logger.HytaleLogger;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class TestHelper {

    public static void runTests(Object testObj) {
        final HytaleLogger testLogger = HytaleLogger.get(testObj.getClass().getSimpleName());
        final long start = System.nanoTime();
        final AtomicInteger total = new AtomicInteger(0);
        final AtomicInteger pass = new AtomicInteger(0);
        final AtomicInteger fail = new AtomicInteger(0);
        for (Method method : testObj.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Test.class)) {
                method.setAccessible(true);
                final String testName = testObj.getClass().getSimpleName() + "#" + method.getName();
                total.addAndGet(1);
                try {
                    method.invoke(testObj);
                    testLogger.atFine().log("Test %s passed!", testName);
                    pass.addAndGet(1);
                }
                catch (Exception e) {
                    testLogger.atSevere().withCause(e).log("Test %s failed!", testName);
                    fail.addAndGet(1);
                }
            }
        }
        final long time = System.nanoTime() - start;
        testLogger.atInfo().log("Ran %d test(s). %d/%d passed! %d/%d failed! Took %f ms.", total.get(), pass.get(), total.get(), fail.get(), total.get(), time / 1000000f);

    }
}