package com.company;

public class TransactionInput {
    public String transactionOutputId;       // transactionID
    public TransactionOutput UTXO;          // Unspent Transaction Output

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}
