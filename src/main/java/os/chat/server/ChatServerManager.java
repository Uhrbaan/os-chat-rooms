package os.chat.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;
import os.chat.client.ChatClient;
import os.chat.client.NetworkHelper;

/**
 * This class manages the available {@link ChatServer}s and available rooms.
 * <p>
 * At first you should not modify its functionalities but only export them for being called by the
 * {@link ChatClient}.
 * <p>
 * Later you will modify this to allow creating new rooms and looking them up from the
 * {@link ChatClient}.
 */
public class ChatServerManager implements ChatServerManagerInterface {

	/**
	 * NOTE: technically this vector is redundant, since the room name can also be retrieved from
	 * the chat server vector.
	 */
	private Vector<String> chatRoomsList;
	private Vector<Process> chatRooms;

	private static ChatServerManager instance = null;
	private Registry registry;

	/**
	 * Constructor of the <code>ChatServerManager</code>.
	 * <p>
	 * Must register its functionalities as stubs to be called from RMI by the {@link ChatClient}.
	 */
	public ChatServerManager() {
		chatRoomsList = new Vector<String>();
		chatRooms = new Vector<>();

		try {
			// Change RMI localhost to public local IP address
			String myIP = NetworkHelper.getLocalHost();
			System.setProperty("java.rmi.server.hostname", myIP);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// Create a stub, which acts like a gateway between the actual server object and the
			// network
			ChatServerManagerInterface stub =
					(ChatServerManagerInterface) UnicastRemoteObject.exportObject(this, 0);
			registry = LocateRegistry.getRegistry();
			registry.rebind("ChatServerManager", stub);
		} catch (RemoteException e) {
			System.out.println("can not export the object");
			e.printStackTrace();
		}
		System.out.println("ChatServerManager was created");

		// initial: we create a single chat room and the corresponding ChatServer
		try {
			spawnChatRoom("sports");
			chatRoomsList.add("sports");
		} catch (Exception e) {
			System.out.println("Unable to create default sports room:");
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves the chat server manager instance. This method creates a singleton chat server
	 * manager instance if none was previously created.
	 * 
	 * @return a reference to the singleton chat server manager instance
	 */
	public static ChatServerManager getInstance() {
		if (instance == null)
			instance = new ChatServerManager();

		return instance;
	}

	/**
	 * Getter method for list of chat rooms.
	 * 
	 * @return a list of chat rooms
	 * @see Vector
	 */
	public Vector<String> getRoomsList() {
		return chatRoomsList;
	}

	/**
	 * Creates a chat room with a specified room name <code>roomName</code>.
	 * 
	 * @param roomName the name of the chat room
	 * @return <code>true</code> if the chat room was successfully created, <code>false</code>
	 *         otherwise.
	 */
	public boolean createRoom(String roomName) {
		System.out.println("Reached Room creation.");
		try {
			if (chatRoomsList.contains(roomName)) {
				// Return false if the room already exists
				throw new Exception("A room of the name " + roomName + " already exists.");
			}
			spawnChatRoom(roomName);
			chatRoomsList.add(roomName);
		} catch (Exception e) {
			System.out.println("Unable to create new room " + roomName + ":");
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void spawnChatRoom(String roomName) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", "build/classes/java/main",
				"os.chat.server.ChatServer", "room_" + roomName);
		processBuilder.inheritIO(); // new process should use same stdin/err/out.
		Process proc = processBuilder.start();
		System.out.println("Process " + "room_" + roomName + " (" + proc + ") " + " was created.");
		chatRooms.add(proc);
	}

	public static void main(String[] args) {
		try {
			// Start RMI here on default port.
			LocateRegistry.createRegistry(1099);
		} catch (RemoteException e) {
			System.out.println("error: can not create registry");
			e.printStackTrace();
		}
		System.out.println("registry was created");
		getInstance();
	}
}
