package com.ctrip.ops.sysdev.converter;

public class FloatConverter implements Converter {

	public Object convert(Object from) {
		if (from instanceof Number) {
			return ((Number) from).floatValue();
		} else if (from instanceof Boolean) {
			return ((Boolean) from).booleanValue() ? Float.valueOf(1) : 0;
		} else if (from instanceof Enum) {
			return Float.valueOf(((Enum<?>) from).ordinal());
		} else {
			return Float.valueOf(from.toString());
		}
	}
}
