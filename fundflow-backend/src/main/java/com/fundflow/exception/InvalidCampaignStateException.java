package com.fundflow.exception;

/** Thrown when an action is attempted against a campaign in an incompatible status. */
public class InvalidCampaignStateException extends RuntimeException {
    public InvalidCampaignStateException(String message) {
        super(message);
    }
}
