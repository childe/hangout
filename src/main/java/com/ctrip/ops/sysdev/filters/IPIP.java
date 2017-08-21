package com.ctrip.ops.sysdev.filters;

import java.util.Map;
import java.util.HashMap;
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
			String src = (String)event.get(this.source);
			try {
				if (src.length() > 0) {
					String[] ips;
					ips = IPExt.find(src);
					String country_name = ips[0];
					String region_name = ips[1];
					String city_name = ips[2];
					String isp = ips[4];

					Map targetObj = new HashMap();
					event.put(this.target, targetObj);
					targetObj.put("country_name", country_name);
					targetObj.put("region_name", region_name);
					targetObj.put("city_name", city_name);
					targetObj.put("isp", isp);
				}
			} catch (Exception e) {
				logger.error(e);
				success = false;
			}

			this.postProcess(event, success);
		}

		return event;
	}
}
