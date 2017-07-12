package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Log4j2
public class Date extends BaseFilter {
    private TemplateRender templateRender;
    private FieldSetter fiedlSetter;
    private boolean addYear;
    private List<DateParser> parsers;

    public Date(Map config) {
        super(config);
    }

    @SuppressWarnings("unchecked")
    protected void prepare() {
        String src = "logtime";
        if (config.containsKey("src")) {
            src = (String) config.get("src");
        }
        try {
            this.templateRender = TemplateRender.getRender(src, false);
        } catch (IOException e) {
            log.error("could NOT build TemplateRender from " + src);
            System.exit(1);
        }

        String target = "@timestamp";
        if (config.containsKey("target")) {
            target = (String) config.get("target");
        }
        this.fiedlSetter = FieldSetter.getFieldSetter(target);

        if (config.containsKey("tag_on_failure")) {
            this.tagOnFailure = (String) config.get("tag_on_failure");
        } else {
            this.tagOnFailure = "datefail";
        }

        if (config.containsKey("add_year")) {
            this.addYear = (Boolean) config.get("add_year");
        } else {
            this.addYear = false;
        }
        this.parsers = new ArrayList<DateParser>();
        for (String format : (List<String>) config.get("formats")) {
            if (format.equalsIgnoreCase("ISO8601")) {
                parsers.add(new ISODateParser((String) config.get("timezone")));
            } else if (format.equalsIgnoreCase("UNIX")) {
                parsers.add(new UnixParser());
            } else if (format.equalsIgnoreCase("UNIX_MS")) {
                parsers.add(new UnixMSParser());
            } else {
                parsers.add(new FormatParser(format, (String) config
                        .get("timezone"), (String) config.get("locale")));
            }
        }
    }

    @Override
    protected Map filter(Map event) {
        Object inputObj = this.templateRender.render(event);
        if (inputObj == null) {
            return event;
        }

        String input = inputObj.toString();
        boolean success = false;

        if (addYear) {
            input = Calendar.getInstance().get(Calendar.YEAR) + input;
        }

        for (DateParser parser : this.parsers) {
            try {
                this.fiedlSetter.setField(event, parser.parse(input));
                success = true;
                break;
            } catch (Exception e) {
                log.trace(String.format("parser %s tailed, try next", parser.toString()));
            }
        }

        postProcess(event, success);

        return event;
    }

}
