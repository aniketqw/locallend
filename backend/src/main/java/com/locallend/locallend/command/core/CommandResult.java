package com.locallend.locallend.command.core;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result wrapper for command execution.
 * Contains the result data along with execution metadata.
 *
 * @param <T> The type of data returned by the command
 */
@Getter
@Builder
public class CommandResult<T> {

    /**
     * The actual result data from command execution
     */
    private final T data;

    /**
     * Whether the command executed successfully
     */
    @Builder.Default
    private final boolean success = true;

    /**
     * Error message if command failed
     */
    private final String errorMessage;

    /**
     * Error code if command failed
     */
    private final String errorCode;

    /**
     * When the command was executed
     */
    @Builder.Default
    private final LocalDateTime executedAt = LocalDateTime.now();

    /**
     * How long the command took to execute in milliseconds
     */
    private final long executionTimeMs;

    /**
     * Any warnings generated during execution
     */
    @Builder.Default
    private final List<String> warnings = new ArrayList<>();

    /**
     * Command execution metadata
     */
    @Builder.Default
    private final CommandMetadata metadata = CommandMetadata.builder().build();

    /**
     * Creates a successful result.
     *
     * @param data The result data
     * @param <T> The type of data
     * @return A successful command result
     */
    public static <T> CommandResult<T> success(T data) {
        return CommandResult.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Creates a successful result with execution time.
     *
     * @param data The result data
     * @param executionTimeMs The execution time in milliseconds
     * @param <T> The type of data
     * @return A successful command result with timing
     */
    public static <T> CommandResult<T> success(T data, long executionTimeMs) {
        return CommandResult.<T>builder()
                .success(true)
                .data(data)
                .executionTimeMs(executionTimeMs)
                .build();
    }

    /**
     * Creates a failed result.
     *
     * @param errorMessage The error message
     * @param errorCode The error code
     * @param <T> The type of data (null in this case)
     * @return A failed command result
     */
    public static <T> CommandResult<T> failure(String errorMessage, String errorCode) {
        return CommandResult.<T>builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .data(null)
                .build();
    }

    /**
     * Creates a failed result from an exception.
     *
     * @param exception The exception that caused the failure
     * @param <T> The type of data (null in this case)
     * @return A failed command result
     */
    public static <T> CommandResult<T> failure(Exception exception) {
        return CommandResult.<T>builder()
                .success(false)
                .errorMessage(exception.getMessage())
                .errorCode(exception.getClass().getSimpleName())
                .data(null)
                .build();
    }

    /**
     * Metadata about command execution
     */
    @Getter
    @Builder
    public static class CommandMetadata {
        private final String commandType;
        private final String handlerType;
        private final String userId;
        private final String correlationId;

        @Builder.Default
        private final LocalDateTime timestamp = LocalDateTime.now();
    }
}