package com.ctrip.ops.sysdev.baseplugin;

import java.util.Map;

/**
 * Created by liujia on 17/7/4.
 */

public abstract class BaseMetric extends Base {
    public BaseMetric(Map config) {
        super(config);
    }

    public void register() {
    }
}
