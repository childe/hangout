package com.ctrip.ops.sysdev.monitor;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.util.parsing.combinator.testing.Str;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;


/**
 * Created by jian.shu@ele.me on 16/11/12.
 */

public class JMXPollUtil {

    /*
        public  static Map<String,Map<String,String>> getAllMBeans() {
            Map<String, Map<String, String>> mbeanMap = Maps.newHashMap();
            Map<String,String> tmp = new HashMap<String,String>();
            tmp.put("nginx_connection","100");
            mbeanMap.put("all_metrics",tmp);
            return mbeanMap;

        }*/
    private static Logger LOG = LoggerFactory.getLogger(JMXPollUtil.class);
    private static MBeanServer mbeanServer = ManagementFactory.
            getPlatformMBeanServer();


    public static Map<String, Map<String, String>> getAllMBeans() {
        Map<String, Map<String, String>> mbeanMap = Maps.newHashMap();
        Set<ObjectInstance> queryMBeans = null;
        try {
            queryMBeans = mbeanServer.queryMBeans(null, null);
        } catch (Exception ex) {
            LOG.error("Could not get Mbeans for monitoring", ex);
            Throwables.propagate(ex);
        }
        for (ObjectInstance obj : queryMBeans) {
            try {
                if (!obj.getObjectName().toString().startsWith("com.ctrip.ops")) {
                    continue;
                }
                MBeanAttributeInfo[] attrs = mbeanServer.
                        getMBeanInfo(obj.getObjectName()).getAttributes();
                String strAtts[] = new String[attrs.length];
                for (int i = 0; i < strAtts.length; i++) {
                    strAtts[i] = attrs[i].getName();
                }
                AttributeList attrList = mbeanServer.getAttributes(
                        obj.getObjectName(), strAtts);
                String component = obj.getObjectName().toString().substring(
                        obj.getObjectName().toString().indexOf('=') + 1);
                Map<String, String> attrMap = Maps.newHashMap();


                for (Object attr : attrList) {
                    Attribute localAttr = (Attribute) attr;
                    if (localAttr.getName().equalsIgnoreCase("type")) {
                        component = localAttr.getValue() + "." + component;
                    }
                    attrMap.put(localAttr.getName(), localAttr.getValue().toString());
                }
                mbeanMap.put(component, attrMap);
            } catch (Exception e) {
                LOG.error("Unable to poll JMX for metrics.", e);
            }
        }
        return mbeanMap;
    }
}

