package org.ctrip.ops.sysdev;

import org.apache.commons.cli.*;
import org.ctrip.ops.sysdev.filters.BaseFilter;
import org.ctrip.ops.sysdev.inputs.BaseInput;
import org.ctrip.ops.sysdev.outputs.BaseOutput;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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

	/**
	 *  parse the input command arguments
	 * @param args
	 * @return
	 * @throws ParseException
	 */
	private static CommandLine parseArg(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("h", false, "usage help");
		options.addOption("help", false, "usage help");
		options.addOption("f", true, "configuration file");
		options.addOption("l", false, "log file");
		options.addOption("w", false, "filter worker number");
		options.addOption("v", false, "print info log");
		options.addOption("vv", false, "print debug log");
		options.addOption("vvv", false, "print trace log");

		CommandLineParser paraer = new BasicParser();
		CommandLine cmdLine = paraer.parse(options, args);

		if (cmdLine.hasOption("help") || cmdLine.hasOption("h")) {
			usage();
			System.exit(-1);
		}

		// TODO need process invalid arguments
		if(!cmdLine.hasOption("f")) {
			throw new IllegalArgumentException("Required -f argument to specify config file");
		}

		return cmdLine;
	}

	/**
	 * print help information
	 */
	private static void usage() {
		StringBuilder helpInfo = new StringBuilder();
		helpInfo.append("-h").append("\t\t\thelp command").append("\n")
				.append("-help").append("\t\t\thelp command").append("\n")
				.append("-f").append("\t\t\trequired config, indicate config file").append("\n")
				.append("-l").append("\t\t\tlog file that store the output").append("\n")
				.append("-w").append("\t\t\tfilter worker numbers").append("\n")
				.append("-v").append("\t\t\tprint info log").append("\n")
				.append("-vv").append("\t\t\tprint debug log").append("\n")
				.append("-vvv").append("\t\t\tprint trace log").append("\n");

		System.out.println(helpInfo.toString());
	}


	public static void main(String[] args) throws Exception {
		CommandLine cmdLine = parseArg(args);
		setupLogger(cmdLine);

		// parse configure file
		Map configs = HangoutConfig.parse(cmdLine.getOptionValue("f"));
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
					Constructor<?> ctor = filterClass.getConstructor(Map.class);

					BaseFilter filterInstance = (BaseFilter) ctor
							.newInstance(filterConfig);
					filterProcessors[idx] = filterInstance;
					idx++;
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
				Constructor<?> ctor = outputClass.getConstructor(Map.class);

				outputProcessors[idx] = (BaseOutput) ctor
						.newInstance(outputConfig);
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

	/**
	 * Setup logger according arguments
	 * @param cmdLine
	 */
	private static void setupLogger(CommandLine cmdLine) {
		if (cmdLine.hasOption("l")) {
			DailyRollingFileAppender fa = new DailyRollingFileAppender();
			fa.setName("FileLogger");
			fa.setFile(cmdLine.getOptionValue("l"));
			fa.setLayout(new PatternLayout("%d %p %C %t %m%n"));
			if (cmdLine.hasOption("vvvv")) {
				fa.setThreshold(Level.TRACE);
				Logger.getRootLogger().setLevel(Level.TRACE);
			} else if (cmdLine.hasOption("vv")) {
				fa.setThreshold(Level.DEBUG);
			} else if (cmdLine.hasOption("v")) {
				fa.setThreshold(Level.INFO);
			} else {
				fa.setThreshold(Level.WARN);
			}
			fa.setAppend(true);
			fa.activateOptions();
			Logger.getRootLogger().addAppender(fa);
		} else {
			ConsoleAppender console = new ConsoleAppender();
			String PATTERN = "%d %p %C %t %m%n";
			console.setLayout(new PatternLayout(PATTERN));
			if (cmdLine.hasOption("vvvv")) {
				console.setThreshold(Level.TRACE);
				Logger.getRootLogger().setLevel(Level.TRACE);
			} else if (cmdLine.hasOption("vv")) {
				console.setThreshold(Level.DEBUG);
			} else if (cmdLine.hasOption("v")) {
				console.setThreshold(Level.INFO);
			} else {
				console.setThreshold(Level.WARN);
			}
			console.activateOptions();
			Logger.getRootLogger().addAppender(console);
		}
	}

}
