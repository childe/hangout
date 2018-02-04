package com.ctrip.ops.sysdev.filters;

import java.util.Map;

public class FloatConverter implements ConverterI {
    private boolean allow_infinity;

    public FloatConverter(Map config) {
        if (config.containsKey("allow_infinity")) {
            this.allow_infinity = (boolean) (config.get("allow_infinity"));
        } else {
            this.allow_infinity = true;
        }
    }

    public Object convert(Object from) {
        Float f;
        if (from instanceof Number) {
            f = ((Number) from).floatValue();
        } else if (from instanceof Boolean) {
            f = (Boolean) from ? 1f : 0;
        } else if (from instanceof Enum) {
            f = (float) ((Enum<?>) from).ordinal();
        } else {
            f = Float.valueOf(from.toString());
        }
        if (f.isInfinite() && !this.allow_infinity) {
            return null;
        }
        return f;
    }
}
