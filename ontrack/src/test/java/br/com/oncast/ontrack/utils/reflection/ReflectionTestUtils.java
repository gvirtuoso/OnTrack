package br.com.oncast.ontrack.utils.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionTestUtils {

	@SuppressWarnings("unchecked")
	public static <T> T set(final Object subject, final String fieldName, final Object value) {
		try {
			final Field field = subject.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(subject, value);
			field.setAccessible(false);
			return (T) subject;
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T callPrivateMethod(final Object instance, final String methodName, final Object... args) {
		try {
			final Method method = instance.getClass().getDeclaredMethod(methodName, getClassesFrom(args));
			method.setAccessible(true);
			return (T) method.invoke(instance, args);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Class<?>[] getClassesFrom(final Object[] args) {
		if (args.length == 0) return null;

		final Class<?>[] classes = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			classes[i] = args[i].getClass();
		}

		return classes;
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(final Object instance, final String fieldName) {
		try {
			final Field field = instance.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(instance);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getStatic(final Class<?> clazz, final String fieldName) {
		try {
			final Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(null);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
