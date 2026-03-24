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
$Env:OS_CHAT_ROOMS_SERVER_IP = "127.0.0.1"
.\gradlew run|client|server
```

> Note: You should also disable your firewall while testing out the app for simplicity. **Please don't forget to re-enable it after !**

> Note: If like me you are running tailscale, Java by default will pick its local address over your real local address. This will cause issues. I recommend disableing the tailscale interface temporarily with: 
```sh
sudo tailscale down
```

> Note that the instructions state the following: "Notice here that clients, the chat server manager, as well as the chat servers are all independent processes. This means that they may run on different JVMs. They communicate through client-server interaction.".
> 
> This might imply that java should start the ChatServer as a separate process, rather than simply instanciating it in the ChatServerManager. 
> Since this seems to be a wierd thing to do, I've created a separate branch for this purpose.