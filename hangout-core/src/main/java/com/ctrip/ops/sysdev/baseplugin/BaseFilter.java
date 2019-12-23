package com.ctrip.ops.sysdev.baseplugin;


import com.ctrip.ops.sysdev.fieldDeleter.FieldDeleter;
import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import com.ctrip.ops.sysdev.render.FreeMarkerRender;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.*;

@Log4j2
public class BaseFilter extends Base {

    protected Map config;
    protected String tagOnFailure;
    protected List<FieldDeleter> removeFields;
    protected Map<FieldSetter, TemplateRender> addFields;
    private List<TemplateRender> IF;
    public boolean processExtraEventsFunc;
    public BaseFilter nextFilter;
    public List<BaseOutput> outputs;

    public BaseFilter(Map config) {
        super(config);
        this.config = config;

        this.nextFilter = null;
        this.outputs = new ArrayList<BaseOutput>();

        final List<String> ifConditions = (List<String>) this.config.get("if");
        if (ifConditions != null) {
            IF = new ArrayList<TemplateRender>(ifConditions.size());
            for (String c : ifConditions) {
                try {
                    IF.add(new FreeMarkerRender(c, c));
                } catch (IOException e) {
                    log.fatal(e.getMessage(),e);
                    System.exit(1);
                }
            }
        }

        this.tagOnFailure = (String) config.get("tag_on_failure");

        final List<String> remove_fields = (ArrayList<String>) config.get("remove_fields");
        if (remove_fields != null) {
            this.removeFields = new ArrayList<>(remove_fields.size());
            for (String field : remove_fields) {
                this.removeFields.add(FieldDeleter.getFieldDeleter(field));
            }
        }

        final Map<String, String> add_fields = (Map<String, String>) config.get("add_fields");
        if (add_fields != null) {
            this.addFields = new HashMap<>(add_fields.size());
            final Iterator<Map.Entry<String, String>> it = add_fields.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();

                String field = entry.getKey();
                Object value = entry.getValue();

                try {
                    this.addFields.put(FieldSetter.getFieldSetter(field), TemplateRender.getRender(value));
                } catch (IOException e) {
                    log.fatal(e.getMessage());
                    System.exit(1);
                }
            }
        }

        this.prepare();
    }

    protected void prepare() {
    }

    public boolean needProcess(Map event) {
        if (this.IF != null) {
            for (TemplateRender render : this.IF) {
                if (!render.render(event).equals("true")) {
                    return false;
                }
            }
        }
        return true;
    }

    public Map process(Map event) {
        if (event == null) {
            return null;
        }

        if (this.needProcess(event) == true) {
            event = this.filter(event);
        }

        if (event == null) {
            return null;
        }

        if (this.nextFilter != null) {
            event = this.nextFilter.process(event);
        } else {
            for (BaseOutput o : this.outputs) {
                o.process(event);
            }
        }
        return event;
    }

    public void processExtraEvents(Stack<Map<String, Object>> to_st) {
        this.filterExtraEvents(to_st);
    }

    protected Map filter(Map event) {
        return event;
    }

    protected void filterExtraEvents(Stack<Map<String, Object>> to_stt) {
        return;
    }

    public void postProcess(Map event, boolean ifSuccess) {
        if (ifSuccess == false) {
            if (this.tagOnFailure == null || this.tagOnFailure.length() <= 0) {
                return;
            }
            if (!event.containsKey("tags")) {
                event.put("tags", new ArrayList<String>(Arrays.asList(this.tagOnFailure)));
            } else {
                Object tags = event.get("tags");
                if (tags.getClass() == ArrayList.class
                        && ((ArrayList) tags).indexOf(this.tagOnFailure) == -1) {
                    ((ArrayList) tags).add(this.tagOnFailure);
                }
            }
        } else {
            if (this.removeFields != null) {
                for (FieldDeleter f : this.removeFields) {
                    f.delete(event);
                }
            }

            if (this.addFields != null) {
                Iterator<Map.Entry<FieldSetter, TemplateRender>> it = this.addFields.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<FieldSetter, TemplateRender> entry = it.next();
                    FieldSetter fieldSetter = entry.getKey();
                    TemplateRender templateRender = entry.getValue();
                    fieldSetter.setField(event, templateRender.render(event));
                }
            }
        }
    }
}
