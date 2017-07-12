package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import lombok.extern.log4j.Log4j2;
import ua_parser.Client;
import ua_parser.Parser;

import java.io.IOException;
import java.util.Map;

@Log4j2
public class UA extends BaseFilter {


	public UA(Map config) {
		super(config);
	}

	private String source;
	private Parser uaParser;

	protected void prepare() {
		if (!config.containsKey("source")) {
			log.error("no field configured in Json");
			System.exit(1);
		}
		this.source = (String) config.get("source");

		try {
			this.uaParser = new Parser();
		} catch (IOException e) {
			log.error(e);
			e.printStackTrace();
			System.exit(1);
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected Map filter(final Map event) {
		if (event.containsKey(this.source)) {
			Client c = uaParser.parse((String) event.get(this.source));

			event.put("userAgent_family", c.userAgent.family);
			event.put("userAgent_major", c.userAgent.major);
			event.put("userAgent_minor", c.userAgent.minor);
			event.put("os_family", c.os.family);
			event.put("os_major", c.os.major);
			event.put("os_minor", c.os.minor);
			event.put("device_family", c.device.family);
		}
		return event;
	}
}
