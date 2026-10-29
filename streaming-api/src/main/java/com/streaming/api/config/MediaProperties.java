package com.streaming.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "media")
public record MediaProperties(Path root) {

    public Path uploads() { return root.resolve("uploads"); }

    public Path output() { return root.resolve("output"); }
}
