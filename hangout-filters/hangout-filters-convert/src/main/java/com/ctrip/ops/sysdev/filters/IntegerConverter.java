package com.ctrip.ops.sysdev.filters;

public class IntegerConverter implements ConverterI {

	public Object convert(Object from) {
		if (from instanceof Number) {
			return ((Number) from).intValue();
		} else if (from instanceof Boolean) {
			return (Boolean) from ? 1 : 0;
		} else if (from instanceof Enum) {
			return ((Enum<?>) from).ordinal();
		} else {
			return Integer.valueOf(from.toString().trim());
		}
	}
}
