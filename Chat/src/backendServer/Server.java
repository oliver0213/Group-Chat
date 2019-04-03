package backendServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

//extends thread so we can trap a thread here to continuously scan for incoming socket connections on the port and also let the app continue
public class Server extends Thread {
	
	//declares a final int to store the server port for the thread stuck in the server class so it can scan
	private final int serverPort;
	
	//creates an arraylist so it can add objects of class server worker and allow online users to find socket numbers for other users
	private ArrayList<serverWorker> workerList = new ArrayList<>();
	
	//constructor that allows the thread stuck in the Server class to have the server's port number so it can scan for new sockets connecting to it
	public Server(int serverPort) {
		this.serverPort = serverPort;
	}
	
	//allows toms (instance of serverworker) that are trapped in server worker infinite loop, get the updated list of toms who all have unique sockets
	public List<serverWorker> getWorkerList(){
		return workerList;
	}
	
	//run method called by the main class
	@Override
	public void run() {
		try {
			//creates a server socket sitting on the initial port pass by main
			@SuppressWarnings("resource")
			ServerSocket cSocket = new ServerSocket(serverPort);
			//creates infinite loop to trap the thread passed into server.start(server.run) by the server main
			while(true) {
				//prints to the debugger of the server
				System.out.println("Waiting for a client...");
				//creates a socket and waits for a new connection to equate it to (waits for client socket to hit assigned port and records its number)
				Socket clientSocket = cSocket.accept();
				//prints that connection was accepted and assigned to a new socket with its number to server debugger
				System.out.println("Accepted connection from: " + clientSocket);
				//creates an instance of worker (i named him tom) then passes this instance of the server object along with its socket number
				serverWorker tom = new serverWorker (Server.this, clientSocket);
				//adds the worker to the worker list to enable online users to see other online users by their unique socket number
				workerList.add(tom);
				//creates a new thread and sends it to tom.start(tom.run)
				tom.start();
				//once the thread hits here it return to start of the infinite loot where it can scan for a new socket connection in the port and assign it to a tom
			}
		//this is required because it is scanning the port for a new connection from another socket
		}catch (IOException e) {
			//you did something wrong if you hit here
			e.printStackTrace();
		}	
	}
}
