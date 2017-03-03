package com.ctrip.ops.sysdev.test;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Translate;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestTranslate {
    @Test
    public void testTranslate() throws IOException {
        File temp = File.createTempFile("test-dict", ".yml");
        BufferedWriter output = new BufferedWriter(new FileWriter(temp));
        output.write("abc: xyz\n");
        output.flush();


        String c = String
                .format("%s\n%s\n%s",
                        "dictionary_path: " + temp.getAbsolutePath(),
                        "source: name",
                        "target: nick"
                );
        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        Translate filter = new Translate(config);
        Map<String, Object> event = new HashMap();
        event.put("name", "abc");
        event = filter.process(event);

        Assert.assertEquals(event.get("nick"), "xyz");

        //multi level json
        c = String
                .format("%s\n%s\n%s",
                        "dictionary_path: " + temp.getAbsolutePath(),
                        "source: '[name][first]'",
                        "target: '[nick][first]'"
                );
        yaml = new Yaml();
        config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        filter = new Translate(config);
        event.clear();
        event.put("name", new HashMap() {{
            this.put("first", "abc");
        }});
        event = filter.process(event);

        Assert.assertEquals(((Map) event.get("nick")).get("first"), "xyz");

        // not exists in dictionary
        c = String
                .format("%s\n%s\n%s",
                        "dictionary_path: " + temp.getAbsolutePath(),
                        "source: '[name][first]'",
                        "target: '[nick][first]'"
                );
        yaml = new Yaml();
        config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        filter = new Translate(config);
        event.clear();
        event.put("name", new HashMap() {{
            this.put("first", "hello java");
        }});
        event = filter.process(event);

        Assert.assertNull(event.get("nick"));
    }
}
