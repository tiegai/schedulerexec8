package com.nike.ncp.scheduler.common.glue;

import com.nike.ncp.scheduler.common.handler.IJobHandler;
import com.nike.ncp.scheduler.common.glue.impl.SpringGlueFactory;
import groovy.lang.GroovyClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * glue factory, product class/object by name
 */
public class GlueFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlueFactory.class);

    private static final int TYPE_0 = 0;
    private static final int TYPE_1 = 1;
    private static GlueFactory glueFact = new GlueFactory();

    public static GlueFactory getInstance() {
        return glueFact;
    }

    public static void refreshInstance(int type) {
        if (type == TYPE_0) {
            glueFact = new GlueFactory();
        } else if (type == TYPE_1) {
            glueFact = new SpringGlueFactory();
        }
    }


    /**
     * groovy class loader
     */
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

    //private transient GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
    //private transient ConcurrentMap<String, Class<?>> classCache = new ConcurrentHashMap<>();
    @SuppressWarnings("all")
    private Class<?> getCodeSourceClass(String codeSource) {
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
        ConcurrentMap<String, Class<?>> classCache = new ConcurrentHashMap<>();
        try {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(codeSource.getBytes(Charset.forName("UTF-8")));
            String md5Str = new BigInteger(1, md5).toString(16);

            Class<?> clazz = classCache.get(md5Str);
            if (clazz == null) {
                clazz = groovyClassLoader.parseClass(codeSource);
                clazz = classCache.putIfAbsent(md5Str, clazz);
            }
            return clazz;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return groovyClassLoader.parseClass(codeSource);
        }
    }

    public void injectService(Object instance) {
        // do something
    }

}
