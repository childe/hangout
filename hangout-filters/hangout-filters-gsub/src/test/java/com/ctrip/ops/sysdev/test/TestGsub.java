package com.ctrip.ops.sysdev.test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import com.ctrip.ops.sysdev.filters.Gsub;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.Test;


public class TestGsub {
    @Test
    @SuppressWarnings({"rawtypes", "unchecked", "serial"})
    public void testGsub() throws UnsupportedEncodingException {
        String s = "{\\x0D\\x0A  \\x22size\\x22: 100,\\x0D\\x0A  \\x22sort\\x22: [\\x0D\\x0A    {\\x0D\\x0A      \\x22cityInfo.cityId\\x22: {\\x0D\\x0A        \\x22order\\x22: \\x22asc\\x22\\x0D\\x0A      }\\x0D\\x0A    },\\x0D\\x0A    {\\x0D\\x0A      \\x22hotelName\\x22: {\\x0D\\x0A        \\x22order\\x22: \\x22asc\\x22\\x0D\\x0A      }\\x0D\\x0A    }\\x0D\\x0A  ],\\x0D\\x0A  \\x22filter\\x22: {\\x0D\\x0A    \\x22and\\x22: {\\x0D\\x0A      \\x22filters\\x22: [\\x0D\\x0A        {\\x0D\\x0A          \\x22term\\x22: {\\x0D\\x0A            \\x22cityInfo.countryId\\x22: 1\\x0D\\x0A          }\\x0D\\x0A        },\\x0D\\x0A        {\\x0D\\x0A          \\x22term\\x22: {\\x0D\\x0A            \\x22masterHotelId\\x22: -1\\x0D\\x0A          }\\x0D\\x0A        },\\x0D\\x0A        {\\x0D\\x0A          \\x22fquery\\x22: {\\x0D\\x0A            \\x22query\\x22: {\\x0D\\x0A              \\x22query_string\\x22: {\\x0D\\x0A                \\x22query\\x22: \\x22jiudi\\x22,\\x0D\\x0A                \\x22fields\\x22: [\\x0D\\x0A                  \\x22hotelName\\x22,\\x0D\\x0A                  \\x22hotelNameEn\\x22\\x0D\\x0A                ],\\x0D\\x0A                \\x22default_operator\\x22: \\x22and\\x22,\\x0D\\x0A                \\x22analyzer\\x22: \\x22ik_smart\\x22\\x0D\\x0A              }\\x0D\\x0A            },\\x0D\\x0A            \\x22_cache\\x22: true\\x0D\\x0A          }\\x0D\\x0A        }\\x0D\\x0A      ]\\x0D\\x0A    }\\x0D\\x0A  }\\x0D\\x0A}";
        /*
         * Gsub: fields: "payload": ['\\x','%']
		 */
        HashMap config = new HashMap() {
            {
                put("fields", new HashMap() {
                    {
                        put("payload", new ArrayList() {
                            {
                                add("\\\\x");
                                add("%");
                            }
                        });
                    }
                });
            }
        };

        Gsub gsubFilter = new Gsub(config);

        Map event = new HashMap();
        event.put("payload", s);
        event = gsubFilter.process(event);
        String payload = URLDecoder.decode((String) event.get("payload"), "UTF-8");
        Assert.assertNotNull(JSONValue.parse(payload));

        String c = String.format("%s\n%s\n%s",
                "        fields:",
                "            '[metric][value1]': ['a','1']",
                "            '[metric][value2]': ['^a','1']"
        );
        Yaml yaml = new Yaml();
        config = (HashMap) yaml.load(c);
        Assert.assertNotNull(config);

        gsubFilter = new Gsub(config);

        event = new HashMap();
        event.put("nothing", "11.11");
        event.put("metric", new HashMap() {{
            this.put("value1", "abcdefgaa");
            this.put("value2", "abcdefgaa");
        }});

        event = gsubFilter.process(event);
        Assert.assertEquals(event.get("nothing"), "11.11");
        Assert.assertEquals(((Map) event.get("metric")).get("value1"), "1bcdefg11");
        Assert.assertEquals(((Map) event.get("metric")).get("value2"), "1bcdefgaa");
        Assert.assertNull(event.get("tags"));
    }
}
