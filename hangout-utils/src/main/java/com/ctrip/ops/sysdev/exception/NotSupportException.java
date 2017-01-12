package com.ctrip.ops.sysdev.exception;

/**
 * Created by gnuhpc on 2016/12/12.
 */
@SuppressWarnings("ALL")
public class NotSupportException extends Exception{
    public NotSupportException(String msg) {
        super("Not Supported Exception! " + msg);
    }
}
