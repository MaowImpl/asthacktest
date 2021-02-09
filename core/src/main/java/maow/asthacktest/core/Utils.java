package maow.asthacktest.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Utils {
    private Utils() {}

    // Reflection to test the existence of the new method.
    public static <T> void invoke(Class<T> clazz, String name) {
        try {
            final T obj = clazz.newInstance();
            final Method method = clazz.getMethod(name);
            method.invoke(obj);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}
