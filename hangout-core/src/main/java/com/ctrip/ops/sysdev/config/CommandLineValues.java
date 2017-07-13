package com.ctrip.ops.sysdev.config;

import lombok.Data;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by gnuhpc on 2016/12/11.
 */

@SuppressWarnings("ALL")
@Data
public class CommandLineValues {
    private final String[] arguments;
    private CmdLineParser parser;

    @Option(name = "-h", aliases = {"--help"}, usage = "Print Help Information", help = true)
    private boolean isHelp = false;

    @Option(name = "-f", aliases = {"--configfile"}, usage = " Specify a config file", required = true)
    private String configFile;

    @Option(name = "--version", usage = "Show Hangout Version")
    private boolean isShowVersion;

    public CommandLineValues(String... args) {
        parser = new CmdLineParser(this);
        arguments = args;
    }

    public void parseCmd() {
        printVersion();

        try {
            parser.parseArgument(arguments);

            // If help is needed
            if (isHelp) {
                parser.printUsage(System.err);
                System.exit(0);
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            // print the list of available options
            System.exit(1);
        }
    }

    private void printVersion() {
        if (getVersion() != null) {
            System.out.println("Hangout  Version:" + getVersion() + "  Copyright @Ctrip   Author : childe@github, gnuhpc@github");
        }
    }

    private String getVersion() {
        InputStream resourceAsStream =
                this.getClass().getResourceAsStream(
                        "/META-INF/maven/ctrip/hangout-core/pom.properties"
                );
        Properties prop = new Properties();
        try {
            prop.load(resourceAsStream);
        } catch (Exception e) {
            return null;
        }
        return prop.getProperty("version");
    }

    private void printUsage() {
        System.out.println("hangout [options...] arguments...");
        parser.printUsage(System.out);
        System.err.println();
    }
}
