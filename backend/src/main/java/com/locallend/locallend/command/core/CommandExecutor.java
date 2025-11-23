package com.locallend.locallend.command.core;

import com.locallend.locallend.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Central command executor service that coordinates command handling.
 * Implements the Command Pattern to decouple request from execution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommandExecutor {

    private final ApplicationContext applicationContext;
    private final Map<Class<?>, CommandHandler<?, ?>> handlerRegistry = new HashMap<>();

    @Value("${feature.command-pattern.enabled:true}")
    private boolean commandPatternEnabled;

    @Value("${feature.command-pattern.log-execution:true}")
    private boolean logCommandExecution;

    /**
     * Initialize the command handler registry by discovering all handlers.
     */
    @PostConstruct
    public void initializeHandlers() {
        if (!commandPatternEnabled) {
            log.info("Command pattern is disabled via configuration");
            return;
        }

        // Auto-discover all command handlers in the application context
        Map<String, CommandHandler> handlers = applicationContext.getBeansOfType(CommandHandler.class);

        for (CommandHandler<?, ?> handler : handlers.values()) {
            Class<?> commandType = handler.getCommandType();
            if (commandType != null) {
                handlerRegistry.put(commandType, handler);
                log.info("Registered command handler: {} for command type: {}",
                    handler.getClass().getSimpleName(), commandType.getSimpleName());
            }
        }

        log.info("Command executor initialized with {} handlers", handlerRegistry.size());
    }

    /**
     * Executes a command with the appropriate handler.
     *
     * @param command The command to execute
     * @param context The execution context
     * @param <R> The type of result
     * @return The command result
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public <R> CommandResult<R> execute(Command<R> command, CommandContext context) {
        if (!commandPatternEnabled) {
            throw new BusinessException("Command pattern is disabled");
        }

        long startTime = System.currentTimeMillis();
        String correlationId = context.getCorrelationId() != null ?
            context.getCorrelationId() : UUID.randomUUID().toString();

        try {
            // Log command execution start
            if (logCommandExecution) {
                log.info("Executing command: {} | User: {} | CorrelationId: {}",
                    command.getClass().getSimpleName(), context.getUserId(), correlationId);
            }

            // Find handler for command
            CommandHandler<Command<R>, R> handler =
                (CommandHandler<Command<R>, R>) handlerRegistry.get(command.getClass());

            if (handler == null) {
                throw new BusinessException("No handler found for command: " +
                    command.getClass().getSimpleName());
            }

            // Execute command
            R result = handler.handle(command, context);

            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;

            // Log successful execution
            if (logCommandExecution) {
                log.info("Command executed successfully: {} | Time: {}ms | CorrelationId: {}",
                    command.getClass().getSimpleName(), executionTime, correlationId);
            }

            // Return successful result
            return CommandResult.<R>builder()
                    .success(true)
                    .data(result)
                    .executionTimeMs(executionTime)
                    .metadata(CommandResult.CommandMetadata.builder()
                            .commandType(command.getClass().getSimpleName())
                            .handlerType(handler.getClass().getSimpleName())
                            .userId(context.getUserId())
                            .correlationId(correlationId)
                            .timestamp(LocalDateTime.now())
                            .build())
                    .build();

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            // Log error
            log.error("Command execution failed: {} | Error: {} | Time: {}ms | CorrelationId: {}",
                command.getClass().getSimpleName(), e.getMessage(), executionTime, correlationId, e);

            // Return failed result
            return CommandResult.<R>builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .errorCode(e.getClass().getSimpleName())
                    .executionTimeMs(executionTime)
                    .metadata(CommandResult.CommandMetadata.builder()
                            .commandType(command.getClass().getSimpleName())
                            .userId(context.getUserId())
                            .correlationId(correlationId)
                            .timestamp(LocalDateTime.now())
                            .build())
                    .build();
        }
    }

    /**
     * Executes a command with automatic context creation from current user.
     *
     * @param command The command to execute
     * @param userId The ID of the user executing the command
     * @param <R> The type of result
     * @return The command result
     */
    public <R> CommandResult<R> execute(Command<R> command, String userId) {
        CommandContext context = CommandContext.builder()
                .userId(userId)
                .correlationId(UUID.randomUUID().toString())
                .build();
        return execute(command, context);
    }

    /**
     * Checks if the command pattern is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return commandPatternEnabled;
    }

    /**
     * Gets the number of registered handlers.
     *
     * @return The handler count
     */
    public int getHandlerCount() {
        return handlerRegistry.size();
    }
}