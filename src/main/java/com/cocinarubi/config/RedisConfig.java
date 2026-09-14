package com.cocinarubi.config;

import java.time.Duration;
import java.util.Map;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Configura Redis como proveedor de caché con TTL diferenciado por nombre de caché.
 * Capa: Config — infraestructura de caché.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> configs = Map.of(
            "menu-web", defaults.entryTtl(Duration.ofMinutes(3))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults.entryTtl(Duration.ofMinutes(5)))
                .withInitialCacheConfigurations(configs)
                .build();
    }
}
