package com.company;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Date;

public class Block {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //our data will be a simple message.
    private long timeStamp;
    private int nonce;

    public Block(String previousHash) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String calulatedHash = StringUtility.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        merkleRoot
        );
        return calulatedHash;
    }

    public void mineBlock(int difficulty) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        merkleRoot = StringUtility.getMerkleRoot(transactions);
        String target = new String(new char[difficulty]).replace('\0', '0');

        while(!hash.substring(0, difficulty).equals(target)){
            nonce++;
            hash = calculateHash();
        }

        System.out.println("Block Mined!! " + hash);
    }

    //Add transactions to this block
    public boolean addTransaction(Transaction transaction) throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        //process transaction and check if valid, unless block is genesis block then ignore.
        if(transaction == null) return false;
        if((previousHash != "0")) {
            if((!transaction.processTransaction())) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }

    public String toString(){
        return hash;
    }
}
