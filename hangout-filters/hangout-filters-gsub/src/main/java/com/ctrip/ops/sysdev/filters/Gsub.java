package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import com.ctrip.ops.sysdev.render.TemplateRender;
import org.apache.log4j.Logger;
import scala.Tuple4;

@SuppressWarnings("ALL")
public class Gsub extends BaseFilter {
    private static final Logger logger = Logger.getLogger(Gsub.class.getName());

    public Gsub(Map config) {
        super(config);
    }

    private Map<String, List<String>> fields;
    private List<Tuple4> f;


    protected void prepare() {
        this.fields = (Map<String, List<String>>) config.get("fields");
        f = new ArrayList(this.fields.size());
        Iterator<Entry<String, List<String>>> it = fields.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();

            String field = entry.getKey();
            String regex = entry.getValue().get(0);
            String replacement = entry.getValue().get(1);

            TemplateRender templateRender = null;
            try {
                templateRender = TemplateRender.getRender(field, false);
            } catch (Exception e) {
                logger.error("could not render template: " + field);
                System.exit(1);
            }
            f.add(new Tuple4(FieldSetter.getFieldSetter(field), templateRender, regex, replacement));

        }
    }

    @Override
    protected Map filter(final Map event) {
        for (Tuple4 t4 : f) {
            FieldSetter fs = (FieldSetter) t4._1();
            TemplateRender templateRender = (TemplateRender) t4._2();
            fs.setField(event, templateRender.render(event).toString().replaceAll((String) t4._3(), (String) t4._4()));
        }
        return event;
    }
}
