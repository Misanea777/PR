package com.company;


import com.company.Server.TcpServer;
import com.company.customThreads.CustomThreadPool;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class Main {

    private static final String BASIC_URI = "http://localhost:5000";

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

        Navigator navigator = Navigator.register();
        navigator.fetchAllData();

        //Fetcher.printDataBlocks();

        Fetcher.merge();
        System.out.println();
//        while(true) {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//            String name = reader.readLine();
//            if(name.compareTo("ex") == 0) return;
//            System.out.println(Fetcher.getFromColumn("id", name));
//        }

        TcpServer t = new TcpServer(5001, new CustomThreadPool(5,5,5));
        t.start();
    }



}
