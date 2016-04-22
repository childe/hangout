package com.ctrip.ops.sysdev.inputs;

import com.ctrip.ops.sysdev.filters.BaseFilter;
import com.ctrip.ops.sysdev.outputs.BaseOutput;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

public class Stdin extends BaseInput {
    private static final Logger logger = Logger
            .getLogger(Stdin.class.getName());

    private boolean hostname;
    private String hostnameValue;

    public Stdin(Map<String, Object> config, ArrayList<Map> filter,
                 ArrayList<Map> outputs) throws Exception {
        super(config, filter, outputs);
        this.prepare();
    }

    @Override
    protected void prepare() {
        this.decoder = this.createDecoder();
        this.filterProcessors = this.createFilterProcessors();
        this.outputProcessors = this.createOutputProcessors();

        if (config.containsKey("hostname")) {
            this.hostname = (boolean) config.get("hostname");
        } else {
            this.hostname = false;
        }

        if (this.hostname == true) {
            try {
                this.hostnameValue = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                logger.warn("failed to get hostname");
                this.hostname = false;
            }
        }
    }

    public void emit() {
        try {
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(System.in));

            String input;

            while ((input = br.readLine()) != null) {
                try {
                    Map<String, Object> event = this.decoder
                            .decode(input);

                    if (this.hostname == true) {
                        event.put("hostname", this.hostnameValue);
                    }

                    if (this.filterProcessors != null) {
                        for (BaseFilter bf : filterProcessors) {
                            if (event == null) {
                                break;
                            }
                            event = bf.process(event);
                        }
                    }
                    if (event != null) {
                        for (BaseOutput bo : outputProcessors) {
                            bo.process(event);
                        }
                    }
                } catch (Exception e) {
                    logger.error("process event failed:" + input);
                    e.printStackTrace();
                    logger.error(e);
                }
            }

        } catch (IOException io) {
            io.printStackTrace();
            logger.error("Stdin loop got exception");
            System.exit(1);
        }
    }
}
