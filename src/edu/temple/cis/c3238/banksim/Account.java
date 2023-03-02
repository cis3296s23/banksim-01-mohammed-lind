package edu.temple.cis.c3238.banksim;

/**
 * @author Cay Horstmann
 * @author Modified by Paul Wolfgang
 * @author Modified by Charles Wang
 * @author Modified by Alexa Delacenserie
 * @author Modified by Tarek Elseify
 */


//this is a test comment for purposes of making a commit to git
public class Account {

    private volatile int balance;
    private final int id;

    volatile int insufficientFunds=0;

    private Bank currentBank;
    public Account(int id, int initialBalance, Bank currentBank) {
        this.id = id;
        this.balance = initialBalance;
        this.currentBank=currentBank;
    }

    public int getBalance() {
        return balance;
    }

    public synchronized boolean withdraw(int amount) {

        synchronized(this) {
            if (amount > balance) {
                insufficientFunds=1;
            }
            //checks to see if amount is greater than the balance, if so changes protected variable to 1
            if(insufficientFunds == 0){
                //all for withdraw if sufficient funds
                int currentBalance = balance;
                // Thread.yield(); // Try to force collision
                int newBalance = currentBalance - amount;
                balance = newBalance;
                return true;
            }
            return false;
            //funds were not sufficient
        }
    }

    public synchronized void deposit(int amount) {
        int currentBalance = balance;
        // Thread.yield();   // Try to force collision
        int newBalance = currentBalance + amount;
        balance = newBalance;
        notifyAll();
        //wake all threads
    }
    
    @Override
    public String toString() {
        return String.format("Account[%d] balance %d", id, balance);
    }
}
