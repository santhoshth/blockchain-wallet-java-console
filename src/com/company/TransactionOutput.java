package com.company;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class TransactionOutput {
    public String id;
    public PublicKey receiver;
    public float value;
    public String parentTransactionId;      // Id of the transaction where this output was created in

    public TransactionOutput(PublicKey receiver, float value, String parentTransactionId) throws NoSuchAlgorithmException {
        this.receiver = receiver;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtility.applySha256(StringUtility.getStringFromKey(receiver) + Float.toString(value) + parentTransactionId);
    }

    // check if coins belongs to you
    public boolean isMine(PublicKey publicKey){
        return publicKey == receiver;
    }
}
