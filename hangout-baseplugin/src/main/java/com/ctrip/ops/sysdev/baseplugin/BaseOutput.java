package com.ctrip.ops.sysdev.baseplugin;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ctrip.ops.sysdev.render.FreeMarkerRender;
import com.ctrip.ops.sysdev.render.TemplateRender;

@Log4j2
public abstract class BaseOutput extends Base {
    protected Map config;
    protected List<TemplateRender> IF;

    public BaseOutput(Map config) {
        super(config);

        this.config = config;

        if (this.config.containsKey("if")) {
            IF = new ArrayList<TemplateRender>();
            for (String c : (List<String>) this.config.get("if")) {
                try {
                    IF.add(new FreeMarkerRender(c, c));
                } catch (IOException e) {
                    log.fatal(e.getMessage());
                    System.exit(1);
                }
            }
        } else {
            IF = null;
        }

        this.prepare();
    }

    protected abstract void prepare();

    protected abstract void emit(Map event);

    public void shutdown() {
        log.info("shutdown" + this.getClass().getName());
    }

    public void process(Map event) {
        boolean ifSuccess = true;
        if (this.IF != null) {
            for (TemplateRender render : this.IF) {
                if (!render.render(event).equals("true")) {
                    ifSuccess = false;
                    break;
                }
            }
        }
        if (ifSuccess) {
            this.emit(event);
            if (this.enableMeter == true) {
                this.meter.mark();
            }
        }
    }
}
