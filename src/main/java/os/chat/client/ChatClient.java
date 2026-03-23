package os.chat.client;


import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Vector;
import org.apache.commons.validator.routines.InetAddressValidator;
import os.chat.server.ChatServer;
import os.chat.server.ChatServerInterface;
import os.chat.server.ChatServerManagerInterface;

/**
 * This class implements a chat client that can be run locally or remotely to communicate with a
 * {@link ChatServer} using RMI.
 */
public class ChatClient implements CommandsFromWindow, CommandsFromServer {

	/**
	 * The name of the user of this client
	 */
	private String userName;

	/**
	 * The graphical user interface, accessed through its interface. In return, the GUI will use the
	 * CommandsFromWindow interface to call methods to the ChatClient implementation.
	 */
	private final CommandsToWindow window;

	/**
	 * The interface to the server manager interface so we will be able to remotely call these
	 * functions.
	 */
	ChatServerManagerInterface csm;

	/**
	 * Collects the interfaces to all joined servers as a key-value pair of <code>roomName</code>
	 * and {@link ChatServerInterface}.
	 */
	HashMap<String, ChatServerInterface> joinedServers;


	/**
	 * The registry is used to lookup distant RMI interfaces.
	 */
	Registry registry;

	/**
	 * Constructor for the <code>ChatClient</code>. Must perform the connection to the server. If
	 * the connection is not successful, it must exit with an error.
	 * 
	 * @param window reference to the GUI operating the chat client
	 * @param userName the name of the user for this client
	 * @since Q1
	 */
	public ChatClient(CommandsToWindow window, String userName) {
		this.window = window;
		this.userName = userName;
		this.joinedServers = new HashMap<>();

		// Getting the IP address of the server
		String ipAddr = System.getenv("OS_CHAT_ROOMS_SERVER_IP");
		System.out.println("OS_CHAT_ROOMS_SERVER_IP=" + ipAddr);
		InetAddressValidator validator = InetAddressValidator.getInstance();
		if (ipAddr != null && validator.isValid(ipAddr)) {
			System.out.println("Connecting to server on " + ipAddr + ".");
		} else {
			ipAddr = null; // setting it to null will by default make the getRegistry function look
			// for localhost.
			System.out.println(
					"OS_CHAT_ROOMS_SERVER_IP does not contain a valid IP address. Defaulting to localhost.");
		}

		// Setting own IP address to our public IP address to advertise to the server
		try {
			String publicLocalHost = NetworkHelper.getLocalHost();
			System.out.println("Changing RMI localhost to " + publicLocalHost + ".");
			System.setProperty("java.rmi.server.hostname", publicLocalHost);
		} catch (UnknownHostException e) {
			System.out.println("Unable to get the public localHost. Leaving it to the default.");
		}

		try {
			// Find the RMI server that was started
			registry = LocateRegistry.getRegistry(ipAddr);
			// Connect to the serverManager and cast it to our interface so we will be able to call
			// the functions.
			csm = (ChatServerManagerInterface) registry.lookup("ChatServerManager");

			// Export *this* object to be called over RMI
			// The RMI will be exposed on default port at the public LocalHost address defined
			// above.
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			System.out.println("can not locate registry");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("can not lookup for ChatServerManager");
			e.printStackTrace();
		}
	}

	/*
	 * Implementation of the functions from the CommandsFromWindow interface. See methods
	 * description in the interface definition.
	 */

	/**
	 * Sends a new <code>message</code> to the server to propagate to all clients registered to the
	 * chat room <code>roomName</code>.
	 * 
	 * @param roomName the chat room name
	 * @param message the message to send to the chat room on the server
	 */
	public void sendText(String roomName, String message) {
		try {
			joinedServers.get(roomName).publish(message, userName);
		} catch (Exception e) {
			System.out.println(userName + " failed to publish message " + message + ": " + e);
		}
	}

	/**
	 * Retrieves the list of chat rooms from the server (as a {@link Vector} of {@link String}s)
	 * 
	 * @return a list of available chat rooms or an empty Vector if there is none, or if the server
	 *         is unavailable
	 * @see Vector
	 */
	public Vector<String> getChatRoomsList() {
		try {
			// Run getRoomsList over RMI
			return csm.getRoomsList();
		} catch (RemoteException e) {
			System.out.println("can not call ChatServerManager.getRoomsList()");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Join the chat room. Does not leave previously joined chat rooms. To join a chat room we need
	 * to know only the chat room's name.
	 * 
	 * @param name the name (unique identifier) of the chat room
	 * @return <code>true</code> if joining the chat room was successful, <code>false</code>
	 *         otherwise
	 */
	public boolean joinChatRoom(String roomName) {
		try {
			// First get the remote interface to the server that the client tries to join
			// NOTE: IMO this is very hacky, since the name of the remote object has to exactly
			// match.
			joinedServers.put(roomName, (ChatServerInterface) registry.lookup("room_" + roomName));

			// Register to that server so the serever gets the reference to the client's interface.
			// Since the reference is not serializable, we have to create a stub that the distant
			// server can call the remote function.
			joinedServers.get(roomName).register((CommandsFromServer) this);
		} catch (Exception e) {
			System.out.println("Cannot join " + roomName + ":" + e);
			return false;
		}

		System.out.println(userName + " joined " + roomName + " room.");
		return true;
	}

	/**
	 * Leaves the chat room with the specified name <code>roomName</code>. The operation has no
	 * effect if has not previously joined the chat room.
	 * 
	 * @param roomName the name (unique identifier) of the chat room
	 * @return <code>true</code> if leaving the chat room was successful, <code>false</code>
	 *         otherwise
	 */
	public boolean leaveChatRoom(String roomName) {
		try {
			// First we un-register from the room before deleting it completely from the list. Then,
			// we remove the reference entirely.
			joinedServers.get(roomName).unregister(this);
			joinedServers.remove(roomName);
		} catch (Exception e) {
			System.out.println("Cannot leave " + roomName + ":" + e);
			return false;
		}

		System.out.println(userName + "left" + roomName + ".");
		return true;
	}

	/**
	 * Creates a new room named <code>roomName</code> on the server.
	 * 
	 * @param roomName the chat room name
	 * @return <code>true</code> if chat room was successfully created, <code>false</code>
	 *         otherwise.
	 */
	public boolean createNewRoom(String roomName) {
		try {
			// RMI command returns true if room was created, false else.
			return csm.createRoom(roomName);
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 * Implementation of the functions from the CommandsFromServer interface. See methods
	 * description in the interface definition.
	 */


	/**
	 * Publish a <code>message</code> in the chat room <code>roomName</code> of the GUI interface.
	 * This method acts as a proxy for the
	 * {@link CommandsToWindow#publish(String chatName, String message)} interface i.e., when the
	 * server calls this method, the {@link ChatClient} calls the
	 * {@link CommandsToWindow#publish(String chatName, String message)} method of it's window to
	 * display the message.
	 * 
	 * @param roomName the name of the chat room
	 * @param message the message to display
	 */
	public void receiveMsg(String roomName, String message) {
		window.publish(roomName, message);
	}

	// This class does not contain a main method. You should launch the whole program by launching
	// ChatClientWindow's main method.
}
