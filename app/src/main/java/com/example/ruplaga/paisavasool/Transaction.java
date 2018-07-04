package com.example.ruplaga.paisavasool;

/**
 * Created by ruplaga on 6/25/2018.
 */

enum TransactionType {
    Income,
    Expense
}
public class Transaction {

    String transactionId;
    TransactionType transactionType;
    Float transactionAmount;
    String transactionModeOfPayment;
    String transactionCategory;
    Long transactionDate;
    String transactionNotes;

    public Transaction() {
    }

    public Transaction(String transactionId, TransactionType transactionType, Float transactionAmount, String transactionModeOfPayment, String transactionCategory, long transactionDate, String transactionNotes) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.transactionAmount = transactionAmount;
        this.transactionModeOfPayment = transactionModeOfPayment;
        this.transactionCategory = transactionCategory;
        this.transactionDate = transactionDate;
        this.transactionNotes = transactionNotes;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public Float getTransactionAmount() {
        return transactionAmount;
    }

    public String getTransactionModeOfPayment() {
        return transactionModeOfPayment;
    }

    public String getTransactionCategory() {
        return transactionCategory;
    }

    public long getTransactionDate() {
        return transactionDate;
    }

    public String getTransactionNotes() {
        return transactionNotes;
    }
}
