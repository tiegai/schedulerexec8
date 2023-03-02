package com.nike.ncp.scheduler.common.configuration;

import com.nike.wingtips.spring.interceptor.WingtipsClientHttpRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(3 * 1000);
        factory.setReadTimeout(5 * 1000);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add(new WingtipsClientHttpRequestInterceptor(true));
        return restTemplate;
    }
}
