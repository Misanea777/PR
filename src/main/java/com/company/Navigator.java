package com.company;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.text.html.HTMLDocument;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Navigator {
    private String access_token;
    private String link;
    private static final String BASIC_URI = "http://localhost:5000";

    private HttpClient client = HttpClient.newHttpClient();
    private HttpRequest request;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public Navigator() {
    }

    public Navigator(String access_token, String link) {
        this.access_token = access_token;
        this.link = link;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "Navigator{" +
                "access_token='" + access_token + '\'' +
                ", link='" + link + '\'' +
                '}';
    }

    public CompletableFuture<Void> navigateAsync(String link) throws ExecutionException, InterruptedException {
        client = HttpClient.newBuilder()
                .executor(executorService) // using cashed thred pool
                .build();
        request = HttpRequest.newBuilder()
                .GET()
                .header("X-Access-Token", this.access_token)
                .uri(URI.create(BASIC_URI + link))
                .build();
        CompletableFuture<Void> resp =  client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    try {
                        navigateLinksAsync(Fetcher.fetch(link, body));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    return body;
                })
                .thenAccept(body -> {
                    System.out.println(link );
                });

        return resp;
    }

    public  HttpResponse<String> navigateSync(String link) throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder()
                .GET()
                .header("X-Access-Token", this.access_token)
                .uri(URI.create(BASIC_URI + link))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }


    public static Navigator register() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASIC_URI + "/register"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        Navigator navigator = mapper.readValue(response.body(), Navigator.class);
        return navigator;
    }

    public void navigateLinksAsync(Iterator<JsonNode> links) throws ExecutionException, InterruptedException {
        List<CompletableFuture<Void>> responses = new ArrayList<CompletableFuture<Void>>();
        while (links.hasNext()) {
            responses.add(navigateAsync(links.next().toString().replace("\"", "")));
        }
        for (CompletableFuture<Void> response: responses) {
            response.join();
        }
    }


}
