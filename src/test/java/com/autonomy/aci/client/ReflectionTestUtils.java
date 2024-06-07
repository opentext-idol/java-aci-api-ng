package com.autonomy.aci.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Contains utility methods for working with reflection in unit tests.
 */
public class ReflectionTestUtils {

    /**
     * Gets the method from the class and makes it accessible.
     * @param clazz          the <code>Class</code> from which to get the method
     * @param methodName     The name of the method to get
     * @param parameterTypes The types of the method parameters
     * @return An accessible {@link java.lang.reflect.Method} that can be invoked
     * @throws NoSuchMethodException If the method doesn't exist.
     */
    public static Method getAccessibleMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) throws NoSuchMethodException {
        final Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    /**
     * Gets the field from the class and makes it accessible.
     * @param clazz     the <code>Class</code> from which to get the field
     * @param fieldName The name of the field to get
     * @return An accessible {@link java.lang.reflect.Field} that can be invoked
     * @throws NoSuchFieldException If the field doesn't exist.
     */
    public static Field getAccessibleField(final Class<?> clazz, final String fieldName) throws NoSuchFieldException {
        final Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

}
