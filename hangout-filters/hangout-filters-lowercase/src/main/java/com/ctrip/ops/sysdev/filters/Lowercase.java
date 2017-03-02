package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import com.ctrip.ops.sysdev.render.TemplateRender;
import org.apache.log4j.Logger;
import scala.Tuple2;

@SuppressWarnings("ALL")
public class Lowercase extends BaseFilter {
    private static final Logger logger = Logger.getLogger(Lowercase.class.getName());

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
                logger.error("could NOT build TemplateRender from " + field);
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
