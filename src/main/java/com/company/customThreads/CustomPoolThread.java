package com.company.customThreads;

import java.util.concurrent.BlockingQueue;

public class CustomPoolThread extends Thread {
    private BlockingQueue taskQueue = null;
    private boolean isRunning = true;

    public CustomPoolThread(BlockingQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    public void run() {
        while(isRunning) {
            try {
                Runnable task = (Runnable) taskQueue.take();
                task.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void stopRunning() {
        isRunning = false;
        this.interrupt();
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }


}
