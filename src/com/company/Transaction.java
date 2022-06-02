package com.company;

import java.security.*;
import java.util.ArrayList;

public class Transaction {

    public String transactionId;        // hash of the transaction
    public PublicKey sender;            // sender's address or public key
    public PublicKey receiver;          // receiver's address or public key
    public float value;                 // transaction amount
    public byte[] signature;            // this is to prevent anybody else from spending funds from the wallet

    private static int sequence = 0;    // rough count for number of transactions

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    public Transaction(PublicKey sender, PublicKey receiver, float value, ArrayList<TransactionInput> inputs) {
        this.sender = sender;
        this.receiver = receiver;
        this.value = value;
        this.inputs = inputs;
    }

    public String calculateHash() throws NoSuchAlgorithmException {
        sequence++;         // increase the count to avoid 2 identical transactions having same hash
        return StringUtility.applySha256(
                StringUtility.getStringFromKey(sender) +
                        StringUtility.getStringFromKey(receiver) +
                        Float.toString(value) +
                        sequence
        );
    }

    public void generateSignature(PrivateKey privateKey) throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        String data = StringUtility.getStringFromKey(sender) + StringUtility.getStringFromKey(receiver) + Float.toString(value);
        signature = StringUtility.applyECDSASig(privateKey, data);
    }

    public boolean verifySignature() throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        String data = StringUtility.getStringFromKey(sender) + StringUtility.getStringFromKey(receiver) + Float.toString(value);
        return StringUtility.verifyECDSASig(sender, data, signature);
    }

    // returns true if new transaction could be created
    public boolean processTransaction() throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {

        if(!verifySignature()){
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        // gather transaction inputs (Make sure they are unspent):
        for(TransactionInput i : inputs){
            i.UTXO = BlockChain.UTXOs.get(i.transactionOutputId);
        }

        // check if transaction is valid
        if(getInputsValue() < BlockChain.minimumTransaction){
            System.out.println("#Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        // generate transaction outputs
        float leftOver = getInputsValue() - value;  // get value if inputs then the leftover change
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.receiver, value, transactionId));    // send value to receiver
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));   // send the left over 'change' back to sender

        // add the outputs to Unspent list
        for(TransactionOutput o: outputs){
            BlockChain.UTXOs.put(o.id, o);
        }

        // remove transaction inputs from UTXO lists as spent
        for(TransactionInput i : inputs){
            if(i.UTXO == null) continue;    // if transaction can't be found skip it
            BlockChain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    // returns sum of inputs (UTXOs) value
    public float getInputsValue(){
        float total = 0;
        for (TransactionInput i: inputs){
            if(i.UTXO == null) continue;
            total += i.UTXO.value;
        }

        return total;
    }

    // returns sum of outputs
    public float getOutputsValue(){
        float total = 0;
        for (TransactionOutput o: outputs){
            total += o.value;
        }

        return total;
    }
}
