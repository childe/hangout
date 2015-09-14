package org.ctrip.ops.sysdev.configs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ctrip.ops.sysdev.Main;
import org.yaml.snakeyaml.Yaml;

public class HangoutConfig {
	private static final Logger logger = Logger.getLogger(HangoutConfig.class.getName());

	public static Map parse(String filename) {
		Yaml yaml = new Yaml();
		FileInputStream input;
		try {
			input = new FileInputStream(new File(filename));
			Map configs = (Map) yaml.load(input);
			return configs;
		} catch (FileNotFoundException e) {
			logger.error(String.format("parse %s failed", filename));
			logger.trace(e.getMessage());
			return null;
		}

		// final HashMap kafkaInput1 = new HashMap();
		// kafkaInput1.put("topic", "metric_thrift.15s");
		// kafkaInput1.put("groupID", "hangout");
		// kafkaInput1.put("zk", "192.168.81.156:2181");
		//
		// final HashMap kafkaInput2 = new HashMap();
		// kafkaInput2.put("topic", "metric_thrift.1m");
		// kafkaInput2.put("groupID", "hangout");
		// kafkaInput2.put("zk", "192.168.81.156:2181");
		//
		// ArrayList<Map> inputs = new ArrayList<Map>() {
		// {
		// add(new HashMap() {
		// {
		// put("Kafka", kafkaInput1);
		// }
		// });
		// add(new HashMap() {
		// {
		// put("Kafka", kafkaInput2);
		// }
		// });
		// }
		// };
		// configs.put("inputs", inputs);

		// return configs;

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
