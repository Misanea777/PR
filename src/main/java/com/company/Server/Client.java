package com.company.Server;

import com.company.Fetcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class Client implements Runnable {

    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    public Client(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        while(!clientSocket.isClosed()) {
            String userIn = inputFromClient();
            Logger.getLogger(getClass().getName()).info(Thread.currentThread().getName() + " Requested : " + userIn);
            String response = analyzeAndRespond(userIn);
            outputToClient(response);
            if(response.equals("#exit#")) close();
        }
    }

    public String analyzeAndRespond(String input) {
        if(!input.startsWith("--")) return "Incorect syntax\n";

        if(input.contains("--exit")) return "#exit#";

        String[] inputParts = input.split("\s+");

        if(inputParts[0].equals("--getC") || inputParts[0].equals("--getColumns")) {
            if(inputParts.length == 1) return "Column name must be supplied!\n";
            if(!(inputParts[1].startsWith("\"") && inputParts[1].endsWith("\""))) return "Column name must be in double quotes \"(name)\"!\n";
            return Fetcher.getColumn(inputParts[1].substring(1, inputParts[1].length()-1)) + "\n";
        }

        if(inputParts[0].equals("--getFC") || inputParts[0].equals("--getFromColumn")) {
            if(inputParts.length < 3) return "Column name and pattern must be supplied!\n";
            if(!(inputParts[1].startsWith("\"") && inputParts[1].endsWith("\""))) return "Column name must be in double quotes \"(name)\"!\n";
            if(!(inputParts[2].startsWith("\"") && inputParts[2].endsWith("\""))) return "Pattern must be in double quotes \"(name)\"!\n";
            return Fetcher.getFromColumn(inputParts[1].substring(1, inputParts[1].length()-1), inputParts[2].substring(1, inputParts[2].length()-1)) + "\n";
        }

        return "Command not found!\n";

    }

    private String inputFromClient() {
        String msFromClient = null;
        try {;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            msFromClient = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msFromClient.trim();
    }

    private void outputToClient(String output) {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(output);
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).severe("OutToClientErr - " + e.getMessage());
        }
    }

    private void close() {

        try {
            in.close();
            out.close();
            clientSocket.close();
            Logger.getLogger(getClass().getName()).info(Thread.currentThread().getName() + ": User disconeccted");
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).severe(e.getMessage());
        }
    }
}
