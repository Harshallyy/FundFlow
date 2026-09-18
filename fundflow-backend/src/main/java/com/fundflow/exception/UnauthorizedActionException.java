package com.fundflow.exception;

/** Thrown when a user attempts an action on a resource they do not own/control. */
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
