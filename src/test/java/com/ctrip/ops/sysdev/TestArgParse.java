package com.ctrip.ops.sysdev;

import org.apache.commons.cli.Options;
import org.testng.annotations.Test;

/**
 * Created by joeywen on 11/28/15.
 */
public class TestArgParse {

    @Test
    public void testParseArg() throws Exception {
        String[] args = new String[]{"-f", "test config file"};
        Options options = new Options();
        options.addOption("h", false, "usage help");
        options.addOption("help", false, "usage help");
        options.addOption("f", true, "configuration file");
        options.addOption("l", true, "log file");
        options.addOption("w", true, "filter worker number");

        System.out.println(options.getOptions().toString());
    }

}
