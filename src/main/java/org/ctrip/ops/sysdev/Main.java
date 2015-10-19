package org.ctrip.ops.sysdev;

import org.ctrip.ops.sysdev.filters.BaseFilter;
import org.ctrip.ops.sysdev.inputs.BaseInput;
import org.ctrip.ops.sysdev.outputs.BaseOutput;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.ctrip.ops.sysdev.configs.HangoutConfig;

public class Main {
	private static final Logger logger = Logger.getLogger(Main.class.getName());

	public class Option {
		String flag, opt;

		public Option(String flag, String opt) {
			this.flag = flag;
			this.opt = opt;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws Exception {
		ArrayList<String> argsList = new ArrayList<String>();
		HashMap<String, String> optsList = new HashMap<String, String>();
		ArrayList<String> singleOptsList = new ArrayList<String>();

		for (int i = 0; i < args.length; i++) {
			switch (args[i].charAt(0)) {
			case '-':
				if (args[i].length() < 2)
					throw new IllegalArgumentException("Not a valid argument: "
							+ args[i]);
				if (args[i].charAt(1) == '-') {
					if (args[i].length() < 3)
						throw new IllegalArgumentException(
								"Not a valid argument: " + args[i]);
					// --opt
					singleOptsList.add(args[i].substring(2, args[i].length()));
				} else {
					if (args.length - 1 == i || args[i + 1].charAt(0) == '-') {
						singleOptsList.add(args[i].substring(1,
								args[i].length()));
					} else {// -opt
						optsList.put(args[i].substring(1, args[i].length()),
								args[i + 1]);
						i++;
					}
				}
				break;
			default:
				// arg
				argsList.add(args[i]);
				break;
			}
		}

		if (optsList.containsKey("l")) {
			DailyRollingFileAppender fa = new DailyRollingFileAppender();
			fa.setName("FileLogger");
			fa.setFile(optsList.get("l"));
			fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
			if (singleOptsList.contains("vvvv")) {
				fa.setThreshold(Level.TRACE);
			} else if (singleOptsList.contains("vv")) {
				fa.setThreshold(Level.DEBUG);
			} else if (singleOptsList.contains("v")) {
				fa.setThreshold(Level.INFO);
			} else {
				fa.setThreshold(Level.WARN);
			}
			fa.setAppend(true);
			fa.activateOptions();
			Logger.getRootLogger().addAppender(fa);
		} else {
			ConsoleAppender console = new ConsoleAppender();
			String PATTERN = "%d [%p|%c|%C{1}] %m%n";
			console.setLayout(new PatternLayout(PATTERN));
			if (singleOptsList.contains("vvvv")) {
				console.setThreshold(Level.TRACE);
			} else if (singleOptsList.contains("vv")) {
				console.setThreshold(Level.DEBUG);
			} else if (singleOptsList.contains("v")) {
				console.setThreshold(Level.INFO);
			} else {
				console.setThreshold(Level.WARN);
			}
			console.activateOptions();
			Logger.getRootLogger().addAppender(console);
		}

		// parse configure file
		Map configs = HangoutConfig.parse(optsList.get("f"));
		logger.debug(configs);

		// for filter in filters
		BaseFilter[] filterProcessors = new BaseFilter[0];

		if (configs.containsKey("filters")) {
			ArrayList<Map> filters = (ArrayList<Map>) configs.get("filters");
			filterProcessors = new BaseFilter[filters.size()];

			int idx = 0;
			for (Map filter : filters) {
				Iterator<Entry<String, Map>> filterIT = filter.entrySet()
						.iterator();

				while (filterIT.hasNext()) {
					Map.Entry<String, Map> filterEntry = filterIT.next();
					String filterType = filterEntry.getKey();
					Map filterConfig = filterEntry.getValue();

					Class<?> filterClass = Class
							.forName("org.ctrip.ops.sysdev.filters."
									+ filterType);
					Constructor<?> ctor = filterClass.getConstructor(Map.class,
							ArrayBlockingQueue.class);
					// BaseFilter filterInstance = (BaseFilter)
					// ctor.newInstance(
					// filterConfig, inputQueue);
					BaseFilter filterInstance = (BaseFilter) ctor.newInstance(
							filterConfig, null);
					filterProcessors[idx] = filterInstance;
					idx++;

					// inputQueue = filterInstance.getOutputMQ();
					// int threads = 1;
					// if (filterConfig.containsKey("threads")) {
					// threads = (int) filterConfig.get("threads");
					// }
					// for (int i = 0; i < threads; i++) {
					// new Thread(filterInstance).start();
					// }
				}
			}
		}

		// for output in output
		ArrayList<Map> outputs = (ArrayList<Map>) configs.get("outputs");

		BaseOutput[] outputProcessors = new BaseOutput[outputs.size()];

		int idx = 0;
		for (Map output : outputs) {
			Iterator<Entry<String, Map>> outputIT = output.entrySet()
					.iterator();

			while (outputIT.hasNext()) {
				Map.Entry<String, Map> outputEntry = outputIT.next();
				String outputType = outputEntry.getKey();
				Map outputConfig = outputEntry.getValue();
				Class<?> outputClass = Class
						.forName("org.ctrip.ops.sysdev.outputs." + outputType);
				Constructor<?> ctor = outputClass.getConstructor(Map.class,
						ArrayBlockingQueue.class);

				outputProcessors[idx] = (BaseOutput) ctor.newInstance(
						outputConfig, null);
				idx++;
			}
		}

		// for input in all_inputs
		ArrayList<Map> inputs = (ArrayList<Map>) configs.get("inputs");

		for (Map input : inputs) {
			Iterator<Entry<String, Map>> inputIT = input.entrySet().iterator();

			while (inputIT.hasNext()) {
				Map.Entry<String, Map> inputEntry = inputIT.next();
				String inputType = inputEntry.getKey();
				Map inputConfig = inputEntry.getValue();

				Class<?> inputClass = Class
						.forName("org.ctrip.ops.sysdev.inputs." + inputType);
				Constructor<?> ctor = inputClass.getConstructor(Map.class,
						BaseFilter[].class, ArrayList.class);
				BaseInput inputInstance = (BaseInput) ctor.newInstance(
						inputConfig, filterProcessors, outputs);
				inputInstance.emit();
			}
		}
	}
}
