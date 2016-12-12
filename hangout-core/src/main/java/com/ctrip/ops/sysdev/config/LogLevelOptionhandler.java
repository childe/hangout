package com.ctrip.ops.sysdev.config;

import org.apache.log4j.Level;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by gnuhpc on 2016/12/12.
 */
public class LogLevelOptionhandler extends OneArgumentOptionHandler<Level> {
    public LogLevelOptionhandler(CmdLineParser parser, OptionDef option, Setter<? super Level> setter) {
        super(parser, option, setter);
    }

    @Override
    protected Level parse(String s) throws NumberFormatException, CmdLineException {
            switch (s.toLowerCase()){
                case "error":
                    return Level.ERROR;
                case "debug":
                    return Level.DEBUG;
                case "trace":
                    return Level.TRACE;
                case "info":
                    default:
                    return Level.INFO;
            }
    }
}
