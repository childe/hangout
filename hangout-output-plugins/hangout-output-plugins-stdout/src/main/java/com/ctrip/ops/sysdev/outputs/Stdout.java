package com.ctrip.ops.sysdev.outputs;

import java.util.Map;

import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Stdout extends BaseOutput {

    public Stdout(Map config) {
        super(config);
    }

    @Override
    protected void prepare() {
    }

    @Override
    protected void emit(Map event) {
        System.out.println(event);
    }
}

