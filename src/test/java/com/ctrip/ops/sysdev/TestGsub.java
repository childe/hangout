package com.ctrip.ops.sysdev;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Gsub;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestGsub {
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
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
		String payload = URLDecoder.decode((String) event.get("payload"),"UTF-8");
		Assert.assertNotNull(JSONValue.parse(payload));
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public static void main(String[] args) throws ParseException, UnsupportedEncodingException {
		String s = "{\\x0D\\x0A  \\x22size\\x22: 100,\\x0D\\x0A  \\x22sort\\x22: [\\x0D\\x0A    {\\x0D\\x0A      \\x22cityInfo.cityId\\x22: {\\x0D\\x0A        \\x22order\\x22: \\x22asc\\x22\\x0D\\x0A      }\\x0D\\x0A    },\\x0D\\x0A    {\\x0D\\x0A      \\x22hotelName\\x22: {\\x0D\\x0A        \\x22order\\x22: \\x22asc\\x22\\x0D\\x0A      }\\x0D\\x0A    }\\x0D\\x0A  ],\\x0D\\x0A  \\x22filter\\x22: {\\x0D\\x0A    \\x22and\\x22: {\\x0D\\x0A      \\x22filters\\x22: [\\x0D\\x0A        {\\x0D\\x0A          \\x22term\\x22: {\\x0D\\x0A            \\x22cityInfo.countryId\\x22: 1\\x0D\\x0A          }\\x0D\\x0A        },\\x0D\\x0A        {\\x0D\\x0A          \\x22term\\x22: {\\x0D\\x0A            \\x22masterHotelId\\x22: -1\\x0D\\x0A          }\\x0D\\x0A        },\\x0D\\x0A        {\\x0D\\x0A          \\x22fquery\\x22: {\\x0D\\x0A            \\x22query\\x22: {\\x0D\\x0A              \\x22query_string\\x22: {\\x0D\\x0A                \\x22query\\x22: \\x22jiudian\\x22,\\x0D\\x0A                \\x22fields\\x22: [\\x0D\\x0A                  \\x22hotelName\\x22,\\x0D\\x0A                  \\x22hotelNameEn\\x22\\x0D\\x0A                ],\\x0D\\x0A                \\x22default_operator\\x22: \\x22and\\x22,\\x0D\\x0A                \\x22analyzer\\x22: \\x22ik_smart\\x22\\x0D\\x0A              }\\x0D\\x0A            },\\x0D\\x0A            \\x22_cache\\x22: true\\x0D\\x0A          }\\x0D\\x0A        }\\x0D\\x0A      ]\\x0D\\x0A    }\\x0D\\x0A  }\\x0D\\x0A}";
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
		String payload = URLDecoder.decode((String) event.get("payload"),"UTF-8");
		System.out.println(payload);
	}
}
