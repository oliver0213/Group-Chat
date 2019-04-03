package backendServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class serverWorker extends Thread{
	
	//declares final int to to store each clients socket number so they can be identified
	private final Socket clientSocket;
	//declares variable login set to null so each user can have individual name instead of referencing their socket number
	private String login = null;
	//creates object of server class to allow all users to access methods inside of class server, to allow them the current worker list
	private final Server server;
	//creates an outputstream for every user/ socket
	private OutputStream oStream;
	//created to allow users to join groups
	private HashSet<String> topicSet = new HashSet<>();
	
	//constructor to pass in every clients instance of server and their individual socket number
	public serverWorker(Server server, Socket clientSocket) {
		this.server = server;
		this.clientSocket = clientSocket;
	}
	
	//run method for the thread created by the server to hit
	@Override
	public void run() {
		try {
			//throws everybody directly into a method to handle their connection
			clientSocketConnect();
		} catch (IOException e) {
			//you messed up
			e.printStackTrace();
		} catch (InterruptedException e) {
			//how the... 
			e.printStackTrace();
		}
	}
	
	//handles the connection for each user
	private void clientSocketConnect() throws IOException, InterruptedException {
		//creates input stream so each client can receive messages
		InputStream iStream = clientSocket.getInputStream();
		//creates new outputstream for each socket number (this.oStream, allows for program to record each sockets number)
		this.oStream = clientSocket.getOutputStream();
		
		//bufferedreader reads array and lines, input stream reads bytes to a string. together they scan until new input found then take the bytes sent and make a string
		BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
		//string created to store each message for duration of the while loop that only is broken if istream is closed(because istream traps thread and doesn't pass until it reads line)
		String line;
		//creates loop for duration socket connection(because ostream, istream, and socket connection all tied together)
		while ( (line = reader.readLine()) != null) {
			//splits line by every space and store it in a array of strings (allows program to read cmd, username, and text)
			String[] tokens = StringUtils.split(line);
			//prevents empty arrays to pass and keeps all possible cmds in same checker
			if(tokens !=null && tokens.length > 0) {
				//assigns first string of the array to variable so we can find out what was said
				String cmd = tokens[0];
				//if the first string in the input is logoff then it sends it to the handle logoff method
				if ("logoff".equalsIgnoreCase(cmd)) {
					handleLogoff();
				//this is the only way to get to the rest of the method, which disconnects this users socket/ ends their application
					break;
				//if the first string in the input is login then it sends it to the handle login method
				}else if("login".equalsIgnoreCase(cmd)) {
					//allows input so server can tell client if it failed/ succeeded and passes rest of client input so it can check username/password
					clientLogin(oStream, tokens);
				//if the first string in the input is msg then it sends it to the handle msg method
				}else if("msg".equalsIgnoreCase(cmd)){
					//creates array that splits with certain propertie. (what string, what seperation character, max number of splits)(so message isn't cut off if it contains a space
					String[] tokensMsg = StringUtils.split(line, null, 3);
					//sends new array to handle massage method
					handleMessage(tokensMsg);
				//if the first string in the input is join then it sends it to the handle join method (for group chats)
				}else if("join".equalsIgnoreCase(cmd)) {
					//weeds out join cmds without group name
					if (tokens.length > 1) {
					//passes array to handlejoin method
					handleJoin(tokens);
					}
				//if the first string in the input is leave then it sends it to the handle leave method (for group chats)
				}else if("leave".equalsIgnoreCase(cmd)) {
					//weeds out any leave command that don't contain a group name
					if (tokens.length > 1) {
					//passes array to handleleave method
					handleLeave(tokens);
					}
				//if typed cmd(first word of user message)(going to be handled by GUI, user wont see) doesnt match any test cases
				}else {
					//this should never be hit after GUI is created
					String msg = "unknown " + cmd + "\n";
					//tells the client they messed up
					oStream.write(msg.getBytes());
				}
				//in the loop and returns to the client what they typed, so a GUI can keep a message box that has sender client and receiver client messages
				String msg = "\nYou typed: " + line + "\n";
				oStream.write(msg.getBytes());
				
			}
		}
		//if logoff cmd is given this code is hit to turn off this stream
		oStream.close();
	}
	
	//lets the client leave groups
	private void handleLeave(String[] tokens) {
			//assigns string to second string in array
			String topic = tokens[1];
			//removes the topic set from this users instance
			topicSet.remove(topic);
	}

	//checks if member is in topic/groupchat (for sending group message)
	public boolean isMemberOfTopic(String topic) {
		return topicSet.contains(topic);
	}
	
	//when user joins topic
	private void handleJoin(String[] tokens) {
			//assigns second string of array to variable to add it to hashset/ group chats
			String topic = tokens[1];
			//adds group name to hashset
			topicSet.add(topic);
	}

	// format: "msg" "username" body...
	// format: "msg "#topic" body...
	private void handleMessage(String[] tokens) throws IOException {
		//stores login of receiving user 
		String sendTo = tokens[1];
		//stores message to be sent
		String body = tokens[2];
		
		//checks if you are sending to chat or user
		boolean isTopic = sendTo.charAt(0) == '#';
		
		//gets updated list of online users for this user/ socket
		List<serverWorker> workerList = server.getWorkerList();
		//runs through online user list 
		for(serverWorker tom : workerList) {
			//Separates chat messages from user messages
			if (isTopic) {
				//makes sure user is in the group
				if (tom.isMemberOfTopic(sendTo)) {
					//creates variable to store all info in a user friendly way
					String outMsg = "msg " + sendTo + ": " + login + " " + body + "\n";
					//sends variable containing message to outMsg method
					tom.send(outMsg);
				}
			// if not group then lets send it to this user
			}else {
				//tests if sending user typed in a login that is contained in list of serverworkers
				if (sendTo.equalsIgnoreCase(tom.getLogin())) {
					//compiles message to that user
					String outMsg = "msg " + login + " " + body + "\n";
					//sends to outMsg method
					tom.send(outMsg);
				}	
			}
		}
		
	}

	//for loggoff
	private void handleLogoff() throws IOException{
		//gets updated worker list from server
		List<serverWorker> workerList = server.getWorkerList();
		
		//creates a string with this users login to blast out to all online users
		String onLineMsg = "Offline " + login + "\n";
		//runs for all users in online list
		for(serverWorker tom : workerList) {
			//weeds out current user
			if (!login.equals(tom.getLogin())) {
				//sends to send method
				tom.send(onLineMsg);
			}
		}
		
		//stops this instance/socket
		System.out.print("User: " + login + " logged off!");
		oStream.close();
	}
	
	//returns login name of sender
	public String getLogin() {
		return login;
	}
	
	//logs account in
	private void clientLogin(OutputStream oStream, String[] tokens) throws IOException {
		//weeds out any arrays that dont have 3 string
		if(tokens.length == 3) {
			//assigns stings to username/password
			String login = tokens[1];
			String password = tokens[2];
			
			//looks to see if they signed into an actual account
			if ((login.equals("admin") && password.equals("password")) || (login.equals("nathan") && password.equals("password"))) {
				//server tells client they correctly signed in
				String msg = "ok login";
				oStream.write(msg.getBytes());
				//stores each sockets login
				this.login = login;
				//tells server debug who signed in
				System.out.println("User logged in: " + login);
				
				//gets updated list of server workers
				List<serverWorker> workerList = server.getWorkerList();
				
				//send current users all online logins
				for(serverWorker tom : workerList) {
					//weeds out sockets that are connected but not signed in
					if (tom.getLogin() != null) {
						//takes out sender instance from list (so they dont get told they are online
						if (!login.equals(tom.getLogin())) {
							//Concatenates online message
							String msg2 = "\nOnline " + tom.getLogin() + "\n";
							send(msg2);
						}
					}
				}
				
				// tells all online users that this login just signed on
				String onLineMsg = "online " + login + "\n";
				for(serverWorker tom : workerList) {
					if (!login.equals(tom.getLogin())) {
						tom.send(onLineMsg);
					}
				}
			//tells client they did not have correct login info
			}else {
				String msg = "error login";
				oStream.write(msg.getBytes());
				System.err.println("Login failed for " + login);
			}
		}
	}
	//makes sure user is signed in
	private void send(String msg) throws IOException {
		if (login != null) {
		oStream.write(msg.getBytes());
		}
	}
}
