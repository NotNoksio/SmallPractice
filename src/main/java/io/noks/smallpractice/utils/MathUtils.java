package io.noks.smallpractice.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtils {
	
	public static int limit(double actual, int min, int max) {
		return (int) Math.min(Math.max(actual, min), max);
	}
}
