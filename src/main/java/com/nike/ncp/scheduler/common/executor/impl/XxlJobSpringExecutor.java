package com.nike.ncp.scheduler.common.executor.impl;

import com.nike.ncp.scheduler.common.executor.XxlJobExecutor;
import com.nike.ncp.scheduler.common.glue.GlueFactory;
import com.nike.ncp.scheduler.common.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Map;


/**
 * xxl-job executor (for spring)
 *
 */
public class XxlJobSpringExecutor extends XxlJobExecutor implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(XxlJobSpringExecutor.class);


    // start
    @Override
    public void afterSingletonsInstantiated() {

        // init JobHandler Repository
        /*initJobHandlerRepository(applicationContext);*/

        // init JobHandler Repository (for method)
        initJobHandlerMethodRepository(applicationContextValue);

        // refresh GlueFactory
        GlueFactory.refreshInstance(1);

        // super start
        try {
            super.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // destroy
    @Override
    public void destroy() {
        super.destroy();
    }


    /*private void initJobHandlerRepository(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }

        // init job handler action
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(JobHandler.class);

        if (serviceBeanMap != null && serviceBeanMap.size() > 0) {
            for (Object serviceBean : serviceBeanMap.values()) {
                if (serviceBean instanceof IJobHandler) {
                    String name = serviceBean.getClass().getAnnotation(JobHandler.class).value();
                    IJobHandler handler = (IJobHandler) serviceBean;
                    if (loadJobHandler(name) != null) {
                        throw new RuntimeException("xxl-job jobhandler[" + name + "] naming conflicts.");
                    }
                    registJobHandler(name, handler);
                }
            }
        }
    }*/

    @SuppressWarnings("all")
    private void initJobHandlerMethodRepository(ApplicationContext applicationContextParam) {
        if (applicationContextParam == null) {
            return;
        }
        // init job handler from method
        String[] beanDefinitionNames = applicationContextParam.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {

            // get bean
            Object bean = null;
            Lazy onBean = applicationContextParam.findAnnotationOnBean(beanDefinitionName, Lazy.class);
            if (onBean != null) {
                LOGGER.debug("xxl-job annotation scan, skip @Lazy Bean:{}", beanDefinitionName);
                continue;
            } else {
                bean = applicationContextParam.getBean(beanDefinitionName);
            }

            // filter method
            Map<Method, XxlJob> annotatedMethods = null;   // referred to ：org.springframework.context.event.EventListenerMethodProcessor.processBean
            try {
                annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                        new MethodIntrospector.MetadataLookup<XxlJob>() {
                            @Override
                            public XxlJob inspect(Method method) {
                                return AnnotatedElementUtils.findMergedAnnotation(method, XxlJob.class);
                            }
                        });
            } catch (Throwable ex) {
                LOGGER.error("xxl-job method-jobhandler resolve error for bean[" + beanDefinitionName + "].", ex);
            }
            if (annotatedMethods == null || annotatedMethods.isEmpty()) {
                continue;
            }

            // generate and regist method job handler
            for (Map.Entry<Method, XxlJob> methodXxlJobEntry : annotatedMethods.entrySet()) {
                Method executeMethod = methodXxlJobEntry.getKey();
                XxlJob xxlJob = methodXxlJobEntry.getValue();
                // regist
                registJobHandler(xxlJob, bean, executeMethod);
            }

        }
    }

    // ---------------------- applicationContext ----------------------
    private static ApplicationContext applicationContextValue;

    public static void setValue(ApplicationContext applicationContext) {
        XxlJobSpringExecutor.applicationContextValue = applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        XxlJobSpringExecutor.applicationContext = applicationContext;
        setValue(applicationContext);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContextValue;
    }

    /*
    BeanDefinitionRegistryPostProcessor
    registry.getBeanDefine()
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.registry = registry;
    }
    * */

}
