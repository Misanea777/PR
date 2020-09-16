package com.company;


import javax.inject.Singleton;
import java.io.IOException;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class Main {
    @Singleton
    private static final String BASIC_URI = "http://localhost:5000";
    public static List<DataBlock> dataBlocks = new ArrayList<DataBlock>();
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

        Navigator navigator = Navigator.register();
        HttpResponse<String> response = navigator.navigateSync(navigator.getLink());
        navigator.navigateLinksAsync(Fetcher.fetch(navigator.getLink(), response.body()));

        System.out.println();
        for (DataBlock dataBlock: dataBlocks) {
            System.out.println(dataBlock);
        }
    }



}
