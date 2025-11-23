package com.locallend.locallend.command.core;

/**
 * Marker interface for all commands in the system.
 * Commands represent intent to perform an operation.
 *
 * @param <R> The type of result this command produces
 */
public interface Command<R> {
    // Marker interface - commands contain only data, no behavior
}