package com.company.Server;

import com.company.customThreads.CustomThreadPool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class TcpServer {

    private ServerSocket serverSocket;
    private int port;
    private boolean run;
    private CustomThreadPool threadPool;

    public TcpServer(int port, CustomThreadPool threadPool) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(this.port);
        this.threadPool = threadPool;
    }

    public void start() {
        this.run = true;
        Logger.getLogger(getClass().getName()).info("Server started and listening at port : " + port);
        while(run) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                Logger.getLogger(getClass().getName()).info("New Client Connected! " + clientSocket.getPort());
                //new Thread(new Client(clientSocket)).start();
                threadPool.execute(new Client(clientSocket));
            } catch (IOException e) {
                Logger.getLogger(getClass().getName()).severe(e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }

    public void stop() {
        this.run = false;
    }

}
