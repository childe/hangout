package com.ctrip.ops.sysdev.render;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class OneLevelRender implements TemplateRender {
    private String field;

    public OneLevelRender(String field) {
        this.field = field;
    }

    public Object render(Map event) {
        return event.get(field);
    }
}
