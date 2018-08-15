package com.ctrip.ops.sysdev.baseplugin;

import java.util.Map;

public abstract class Bolt extends Base {

    public Bolt(Map config) {
        super(config);
    }

    protected abstract void process(Map event);
}
