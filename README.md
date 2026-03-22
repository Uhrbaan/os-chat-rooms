# Project 1: Chat Rooms 
Exercise done for the course "Operating Systems".

To run the full project (client AND server), run 
```sh 
./gradlew run 
``` 
To only run the client, provide the `:client` task, and for the server, the `:server` task.

You may connect to a remote server by setting the `OS_CHAT_ROOMS_SERVER_IP` environment variable, like this: 
```sh 
OS_CHAT_ROOMS_SERVER_IP=127.0.0.1 ./gradlew :client
```

This will start the client and tell it to connect to the server on `127.0.0.1` (also know as localhost, so the server running on the same machine).

If you use windows, you should run: 
```powershell 
set "OS_CHAT_ROOMS_SERVER_IP=127.0.0.1"
.\gradlew run|client|server
```