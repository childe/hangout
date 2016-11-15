package com.ctrip.ops.sysdev.monitor.http;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;



import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import com.ctrip.ops.sysdev.monitor.*;

/**
 * Created by jian.shu@ele.me on 16/11/11.
 */

public class HTTPMetricsServer implements MonitorServices {

    private Server jettyServer;
    private int port;
    private static Logger LOG = LoggerFactory.getLogger(HTTPMetricsServer.class);
    public static int DEFAULT_PORT = 41414;
    public static String CONFIG_PORT = "port";

    @Override
    public void start() {
        jettyServer = new Server();
        //We can use Contexts etc if we have many urls to handle. For one url,
        //specifying a handler directly is the most efficient.
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setReuseAddress(true);
        connector.setPort(DEFAULT_PORT);
        jettyServer.setConnectors(new Connector[] {connector});
        jettyServer.setHandler(new HTTPMetricsHandler());
        try {
            jettyServer.start();
            while (!jettyServer.isStarted()) {
                Thread.sleep(500);
            }
        } catch (Exception ex) {
            LOG.error("Error starting Jetty. JSON Metrics may not be available.", ex);
        }

    }

    @Override
    public void stop() {
        try {
            jettyServer.stop();
            jettyServer.join();
        } catch (Exception ex) {
            LOG.error("Error stopping Jetty. JSON Metrics may not be available.", ex);
        }

    }


    private class HTTPMetricsHandler extends AbstractHandlers {

        Type mapType =
                new TypeToken<Map<String, Map<String, String>>>() {
                }.getType();
        Gson gson = new Gson();

        @Override
        public void handle(String target,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           int dispatch) throws IOException, ServletException {
            // /metrics is the only place to pull metrics.
            //If we want to use any other url for something else, we should make sure
            //that for metrics only /metrics is used to prevent backward
            //compatibility issues.
            if(request.getMethod().equalsIgnoreCase("TRACE") || request.getMethod()
                    .equalsIgnoreCase("OPTIONS")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                response.flushBuffer();
                ((Request) request).setHandled(true);
                return;
            }
            if (target.equals("/")) {
                response.setContentType("text/html;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("metrics please click"
                        + " <a href = \"./metrics\"> here</a>.");
                response.flushBuffer();
                ((Request) request).setHandled(true);
                return;
            } else if (target.equalsIgnoreCase("/metrics")) {
                response.setContentType("application/json;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                Map<String, Map<String, String>> metricsMap = JMXPollUtil.getAllMBeans();
                String json = gson.toJson(metricsMap, mapType);
                response.getWriter().write(json);
                response.flushBuffer();
                ((Request) request).setHandled(true);
                return;
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            response.flushBuffer();
            //Not handling the request returns a Not found error page.
        }
    }
}
