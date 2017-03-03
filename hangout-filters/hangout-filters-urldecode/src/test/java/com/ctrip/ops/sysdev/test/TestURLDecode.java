package com.ctrip.ops.sysdev.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.URLDecode;
import org.apache.commons.collections4.map.HashedMap;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestURLDecode {
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testURLDecode() {
        String c = String.format("%s\n%s\n%s",
                "fields:",
                "    - query1",
                "    - '[extra][value2]'"
        );
        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        URLDecode URLDecodefilter = new URLDecode(config);

        // Yes
        Map event = new HashMap();
        event.put(
                "query1",
                "wd%3dq%2520gishp%2520su%26rsv_spt%3d1%26rsv_iqid%3d0xa37c438600003f19%26issp%3d1%26f%3d8%26rsv_bp%3d0%26rsv_idx%3d2%26ie%3dutf-8%26tn%3dbaiduhome_pg%26rsv_enter%3d1%26rsv_sug3%3d15%26rsv_sug1%3d6%26rsv_sug2%3d0%26rsv_sug7%3d100%26inputT%3d8493%26rsv_sug4%3d11379"
        );
        event.put("extra", new HashedMap() {{
            this.put("value2", "wd%3dq%2520gishp%2520su%26rsv_spt%3d1%26rsv_iqid%3d0xa37c438600003f19%26issp%3d1%26f%3d8%26rsv_bp%3d0%26rsv_idx%3d2%26ie%3dutf-8%26tn%3dbaiduhome_pg%26rsv_enter%3d1%26rsv_sug3%3d15%26rsv_sug1%3d6%26rsv_sug2%3d0%26rsv_sug7%3d100%26inputT%3d8493%26rsv_sug4%3d11379"
            );
        }});

        event = URLDecodefilter.process(event);
        Assert.assertEquals(
                event.get("query1"),
                "wd=q%20gishp%20su&rsv_spt=1&rsv_iqid=0xa37c438600003f19&issp=1&f=8&rsv_bp=0&rsv_idx=2&ie=utf-8&tn=baiduhome_pg&rsv_enter=1&rsv_sug3=15&rsv_sug1=6&rsv_sug2=0&rsv_sug7=100&inputT=8493&rsv_sug4=11379");
        Assert.assertEquals(
                ((Map) event.get("extra")).get("value2"),
                "wd=q%20gishp%20su&rsv_spt=1&rsv_iqid=0xa37c438600003f19&issp=1&f=8&rsv_bp=0&rsv_idx=2&ie=utf-8&tn=baiduhome_pg&rsv_enter=1&rsv_sug3=15&rsv_sug1=6&rsv_sug2=0&rsv_sug7=100&inputT=8493&rsv_sug4=11379");
        Assert.assertNull(event.get("tags"));

        // Exception
        event = new HashMap();
        event.put(
                "query1",
                "wd%3dq%2520gishp%2520su%26rsv_spt%3d1%26rsv_iqid%3d0xa37c438600003f19%26issp%3d1%26f%3d8%26rsv_bp%3d0%26rsv_idx%3d2%26ie%3dutf-8%26tn%3dbaiduhome_pg%26rsv_enter%3d1%26rsv_sug3%3d15%26rsv_sug1%3d6%26rsv_sug2%3d0%26rsv_sug7%3d100%26inputT%3d8493%26rsv_sug4%3d11379");

        event.put(
                "extra",
                new HashedMap() {{
                    this.put("value2", "wd=q gishp%%0su&rsv_spt=1&rsv_iqid=0xa37c438600003f19&issp=1&f=8&rsv_bp=0&rsv_idx=2&ie=utf-8&tn=baiduhome_pg&rsv_enter=1&rsv_sug3=15&rsv_sug1=6&rsv_sug2=0&rsv_sug7=100&inputT=8493&rsv_sug4=11379");
                }});

        event = URLDecodefilter.process(event);
        Assert.assertEquals(((ArrayList) event.get("tags")).get(0),
                "URLDecodefail");

        // Exception, NO tag
        c = "fields: [\"query1\",\"query2\"]\ntag_on_failure: null";
        yaml = new Yaml();
        config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        URLDecodefilter = new URLDecode(config);
        event = new HashMap();
        event.put(
                "query2",
                "wd=q gishp%%0su&rsv_spt=1&rsv_iqid=0xa37c438600003f19&issp=1&f=8&rsv_bp=0&rsv_idx=2&ie=utf-8&tn=baiduhome_pg&rsv_enter=1&rsv_sug3=15&rsv_sug1=6&rsv_sug2=0&rsv_sug7=100&inputT=8493&rsv_sug4=11379");
        event = URLDecodefilter.process(event);
        Assert.assertNull(event.get("tags"));
    }
}
