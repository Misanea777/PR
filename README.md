# Topic: Lab 1 "Concurrent TCP server"
### Author: Filipescu Mihail
#### Group: FAF-181

## Objectives:
1. Pull a docker container (alexburlacu/pr-server) from the registry
2. Run it, don't forget to forward the port 5000 to the port that you want on the local machine
3. Only languages and libraries supporting threads, locks, and semaphores are allowed. Node or JS generally and Elixir/Erlang are prohibited. Go and Rust are allowed but with some constraints. Discuss with me the implementation.
4. Now that you're up and running, you need to access the root route of the server and find your way to /register
5. The access token that you get after accessing the /register route must be put in a HTTP header of the subsequent requests under the key X-Access-Token key
6. Most routes return a JSON with data, data type, and link keys. Extract data from data key and get next links from link key
7. Hardcoding the routes is strictly forbidden. You need to "traverse" the API
8. Access token has a timeout of 20 seconds, and you are not allowed to get another token every time you access a different route. So, one register per program run
9. Once you fetch all the data, convert it to a common representation, doesn't matter what this representation is
10. The final part of the lab is to make a concurrent TCP server, serving the fetched content, that will respond to (mandatory) a column selector message, like `SelectColumn column_name`, and (optional) `SelectFromColumn column_name glob_pattern`

## Implemantation:
### Route traversing:
Firstly navigate to the /register and getting the X-Access-Token key.
Now we are free to traverse all the routes that are found with recursive method. Each call of this method is executed in a new thread, that is created by **CashedThreadPool**, wich is more suited for I/O opperations.
```
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
```

After there are no more routes to traverse, the obtained data is converted and then aggregated in a **JsonArray** with the unique Id tahat is either **email** or **full_name**.
#### Conversion:
```
    ...
    public static JSONArray fromJsonStringToJson(String data) {
        return new JSONArray(data);
    }

    public static JSONArray fromXmlStringToJson(String data) {
        return XML.toJSONObject(data).getJSONObject("dataset").getJSONArray("record");
    }

    public static JSONArray fromCsvStringToJson(String data) throws IOException {
        CsvSchema csv = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        MappingIterator<Map<?, ?>> mappingIterator =  csvMapper.reader().forType(Map.class).with(csv).readValues(data);
        List<Map<?, ?>> list = mappingIterator.readAll();
        return new JSONArray(list);
    }

    public static JSONArray fromYamlStringToJson(String data) throws JsonProcessingException {
        Object obj = new ObjectMapper(new YAMLFactory()).readValue(data, Object.class);
        String res = new ObjectMapper().writeValueAsString(obj);
        return new JSONArray(res);
    }
    ...
```

### TCP server:
Now the TCP server is created and initiated by suppling the port that the server will listen on, and **CustomThreadPool** that will be explained later.
```
        ...
        TcpServer t = new TcpServer(5001, new CustomThreadPool(5,10,15));
        t.start();
        ...
```

As soon as the user connects to the server, the connection will be given to the threadPooll to be managed. In a such a way multiple users can be connected at the same time to the server. 
The number of users is only limited by the threadPool.
```
    public void start() {
        ...
        while(run) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                Logger.getLogger(getClass().getName()).info("New Client Connected! " + clientSocket.getPort());
                threadPool.execute(new Client(clientSocket));
            } catch (IOException e) {
                Logger.getLogger(getClass().getName()).severe(e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ...
    }
```
The **Client class** extedns **Runnable** and holds all methods necessary for user-server connection.

Users have access to 3 commands: **--getColumn, --getFromColumn and --exit**.

### Custom thread pool:
The implemented thread pool needs **noOfCoreThreads** AKA initial & optimal nr of threds, **noOfMaxThreads** which is the upper limit of threds number and lastly **noOfMaxTasks** that is the maximum number of tasks to be queued.
#### Constructor:
```
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
```

The task can be submited to the pool via **execute()** method. The pool will automaticaly increase the number of threads (if it is able to) if all existing threads are busy. 

Threads can be added and deleted after the initialization of the pool. However the **delete()** method has no use for now (it is added for future **delete after being inactive for too long**).
```
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
```

## Connection to the server:
As a single user you can connect using the **Client-side** implementation:
```
public class Main {

    public  static void main(String[] args) {
        Socket clientSocket;

        try {
            clientSocket = new Socket("localhost", 5001);
            System.out.println("Connected to server!");
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while(!clientSocket.isClosed()) {
                outToServer.writeBytes(new Scanner(System.in).nextLine() + '\n');
                String line;
                while((line = inFromServer.readLine()) != null) {
                    if (line.isEmpty()) {
                        break;
                    }
                    if(line.equals("#exit#")) System.exit(0);
                    System.out.println(line.replace("||", "\n"));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

## Results:
### Client side:
![Output](https://github.com/Misanea777/PR/blob/master/src/main/ScreenShots/sv1.png)
![Output](https://github.com/Misanea777/PR/blob/master/src/main/ScreenShots/sv2.png)
![Output](https://github.com/Misanea777/PR/blob/master/src/main/ScreenShots/sv3.png)
### Server side:
![Output](https://github.com/Misanea777/PR/blob/master/src/main/ScreenShots/sv4.png)
