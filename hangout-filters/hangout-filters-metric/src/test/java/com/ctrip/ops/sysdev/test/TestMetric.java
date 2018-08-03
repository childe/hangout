package com.ctrip.ops.sysdev.test;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Metric;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;


public class TestMetric {
    private ArrayList<Map<String, Object>> process(Metric filter, ArrayList<Map<String, Object>> events) {
        return events;
    }

    @Test
    public void testMetric() {

    }
}
