package edu.temple.cis.c3238.banksim;
import java.util.concurrent.locks.ReentrantLock;
//needed for ReentrantLock; the lock used for thread protection
import java.util.concurrent.locks.Condition;
//added to support creating a new test thread
import java.util.concurrent.atomic.AtomicInteger;
//added to fix problem with integer not being Thread Safe; atomic Integer should be a Thread Safe Equivalent
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
    public boolean testing;
    AtomicInteger counter;
    private volatile boolean closed;

    public Bank(int numAccounts, int initialBalance) {
        this.initialBalance = initialBalance;
        this.numAccounts = numAccounts;
        accounts = new Account[numAccounts];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account(i, initialBalance, this);
        }
        numTransactions = 0;
        counter= new AtomicInteger();
        testing=false;
        closed=false;

    }

    private final ReentrantLock transferLock = new ReentrantLock();
    //lock for Transfer; locking @ transfer level
    private final Condition transferCompleted = transferLock.newCondition();
    //condition to signal transfer is complete

    public void transfer(int from, int to, int amount) throws InterruptedException {
        transferLock.lock();
        //acquire lock for transfer
        try {
            while (testing){
                // Wait for testing to finish
                transferCompleted.await();
            }
            incCounter();
            //increment transfer Counter
            if (accounts[from].withdraw(amount)) {
                accounts[to].deposit(amount);
                //withdraw and deposit "amount" done this way to only deposit when withdraw is successful
            }
            decCounter();
            //decrement counter after transaction
            transferCompleted.signalAll(); // Notify waiting threads
            //notify waiting threads that transfer is completed
        } finally {
            transferLock.unlock();
            //release the lock for other transfers
        }
    }

    public void runTests() throws InterruptedException {
        transferLock.lock();
        //acquire lock for testing
        try {
            testing = true;
            //testing flag
            while (counter.get() > 0) {
                // Wait for transfers to finish
                transferCompleted.await();
            }
            TestThread test = new TestThread();
            //create new test thread
            test.start();
            test.runTest(this);
            //run test using created thread
            testing = false;
            //test flag set to false; should happen after testing finished
            transferCompleted.signalAll(); // Notify waiting threads
            //notify threads that testing done
        } finally {
            transferLock.unlock();
            //release the lock obtained for testing
        }
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
            //if we get this statement, race condition NOT RESOLVED
            System.exit(0);
        } else {
            System.out.printf("%-30s Total balance unchanged.\n", Thread.currentThread().toString());
            //this message is a "success" NOTE: Run the program multiple times; race conditions may only become apparent after multiple runs
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
            //close bank
        }
        for (Account account : accounts) {
            synchronized (account) {
                account.notifyAll();
            }
        }
    }


    public boolean isOpen() {
        return !closed;
    }
    //closes bank using volatile boolean
    // Test thread
    private class TestThread extends Thread {
        public void runTest(Bank b) {
            if (b.isOpen()) {
                b.test();
            }
        }
    }
}