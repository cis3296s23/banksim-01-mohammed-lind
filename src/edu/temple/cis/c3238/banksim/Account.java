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
     private Bank bankaccount;
    public Account(int id, int initialBalance,Bank bankaccount) {
        this.id = id;
        this.balance = initialBalance;
        this.bankaccount = bankaccount;
    }

    public int getBalance() {
        return balance;
    }

    public boolean withdraw(int amount) {
        if (amount <= balance) {
            int currentBalance = balance;
            // Thread.yield(); // Try to force collision
            int newBalance = currentBalance - amount;
            balance = newBalance;
            return true;
        } else {
            return false;
        }
    }

    public void deposit(int amount) {
        int currentBalance = balance;
        // Thread.yield();   // Try to force collision
        int newBalance = currentBalance + amount;
        balance = newBalance;
    }
    
    @Override
    public String toString() {
        return String.format("Account[%d] balance %d", id, balance);
    }
}
