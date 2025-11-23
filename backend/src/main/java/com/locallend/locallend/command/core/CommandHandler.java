package com.locallend.locallend.command.core;

/**
 * Interface for command handlers that process specific command types.
 * Implements the Command Pattern to encapsulate business logic.
 *
 * @param <C> The type of command this handler processes
 * @param <R> The type of result produced by handling the command
 */
public interface CommandHandler<C extends Command<R>, R> {

    /**
     * Handles the execution of a command.
     *
     * @param command The command to handle
     * @param context The execution context containing user info and metadata
     * @return The result of command execution
     */
    R handle(C command, CommandContext context);

    /**
     * Gets the type of command this handler can process.
     *
     * @return The command class type
     */
    Class<C> getCommandType();
}