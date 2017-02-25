package com.ctrip.ops.sysdev.render;

import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j
public class OneLevelRender implements TemplateRender {
    private String field;

    public OneLevelRender(String field) {
        this.field = field;
    }

    public Object render(Map event) {
        return event.get(field);
    }
}
