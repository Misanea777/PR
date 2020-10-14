package com.company.customThreads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CustomThreadPool {
    private BlockingQueue taskQueue = null;
    private List<CustomPoolThread> threads = new ArrayList<CustomPoolThread>();
    private boolean isRunning = true;
    private int noOfCoreThreads;
    private int noOfMaxThreads;


    public CustomThreadPool(int noOfCoreThreads, int noOfMaxThreads, int noOfMaxTasks) {
        this.noOfCoreThreads = noOfCoreThreads;
        this.noOfMaxThreads = noOfMaxThreads;
        taskQueue = new LinkedBlockingQueue(noOfMaxTasks);
        for(int i=0; i<noOfCoreThreads; i++) {
            threads.add(new CustomPoolThread(taskQueue));
        }
        for(CustomPoolThread thread : threads) {
            thread.start();
        }

    }

    private synchronized void addThread() {
        if(threads.size() >= noOfMaxThreads) return;
        CustomPoolThread thread = new CustomPoolThread(taskQueue);
        threads.add(thread);
        thread.start();
    }

    private synchronized void deleteThread(int index) {
        if(threads.size() <= 0) return;
        threads.get(index).stopRunning();
        threads.remove(index);
    }

    public synchronized void execute(Runnable task) throws InterruptedException {
        if(!isRunning) throw new IllegalStateException("Threadpool is not running!");
        if(!areFreeThreads()) addThread();
        taskQueue.put(task);
    }

    public synchronized boolean areFreeThreads() {
        for(CustomPoolThread thread : threads) {
            if(thread.getState().compareTo(Thread.State.WAITING) == 0) return true;
        }
        return false;
    }

    public synchronized void chekThreadsState() {
        for(CustomPoolThread thread : threads) {
            System.out.println(thread.getState());
        }
    }

    public synchronized void stop() {
        isRunning = false;
        for(CustomPoolThread thread : threads) {
            thread.stopRunning();
        }
    }
}
