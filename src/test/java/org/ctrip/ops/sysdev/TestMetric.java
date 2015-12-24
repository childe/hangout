package org.ctrip.ops.sysdev;

import org.ctrip.ops.sysdev.inputs.Metric;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author joey.wen 2015/12/24
 */
public class TestMetric {

    @Test
    public void testScheduleMetric() throws Exception {
        final Map config = mock();
        final Map out = new HashMap();
        out.put("Stdout", new HashMap());
        Metric metric = new Metric(config, null, new ArrayList<Map>(){{add(out);}});
        metric.emit();
        Thread.sleep(60000);
    }

    private Map mock() {
        Map config = new HashMap();
        config.put("interval", 1);
        return config;
    }
}
