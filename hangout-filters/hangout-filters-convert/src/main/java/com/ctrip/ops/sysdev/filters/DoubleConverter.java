package com.ctrip.ops.sysdev.filters;

import java.util.Map;

public class DoubleConverter implements ConverterI {
    private boolean allow_infinity;

    public DoubleConverter(Map config) {
        if (config.containsKey("allow_infinity")) {
            this.allow_infinity = (boolean) (config.get("allow_infinity"));
        } else {
            this.allow_infinity = true;
        }
    }

    public Object convert(Object from) {
        Double d;
        if (from instanceof Number) {
            d = ((Number) from).doubleValue();
        } else if (from instanceof Boolean) {
            d = (Boolean) from ? 1d : 0;
        } else {
            d = Double.valueOf(from.toString().trim());
        }
        if (d.isInfinite() && !this.allow_infinity) {
            return null;
        }
        return d;
    }
}
