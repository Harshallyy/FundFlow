package com.fundflow.entity;

/**
 * Shared status used by Donation, Payment and TransactionLog.
 * Kept as a single enum since all three move through the same
 * PENDING -> SUCCESS/FAILED lifecycle in the mock payment flow.
 */
public enum TxnStatus {
    PENDING,
    SUCCESS,
    FAILED
}
