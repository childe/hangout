package com.ctrip.ops.sysdev.log;

import com.ctrip.ops.sysdev.config.CommandLineValues;
import lombok.extern.log4j.Log4j2;

/**
 * Created by gnuhpc on 2016/12/12.
 */
@SuppressWarnings("ALL")
public class LogSetter {
    /**
     * Init logger according arguments
     *
     * @param cmdLine
     */
    public void initLogger(CommandLineValues cmdLine) {
//        WriterAppender wa;
//
//        if (cmdLine.getLogFile() != null) {
//            String logPath = cmdLine.getLogFile();
//            wa = new DailyRollingFileAppender(); // Pull up
//            DailyRollingFileAppender da = (DailyRollingFileAppender) wa; // Pull down
//            da.setName("FileLogger");
//            ((DailyRollingFileAppender) wa).setFile(logPath);
//            setLogger(cmdLine, wa, Logger.getRootLogger());
//        } else {
//            //Set Console Log
//            wa = new ConsoleAppender(); //Pull up
//            ConsoleAppender ca = (ConsoleAppender) wa; // Pull down
//            setLogger(cmdLine, wa, Logger.getRootLogger());
//        }
    }


//    private static void setLogger(CommandLineValues cmdLine, WriterAppender appender, Logger logger) {
//        String PATTERN = "%d %p %C %t %m%n";
//        PatternLayout patternLayout = new PatternLayout(PATTERN);
//        appender.setLayout(patternLayout);
//        appender.activateOptions();
//        appender.setThreshold(cmdLine.customGetLogLevel());
//        logger.setLevel(cmdLine.customGetLogLevel());
//        logger.addAppender(appender);
//    }
}
