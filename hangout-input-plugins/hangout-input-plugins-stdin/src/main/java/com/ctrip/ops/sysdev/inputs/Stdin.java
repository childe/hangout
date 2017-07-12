package com.ctrip.ops.sysdev.inputs;


import com.ctrip.ops.sysdev.baseplugin.BaseInput;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

@Log4j2
public class Stdin extends BaseInput {

    private boolean hostname;
    private String hostnameValue;

    public Stdin(Map<String, Object> config, ArrayList<Map> filter,
                 ArrayList<Map> outputs) throws Exception {
        super(config, filter, outputs);
    }

    @Override
    protected void prepare() {
        createFilterProcessors();
        createOutputProcessors();

        if (config.containsKey("hostname")) {
            this.hostname = (Boolean) config.get("hostname");
        } else {
            this.hostname = false;
        }

        if (this.hostname) {
            try {
                this.hostnameValue = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                log.warn("failed to get hostname", e);
                this.hostname = false;
            }
        }
    }

    @Override
    protected Map<String, Object> preprocess(Map<String, Object> event) {
        if (this.hostname) {
            event.put("hostname", this.hostnameValue);
        }
        return event;
    }

    @Override
    public void shutdown() {
        System.out.println("Stdin Input shutdown... ");
    }

    public void emit() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            String input;

            while ((input = br.readLine()) != null) {
                this.process(input);
            }

        } catch (IOException io) {
            log.error("Stdin loop got exception", io);
        }
    }
}

