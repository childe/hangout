package org.ctrip.ops.sysdev.configs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class HangoutConfig {
	private static final Logger logger = Logger.getLogger(HangoutConfig.class
			.getName());

	public static Map parse(String filename) throws FileNotFoundException {
		Yaml yaml = new Yaml();
		FileInputStream input;
		input = new FileInputStream(new File(filename));
		Map configs = (Map) yaml.load(input);
		return configs;

	}
}
