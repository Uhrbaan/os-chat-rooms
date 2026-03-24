package os.chat.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import os.chat.client.CommandsFromServer;
import os.chat.client.CommandsFromWindow;
import os.chat.client.NetworkHelper;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;


/**
 * Each instance of this class is a server for one room.
 * <p>
 * At first there is only one room server, and the names of the room available is fixed.
 * <p>
 * Later you will have multiple room server, each managed by its own <code>ChatServer</code>. A
 * {@link ChatServerManager} will then be responsible for creating and adding new rooms.
 */
public class ChatServer implements ChatServerInterface {

	private String roomName;
	private Vector<CommandsFromServer> registeredClients;
	private Registry registry;

	/**
	 * Constructs and initializes the chat room before registering it to the RMI registry.
	 * 
	 * @param roomName the name of the chat room
	 */
	public ChatServer(String roomName) {
		this.roomName = roomName;
		registeredClients = new Vector<CommandsFromServer>();

		try {
			// Create a stub, which acts like a gateway between the actual server object and the
			// network (same as before)
			ChatServerInterface stub =
					(ChatServerInterface) UnicastRemoteObject.exportObject(this, 0);
			registry = LocateRegistry.getRegistry();
			registry.rebind("room_" + roomName, stub);
			System.out.println("ChatServer " + roomName + " was created");
		} catch (RemoteException e) {
			System.out.println("cannot export the object");
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Publishes to all subscribed clients (i.e. all clients registered to a chat room) a message
	 * send from a client.
	 * 
	 * @param message the message to propagate
	 * @param publisher the client from which the message originates
	 */
	public void publish(String message, String publisher) {
		// Have to use a separate list because modifying the registeredClients list while iterating
		// overt will throw an error.
		List<CommandsFromServer> clientsToRemove = new ArrayList<>();

		for (CommandsFromServer c : registeredClients) {
			try {
				c.receiveMsg(roomName, publisher + ": " + message);
			} catch (RemoteException e) {
				System.out
						.println("Message could not be sent to distant " + c + " because of: " + e);
				System.out.println("Assuming client quit unexpectedly. Removing from client list.");
				clientsToRemove.add(c);
			}
		}

		for (CommandsFromServer c : clientsToRemove)
			this.unregister(c);
	}

	/**
	 * Registers a new client to the chat room.
	 * 
	 * @param client the name of the client as registered with the RMI registry
	 */
	public void register(CommandsFromServer client) {
		String clientHost = "";
		try {
			// Get the IP address of the client to print it.
			clientHost = RemoteServer.getClientHost();
		} catch (ServerNotActiveException e) {
			clientHost = "localhost";
		}
		registeredClients.add(client);
		System.out.println("Registered a new client from IP: " + clientHost);
	}

	/**
	 * Unregisters a client from the chat room.
	 * 
	 * @param client the name of the client as registered with the RMI registry
	 */
	public void unregister(CommandsFromServer client) {
		registeredClients.remove(client);
		System.out.println("Unregistered a client.");
	}

	public static void main(String[] args) {
		// The first argument is always the binary being executed.
		System.out.println(args);
		String roomName = args[0];

		// Since ChatServer is a separate process it also has to create a registry.
		try {
			// Start RMI here on default port + 1 because the ChatServerManager is already running
			// on the default port
			LocateRegistry.createRegistry(1099 + 1);
		} catch (RemoteException e) {
			System.out.println("error: can not create registry");
			e.printStackTrace();
		}

		// We also need to advertise the correct ip address
		try {
			// Change RMI localhost to public local IP address
			String myIP = NetworkHelper.getLocalHost();
			System.setProperty("java.rmi.server.hostname", myIP);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Finally we can create an instance
		new ChatServer(roomName);
	}
}
