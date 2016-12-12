package com.ctrip.ops.sysdev.exception;

/**
 * Created by gnuhpc on 2016/12/12.
 */
public class NotSupportException extends Exception{
    public NotSupportException(String msg) {
        super("Not Supported Exception! " + msg);
    }
}
