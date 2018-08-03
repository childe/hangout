package com.ctrip.ops.sysdev.test;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.FlatMetric;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;


public class TestFlatMetric {
    private ArrayList<Map<String, Object>> process(FlatMetric filter, ArrayList<Map<String, Object>> events) {
        return events;
    }

    @Test
    public void testFlatMetric() {
    }
}
