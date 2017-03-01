package com.ctrip.ops.sysdev.outputs;

import java.util.Map;

import com.codahale.metrics.Meter;
import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Logger;

@Log4j
public class Stdout extends BaseOutput {

    private Meter meter;
    public Stdout(Map config) {
        super(config);
    }

    @Override
    protected void prepare() {
        this.meter = watcher.setMetric(this.getClass().getSimpleName());
    }

    @Override
    protected void emit(Map event) {
        System.out.println(event);
        this.meter.mark();
    }
}

