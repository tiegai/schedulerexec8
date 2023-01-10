package com.nike.ncp.scheduler.common.glue;

import com.nike.ncp.scheduler.common.handler.IJobHandler;
import com.nike.ncp.scheduler.common.glue.impl.SpringGlueFactory;
import groovy.lang.GroovyClassLoader;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * glue factory, product class/object by name
 */
public class GlueFactory {
    private static GlueFactory glueFactory = new GlueFactory();

    public static GlueFactory getInstance() {
        return glueFactory;
    }

    public static void refreshInstance(int type) {
        if (type == 0) {
            glueFactory = new GlueFactory();
        } else if (type == 1) {
            glueFactory = new SpringGlueFactory();
        }
    }


    /**
     * groovy class loader
     */
    private transient GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
    @SuppressWarnings("unchecked")
    private transient ConcurrentMap<String, Class<?>> classCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public IJobHandler loadNewInstance(String codeSource) throws Exception {
        if (codeSource != null && codeSource.trim().length() > 0) {
            Class<?> clazz = getCodeSourceClass(codeSource);
            if (clazz != null) {
                //Object instance = clazz.newInstance();
                Object instance = clazz.getDeclaredConstructor().newInstance();
                //if (instance != null) {
                    if (instance instanceof IJobHandler) {
                        this.injectService(instance);
                        return (IJobHandler) instance;
                    } else {
                        throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, " + "cannot convert from instance[" + instance.getClass() + "] to IJobHandler");
                    }
                //}
            }
        }
        throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, instance is null");
    }

    @SuppressWarnings("unchecked")
    private Class<?> getCodeSourceClass(String codeSource) {
        try {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(codeSource.getBytes());
            String md5Str = new BigInteger(1, md5).toString(16);

            Class<?> clazz = classCache.get(md5Str);
            if (clazz == null) {
                clazz = groovyClassLoader.parseClass(codeSource);
                classCache.putIfAbsent(md5Str, clazz);
            }
            return clazz;
        } catch (Exception e) {
            return groovyClassLoader.parseClass(codeSource);
        }
    }

    public void injectService(Object instance) {
        // do something
    }

}
