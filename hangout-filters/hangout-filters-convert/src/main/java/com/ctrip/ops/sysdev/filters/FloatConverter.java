package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.converter.Converter;

public class FloatConverter implements Converter {

	public Object convert(Object from) {
		if (from instanceof Number) {
			return ((Number) from).floatValue();
		} else if (from instanceof Boolean) {
			return (Boolean) from ? 1f : 0;
		} else if (from instanceof Enum) {
			return (float) ((Enum<?>) from).ordinal();
		} else {
			return Float.valueOf(from.toString());
		}
	}
}
