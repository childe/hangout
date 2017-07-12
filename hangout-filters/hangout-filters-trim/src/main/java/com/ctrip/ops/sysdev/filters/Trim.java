package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j2;
import scala.Tuple2;

@Log4j2
public class Trim extends BaseFilter {

    public Trim(Map config) {
        super(config);
    }

    private ArrayList<Tuple2> fields;

    protected void prepare() {
        this.fields = new ArrayList();
        for (String field : (ArrayList<String>) config.get("fields")) {

            TemplateRender templateRender = null;
            try {
                templateRender = TemplateRender.getRender(field, false);
            } catch (IOException e) {
                log.fatal("could NOT build template render from " + field);
            }
            this.fields.add(new Tuple2(FieldSetter.getFieldSetter(field), templateRender));
        }
    }

    @Override
    protected Map filter(final Map event) {
        for (Tuple2 t2 : fields) {
            Object value = ((TemplateRender) t2._2()).render(event);
            if (value != null && String.class.isAssignableFrom(value.getClass())) {
                ((FieldSetter) t2._1()).setField(event, ((String) value).trim());
            }
        }
        return event;
    }
}
