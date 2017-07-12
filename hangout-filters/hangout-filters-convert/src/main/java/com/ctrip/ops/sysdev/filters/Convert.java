package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j2;
import scala.Tuple4;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

@Log4j2
public class Convert extends BaseFilter {
    private Map<FieldSetter, Tuple4> f;

    public Convert(Map config) {
        super(config);
    }

    protected void prepare() {

        if (config.containsKey("tag_on_failure")) {
            this.tagOnFailure = (String) config.get("tag_on_failure");
        } else {
            this.tagOnFailure = "convertfail";
        }

        f = new HashMap();
        Map<String, Map> fields = (Map<String, Map>) config.get("fields");
        Iterator<Entry<String, Map>> it = fields.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Map> entry = it.next();
            String field = entry.getKey();
            Map value = entry.getValue();
            ConverterI converter = null;
            Boolean remove_if_fail = false;
            Object setto_if_fail = null;
            if (((String) value.get("to")).equalsIgnoreCase("long")) {
                converter = new LongConverter();
            } else if (((String) value.get("to")).equalsIgnoreCase("integer")) {
                converter = new IntegerConverter();
            } else if (((String) value.get("to")).equalsIgnoreCase("double")) {
                converter = new DoubleConverter();
            } else if (((String) value.get("to")).equalsIgnoreCase("float")) {
                converter = new FloatConverter();
            } else if (((String) value.get("to")).equalsIgnoreCase("string")) {
                converter = new StringConverter();
            } else if (((String) value.get("to")).equalsIgnoreCase("boolean")) {
                converter = new BooleanConverter();
            }

            if (value.containsKey("remove_if_fail")) {
                remove_if_fail = (Boolean) value.get("remove_if_fail");
            }

            if (value.containsKey("setto_if_fail")) {
                setto_if_fail = value.get("setto_if_fail");
            }

            try {
                f.put(FieldSetter.getFieldSetter(field), new Tuple4(converter, TemplateRender.getRender(field, false), remove_if_fail, setto_if_fail));
            } catch (Exception e) {
                log.error("could not create filed render for '" + field + "'");
                System.exit(1);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Map filter(final Map event) {
        Iterator<Entry<FieldSetter, Tuple4>> it = f.entrySet().iterator();
        boolean success = true;
        while (it.hasNext()) {
            Map.Entry<FieldSetter, Tuple4> entry = it.next();
            FieldSetter fieldSetter = entry.getKey();
            Tuple4 t4 = entry.getValue();
            try {
                TemplateRender tr = (TemplateRender) t4._2();
                fieldSetter.setField(event, ((ConverterI) t4._1()).convert(tr.render(event)));
            } catch (Exception e) {
                success = false;
                if ((Boolean) t4._3()) {
                    fieldSetter.setField(event, null);
                } else {
                    if (t4._4() != null) {
                        fieldSetter.setField(event, t4._4());
                    }
                }
            }
        }

        this.postProcess(event, success);

        return event;
    }
}
