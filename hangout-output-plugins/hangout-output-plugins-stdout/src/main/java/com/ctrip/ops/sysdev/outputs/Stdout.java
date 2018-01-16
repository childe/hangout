package com.ctrip.ops.sysdev.outputs;

import java.io.IOException;
import java.util.Map;

import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONValue;

@Log4j2
public class Stdout extends BaseOutput {

    private TemplateRender format;

    public Stdout(Map config) {
        super(config);
    }

    @Override
    protected void prepare() {
        if (this.config.containsKey("format")) {
            String format = (String) this.config.get("format");
            try {
                this.format = TemplateRender.getRender(format);
            } catch (IOException e) {
                log.fatal("could not build template from" + format);
                System.exit(1);
            }
        } else {
            this.format = null;
        }
    }

    @Override
    protected void emit(Map event) {
        if (this.format == null) {
            System.out.println(event);
        } else {
            Object message = this.format.render(event);
            if (message != null) {
                System.out.println(message);
            }
        }

    }
}

