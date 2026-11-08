package pe.edu.upeu.PharmaBackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Logger log =
            LoggerFactory.getLogger(CacheConfig.class);

    // Spring Data Redis 4 difiere escrituras por defecto. La guía exige
    // que el siguiente listado ya refleje una creación, actualización o venta.
    @Bean
    public RedisCacheManagerBuilderCustomizer escriturasInmediatas(RedisConnectionFactory connectionFactory) {
        return builder -> builder.cacheWriter(RedisCacheWriter.create(
                connectionFactory, config -> config.immediateWrites()));
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException ex,
                                            Cache cache, Object key) {
                log.warn("Cache {} no disponible al leer {} | {}",
                        cache.getName(), key, ex.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException ex,
                                            Cache cache, Object key,
                                            Object value) {
                log.warn("Cache {} no disponible al guardar {} | {}",
                        cache.getName(), key, ex.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException ex,
                                              Cache cache, Object key) {
                log.warn("Cache {} no disponible al invalidar {} | {}",
                        cache.getName(), key, ex.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException ex,
                                              Cache cache) {
                log.warn("Cache {} no disponible al limpiar | {}",
                        cache.getName(), ex.getMessage());
            }
        };
    }
}
