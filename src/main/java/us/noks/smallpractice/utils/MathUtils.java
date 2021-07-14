package us.noks.smallpractice.utils;

public class MathUtils {
	
	public static int limit(double actual, int min, int max) {
		return (int) Math.min(Math.max(actual, min), max);
	}
}
