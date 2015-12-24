package org.ctrip.ops.sysdev;

import org.apache.commons.cli.CommandLine;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by joeywen on 11/28/15.
 */
public class TestArgParse {

    @Test
    public void testParseArg() throws Exception {
        String[] args = new String[]{"-f", "test config file", "-l", "logs/log.txt", "-vvv"};
        CommandLine cmdLine = Main.parseArg(args);

        assertEquals("test config file", cmdLine.getOptionValue("f"));
        assertEquals("logs/log.txt", cmdLine.getOptionValue("l"));
        assertTrue(cmdLine.hasOption("vvv"));
    }

}
