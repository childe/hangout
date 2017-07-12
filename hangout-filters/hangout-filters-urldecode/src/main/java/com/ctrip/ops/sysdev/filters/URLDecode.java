package com.ctrip.ops.sysdev.filters;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.ArrayList;
import java.util.Map;
import java.net.URLDecoder;

import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import com.ctrip.ops.sysdev.render.TemplateRender;
import scala.Tuple2;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class URLDecode extends BaseFilter {


    @SuppressWarnings("rawtypes")
    public URLDecode(Map config) {
        super(config);
    }

    private ArrayList<Tuple2> fields;
    private String enc;

    @SuppressWarnings("unchecked")
    protected void prepare() {
        this.fields = new ArrayList<>();
        for (String field : (ArrayList<String>) config.get("fields")) {
            TemplateRender templateRender = null;
            try {
                templateRender = TemplateRender.getRender(field, false);
            } catch (IOException e) {
                log.fatal("could NOT build template render from " + field);
                System.exit(1);
            }
            this.fields.add(new Tuple2(FieldSetter.getFieldSetter(field), templateRender));
        }

        if (config.containsKey("enc")) {
            this.enc = (String) config.get("enc");
        } else {
            this.enc = "UTF-8";
        }

        if (this.config.containsKey("tag_on_failure")) {
            this.tagOnFailure = (String) this.config.get("tag_on_failure");
        } else {
            this.tagOnFailure = "URLDecodefail";
        }
    }

    @Override
    protected Map filter(final Map event) {
        boolean success = true;
        for (Tuple2 f2 : this.fields) {
            TemplateRender templateRender = (TemplateRender) f2._2();
            Object value = templateRender.render(event);
            if (value != null && String.class.isAssignableFrom(value.getClass())) {
                try {
                    ((FieldSetter) f2._1()).setField(event, URLDecoder.decode((String) value, this.enc));
                } catch (Exception e) {
                    log.error("URLDecode failed", e);
                    success = false;
                }
            }
        }

        this.postProcess(event, success);

        return event;
    }
}
