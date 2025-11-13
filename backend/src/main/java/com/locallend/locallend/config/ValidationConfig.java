package com.locallend.locallend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.Validator;

/**
 * Configuration for Jakarta Bean Validation (JSR-380).
 * Ensures validation annotations on entities and DTOs are processed.
 * Issue #29: Production-ready validation support
 */
@Configuration
public class ValidationConfig {

    /**
     * Configure validator factory bean.
     * Enables @Valid, @NotBlank, @Size, @Email, @Pattern, etc. annotations
     * @return LocalValidatorFactoryBean instance
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
    
    /**
     * Provide Validator bean for programmatic validation.
     * Can be injected into services for manual validation if needed.
     * @return Validator instance
     */
    @Bean
    public Validator validatorBean() {
        return validator();
    }
}
