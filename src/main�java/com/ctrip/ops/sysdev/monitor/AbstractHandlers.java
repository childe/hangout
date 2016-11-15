package com.ctrip.ops.sysdev.monitor;


import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.jetty.Handler;
import org.mortbay.log.Log;

/**
 * Created by shujian on 16/11/12.
 */

public abstract class AbstractHandlers extends AbstractLifeCycle implements Handler {
    protected String _string;
    private Server _server;

    public AbstractHandlers() {
    }

    protected void doStart() throws Exception {
        Log.debug("starting {}", this);
    }

    protected void doStop() throws Exception {
        Log.debug("stopping {}", this);
    }

    public String toString() {
        if(this._string == null) {
            this._string = super.toString();
            this._string = this._string.substring(this._string.lastIndexOf(46) + 1);
        }

        return this._string;
    }

    public void setServer(Server server) {
        Server old_server = this._server;
        if(old_server != null && old_server != server) {
            old_server.getContainer().removeBean(this);
        }

        this._server = server;
        if(this._server != null && this._server != old_server) {
            this._server.getContainer().addBean(this);
        }

    }

    public Server getServer() {
        return this._server;
    }

    public void destroy() {
        if(!this.isStopped()) {
            throw new IllegalStateException("!STOPPED");
        } else {
            if(this._server != null) {
                this._server.getContainer().removeBean(this);
            }

        }
    }
}