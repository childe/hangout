package com.ctrip.ops.sysdev.filters;

import java.util.Map;
import org.apache.log4j.Logger;
import org.ipip.IPExt;


public class IPIP extends BaseFilter {
	private static final Logger logger = Logger.getLogger(IPIP.class
			.getName());

	public IPIP(Map config) {
		super(config);
	}

	private String source;
	private String target;

	protected void prepare() {
		if (!config.containsKey("source")) {
			logger.error("no source configured in GeoIP");
			System.exit(1);
		}
		this.source = (String) config.get("source");

		if (config.containsKey("target")) {
			this.target = (String) config.get("target");
		} else {
			this.target = "geoip";
		}

		if (config.containsKey("tag_on_failure")) {
			this.tagOnFailure = (String) config.get("tag_on_failure");
		} else {
			this.tagOnFailure = "ipipfail";
		}

		// A File object pointing to your GeoIP2 or GeoLite2 database
		if (!config.containsKey("database")) {
			logger.error("no database configured in GeoIP");
			System.exit(1);
		}

		try {
			IPExt.load((String) config.get("database"), true);
		} catch (Exception e) {
			logger.error("failed to prepare DatabaseReader for geoip");
			logger.error(e);
			System.exit(1);
		}
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map filter(final Map event) {
		if (event.containsKey(this.source)) {

			boolean success = true;
			String[] ips;
			try {
				ips = IPExt.find(this.source);
			} catch (Exception e) {
				logger.error(e);
				success = false;
			}

			this.postProcess(event, success);
		}

		return event;
	}
}
