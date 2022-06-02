package com.company;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain {

    public static ArrayList<Block> blockChain = new ArrayList<>();
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();    //list of all unspent transactions.

    public static int difficulty = 2;
    public static float minimumTransaction = 0.1f;
    public static Wallet walletA;
    public static Wallet walletB;
    public static Transaction genesisTransaction;

    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException, SignatureException, InvalidKeyException {
        // Setup BouncyCastle as security provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // Create two wallets
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        // create genesis transaction, which sends 100 SanCoins to WalletA
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);      // manually sign the genesis transaction
        genesisTransaction.transactionId = "0";                         // manually set the transaction id
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.receiver, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.

        System.out.println("Creating and Mining Genesis block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        //testing
        Block block1 = new Block(genesis.hash);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
        addBlock(block1);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        addBlock(block2);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        System.out.println("\nBlockChain is Valid: " + isChainValid());

       

    public static boolean isChainValid() throws UnsupportedEncodingException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        for (int i = 1; i < blockChain.size(); i++) {
            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i-1);

            // compare registered hash and calculated hash
            if(!currentBlock.hash.equals(currentBlock.calculateHash())){
                System.out.println("Current Block Hash not equal " + i);
                return false;
            }

            // compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(previousBlock.calculateHash())){
                System.out.println("Previous Block Hash not equal " + i);
                return false;
            }

            // check if hash is solved
            if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)){
                System.out.println("This block is not mined " + i);
                return false;
            }

            //loop thru blockchains transactions:
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for(TransactionInput input: currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if( currentTransaction.outputs.get(0).receiver != currentTransaction.receiver) {
                    System.out.println("#Transaction(" + t + ") output receiver is not who it should be");
                    return false;
                }
                if( currentTransaction.outputs.get(1).receiver != currentTransaction.sender) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }
            }
        }
        return true;
    }

    public static void addBlock(Block newBlock) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        newBlock.mineBlock(difficulty);
        blockChain.add(newBlock);
    }
}
