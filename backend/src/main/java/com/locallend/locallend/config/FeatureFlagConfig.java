package com.locallend.locallend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Feature flag configuration for LocalLend 2.0 architecture.
 * All new patterns are enabled by default.
 */
@Configuration
@ConfigurationProperties(prefix = "feature")
@Getter
@Setter
public class FeatureFlagConfig {

    /**
     * Enable/disable command pattern for booking operations (default: true)
     */
    private boolean useCommandPattern = true;

    /**
     * Enable/disable state pattern for booking lifecycle (default: true)
     */
    private boolean useStatePattern = true;

    /**
     * Enable/disable event-driven architecture (default: true)
     */
    private boolean useEventDriven = true;

    /**
     * Enable logging for command execution
     */
    private CommandPattern commandPattern = new CommandPattern();

    /**
     * Configuration specific to command pattern
     */
    @Getter
    @Setter
    public static class CommandPattern {
        private boolean enabled = true;
        private boolean logExecution = true;
        private int timeoutSeconds = 30;
    }

    /**
     * Checks if the new architecture features are enabled
     */
    public boolean isNewArchitectureEnabled() {
        return useCommandPattern || useStatePattern || useEventDriven;
    }

    /**
     * Gets a summary of enabled features
     */
    public String getEnabledFeaturesSummary() {
        StringBuilder summary = new StringBuilder("Enabled Features: ");
        if (useCommandPattern) summary.append("Command Pattern, ");
        if (useStatePattern) summary.append("State Pattern, ");
        if (useEventDriven) summary.append("Event-Driven, ");

        String result = summary.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }

        return result.equals("Enabled Features: ") ? "No new features enabled" : result;
    }
}