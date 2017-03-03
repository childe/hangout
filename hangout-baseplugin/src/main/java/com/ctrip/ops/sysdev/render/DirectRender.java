package com.ctrip.ops.sysdev.render;

import java.util.Map;

public class DirectRender implements TemplateRender {
    private Object value;

    public DirectRender(Object value) {
        this.value = value;
    }

    public Object render(Map event) {
        return this.value;
    }
}
