package com.ProjectGraduation.file;
import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dcwnj8pxv");
        config.put("api_key", "192299162465914");
        config.put("api_secret", "e3ugmenvY4FTsjofJ3YtE5S4cCA");
        return new Cloudinary(config);
    }
}

