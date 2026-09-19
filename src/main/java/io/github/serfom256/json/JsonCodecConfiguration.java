package io.github.serfom256.json;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JsonCodecConfiguration {

    @Bean
    @ConditionalOnMissingBean(Serializer.class)
    @ConditionalOnClass(name = "tools.jackson.databind.json.JsonMapper")
    public Serializer jackson3JsonCodec() {
        return new Jackson3JsonCodec();
    }

    @Bean
    @ConditionalOnMissingBean(Serializer.class)
    @ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
    @ConditionalOnMissingClass("tools.jackson.databind.json.JsonMapper")
    public Serializer jackson2JsonCodec() {
        return new Jackson2JsonCodec();
    }
}
