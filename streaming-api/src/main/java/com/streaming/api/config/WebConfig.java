package com.streaming.api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(MediaProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final MediaProperties media;
    private final String corsOrigin;

    public WebConfig(MediaProperties media, @Value("${cors.origin:*}") String corsOrigin) {
        this.media = media;
        this.corsOrigin = corsOrigin;
    }

    @PostConstruct
    void ensureMediaDirectories() throws IOException {
        Files.createDirectories(media.uploads());
        Files.createDirectories(media.output());
    }

    /** HLS is just static files: serve MEDIA_ROOT/output over plain HTTP. */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/output/**")
                .addResourceLocations("file:" + media.output().toAbsolutePath() + "/")
                .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins(corsOrigin);
    }

    /** Players are picky: .m3u8 and .ts need the right Content-Type. */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> hlsMimeTypes() {
        return factory -> {
            MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
            mappings.add("m3u8", "application/vnd.apple.mpegurl");
            mappings.add("ts", "video/mp2t");
            factory.setMimeMappings(mappings);
        };
    }
}
