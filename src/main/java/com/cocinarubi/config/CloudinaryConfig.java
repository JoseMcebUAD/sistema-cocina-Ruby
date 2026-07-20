package com.cocinarubi.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//**Clase que se va a levantar en el servidor para que clodinary sepa quien está intentando acceder al clodinary*/
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;
    @Value("${cloudinary.api-key}")
    private String apiKey;
    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", cloudName,   
                "api_key", apiKey,
                "api_secret", apiSecret  
            )
        );
    }
}
