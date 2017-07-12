package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j2;
import scala.Tuple2;

@SuppressWarnings("ALL")
@Log4j2
public class Lowercase extends BaseFilter {


    public Lowercase(Map config) {
        super(config);
    }

    private ArrayList<Tuple2> fields;

    protected void prepare() {
        this.fields = new ArrayList();
        for (String field : (ArrayList<String>) config.get("fields")) {
            try {
                this.fields.add(new Tuple2(FieldSetter.getFieldSetter(field), TemplateRender.getRender(field, false)));
            } catch (IOException e) {
                log.error("could NOT build TemplateRender from " + field);
                System.exit(1);
            }
        }
    }

    @Override
    protected Map filter(Map event) {
        for (Tuple2 t2 : fields) {
            Object input = ((TemplateRender) t2._2()).render(event);
            if (input != null && String.class.isAssignableFrom(input.getClass())) {
                ((FieldSetter) t2._1()).setField(event, ((String) input).toLowerCase());
            }
        }

        return event;
    }
}
