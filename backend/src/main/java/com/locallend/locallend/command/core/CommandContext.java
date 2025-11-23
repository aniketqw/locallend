package com.locallend.locallend.command.core;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Context information for command execution.
 * Contains metadata about who is executing the command and when.
 */
@Getter
@Builder
public class CommandContext {

    /**
     * The ID of the user executing the command
     */
    private final String userId;

    /**
     * The username of the user executing the command
     */
    private final String username;

    /**
     * When the command execution started
     */
    @Builder.Default
    private final LocalDateTime executionTime = LocalDateTime.now();

    /**
     * Correlation ID for tracking related commands
     */
    private final String correlationId;

    /**
     * Additional metadata for command execution
     */
    @Builder.Default
    private final Map<String, Object> metadata = new HashMap<>();

    /**
     * Gets a metadata value by key.
     *
     * @param key The metadata key
     * @return Optional containing the value if present
     */
    public Optional<Object> getMetadata(String key) {
        return Optional.ofNullable(metadata.get(key));
    }

    /**
     * Gets a metadata value as a specific type.
     *
     * @param key The metadata key
     * @param type The expected type class
     * @param <T> The type to cast to
     * @return Optional containing the typed value if present and castable
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    /**
     * Creates a new context with additional metadata.
     *
     * @param key The metadata key
     * @param value The metadata value
     * @return A new context with the added metadata
     */
    public CommandContext withMetadata(String key, Object value) {
        Map<String, Object> newMetadata = new HashMap<>(this.metadata);
        newMetadata.put(key, value);
        return CommandContext.builder()
                .userId(this.userId)
                .username(this.username)
                .executionTime(this.executionTime)
                .correlationId(this.correlationId)
                .metadata(newMetadata)
                .build();
    }
}