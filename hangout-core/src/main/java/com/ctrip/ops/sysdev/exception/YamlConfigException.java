package com.ctrip.ops.sysdev.exception;

/**
 * Created by gnuhpc on 2016/12/12.
 */
@SuppressWarnings("ALL")
public class YamlConfigException extends Exception{
    public YamlConfigException(String msg){
        super("Invalid Config Exception! " + msg);
    }
}
