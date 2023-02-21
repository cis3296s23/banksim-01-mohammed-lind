package edu.temple.cis.c3238.banksim;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * @author Cay Horstmann
 * @author Modified by Paul Wolfgang
 * @author Modified by Charles Wang
 * @author Modified by Alexa Delacenserie
 * @author Modified by Tarek Elseify
 */


public class Bank {

    public static final int NTEST = 10;
    private final Account[] accounts;
    private long numTransactions = 0;
    private final int initialBalance;
    private final int numAccounts;

    AtomicInteger counter;
    private boolean closed;

    public Bank(int numAccounts, int initialBalance) {
        this.initialBalance = initialBalance;
        this.numAccounts = numAccounts;
        accounts = new Account[numAccounts];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account(i, initialBalance);
        }
        numTransactions = 0;
    }

    public void transfer(int from, int to, int amount) {
        ReentrantLock transferLock = new ReentrantLock();
        boolean completed = false;
        //lock to allow threads to run mutually exclusively and reenter multiple times
        while (!completed) {
            //loops until transaction is completed successfully
            if (transferLock.tryLock()) {
                //if lock available
                if (accounts[from].withdraw(amount)) {
                    //done this way to call withdraw and make sure deposit only done when there is a withdraw.
                    accounts[to].deposit(amount);
                    completed = true;
                    //transaction complete
                }
                transferLock.unlock();
                //free the lock up for another thread.
            }
        }

        //This line of code is for testing, currently uncommented for testing.
        if (shouldTest()) test();
    }

    public void test() {
        int totalBalance = 0;
        for (Account account : accounts) {
            System.out.printf("%-30s %s%n",
                    Thread.currentThread().toString(), account.toString());
            totalBalance += account.getBalance();
        }
        System.out.printf("%-30s Total balance: %d\n", Thread.currentThread().toString(), totalBalance);
        if (totalBalance != numAccounts * initialBalance) {
            System.out.printf("%-30s Total balance changed!\n", Thread.currentThread().toString());
            System.exit(0);
        } else {
            System.out.printf("%-30s Total balance unchanged.\n", Thread.currentThread().toString());
        }
    }

    public int getNumAccounts() {
        return numAccounts;
    }


    public boolean shouldTest() {
        return ++numTransactions % NTEST == 0;
    }

    // Increments the atomic counter
    synchronized void incCounter() {
        counter.getAndIncrement();
    }

    // Decrements the atomic counter
    synchronized void decCounter() {
        counter.getAndDecrement();
    }

    // Returns the number of transactions
    public long getNumTransactions() {
        return numTransactions;
    }

    // Closes bank
    void closeBank() {
        synchronized (this) {
            closed = true;
        }
        for (Account account : accounts) {
            synchronized (account) {
                account.notifyAll();
            }
        }
    }
}