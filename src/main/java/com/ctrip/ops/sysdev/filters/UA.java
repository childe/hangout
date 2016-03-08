package com.ctrip.ops.sysdev.filters;

import java.io.IOException;
import java.util.Map;

import ua_parser.Parser;
import ua_parser.Client;

import org.apache.log4j.Logger;

public class UA extends BaseFilter {
	private static final Logger logger = Logger.getLogger(UA.class.getName());

	public UA(Map config) {
		super(config);
	}

	private String source;
	private Parser uaParser;

	protected void prepare() {
		if (!config.containsKey("source")) {
			logger.error("no field configured in Json");
			System.exit(1);
		}
		this.source = (String) config.get("source");

		try {
			this.uaParser = new Parser();
		} catch (IOException e) {
			logger.error(e);
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
