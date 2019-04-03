package backendServer;

//serverMain acts as the first method called
public class serverMain {
	//make main method for program
	public static void main(String[] args) {
		//creates int called port, which assigns the port used
		int port = 1999; 
		//creates new instance of server and passes it the port number declared above
		Server server = new Server(port);
		//sends thread to server.run
		server.start();
	}
}
//servermain is now done, the original thread is dead but program stays alive because of new thread passed to server.start(server.run)


