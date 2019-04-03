/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

/**
 *
 * @author Administrator
 */
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

public class ChatServer extends Thread {
	static java.awt.List connectInfoList = new java.awt.List(10);
	static Vector clientProcessors = new Vector(10); //Maximum number of clients
	static int activeConnects = 0;

	private static int onlinesum = 0;
	private static int totaluser = 4;
	static User[] users = new User[totaluser];
	String name = "";
	String psd = "";
	BufferedReader br = null;
	PrintStream ps = null;

	String clientInfo = null;
	String clientIP = null;

	public ChatServer() {

		for (int i = 0; i < totaluser; i++)
			users[i] = new User();

		users[0].setName("yunfan");
		users[0].setPsd("123");
		users[0].setOnline(false);

		users[1].setName("nathan");
		users[1].setPsd("123");
		users[1].setOnline(false);

		users[2].setName("kyle");
		users[2].setPsd("123");
		users[2].setOnline(false);


	}

	public void run() {
		System.out.println("Server starting ...");
		ServerSocket server = null;
		try {
			server = new ServerSocket(Constants.SERVER_PORT);
		} catch (IOException e) {
			System.out.println("Error:" + e);
			System.exit(1);
		}
		while (true) {
			if (clientProcessors.size() < Constants.MAX_CLIENT) {
				Socket socket = null;
				try {
					socket = server.accept();
					if (socket != null) {
						System.out.println("server" + socket + "connection");
					}
				} catch (IOException e) {
					System.out.println("Error:" + e);
				}
				ClientProcessor c = null;
				try {
					br = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
					ps = new PrintStream(socket.getOutputStream());
				} catch (IOException e1) {
					System.out.println("Error:" + e1);
				}

				try {
					clientInfo = br.readLine();
				} catch (IOException e) {
					System.out.println("Error:" + e);
				}
				StringTokenizer stinfo = new StringTokenizer(clientInfo,
						Constants.SEPERATOR);
				String head = stinfo.nextToken();

				if (head.equals(Constants.INTENT_LOG)) {
					if (stinfo.hasMoreTokens()) {
						name = stinfo.nextToken();
					}
					if (stinfo.hasMoreTokens()) {
						psd = stinfo.nextToken();
					}
					if (stinfo.hasMoreTokens()) {
						clientIP = stinfo.nextToken();
					}
					new ClientProcessor(clientInfo, ps, name, br);
				} else if (head.equals(Constants.INTENT_REG)) {
					if (stinfo.hasMoreTokens()) {
						name = stinfo.nextToken();
					}
					if (stinfo.hasMoreTokens()) {
						psd = stinfo.nextToken();
					}
					if (stinfo.hasMoreTokens()) {
						clientIP = stinfo.nextToken();
					}
					new ClientProcessor(clientInfo, ps, name, br);
				} else {
					c = new ClientProcessor(clientInfo, ps, name, br);
					clientProcessors.addElement(c);
					onlinesum = ++ChatServer.activeConnects;
					StringBuffer statistics = new StringBuffer(
							Constants.STATISTICS + Constants.SEPERATOR
									+ onlinesum);
					sendMsgToClients(statistics);
					ChatServer.connectInfoList.add(c.clientIP + "connection");

					c.start();
					notifyRoomPeople();
				}
			} else {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
			}
		}

	}

	public void exitActionPerformed(ActionEvent e) {
		sendMsgToClients(new StringBuffer(Constants.QUIT_IDENTIFER));
		closeAll();
		System.exit(0);
	}

	public static void notifyRoomPeople() {
		StringBuffer people = new StringBuffer(Constants.PEOPLE_IDENTIFER);
		for (int i = 0; i < clientProcessors.size(); i++) {
			ClientProcessor c = (ClientProcessor) clientProcessors.elementAt(i);
			people.append(Constants.SEPERATOR).append(c.name);
		}
		sendMsgToClients(people);
	}

	public static synchronized void sendMsgToClients(StringBuffer msg) {
		for (int i = 0; i < clientProcessors.size(); i++) {
			ClientProcessor c = (ClientProcessor) clientProcessors.elementAt(i);
			System.out.println("send msg: " + msg);
			c.send(msg);
		}
	}

	public static void closeAll() {
		while (clientProcessors.size() > 0) {
			ClientProcessor c = (ClientProcessor) clientProcessors
					.firstElement();
			try {
				c.socket.close();
				c.toStop();
			} catch (IOException e) {
				System.out.println("Error:" + e);
			} finally {
				clientProcessors.removeElement(c);
			}
		}
	}

	public static boolean checkClient(ClientProcessor newclient) {
		if (clientProcessors.contains(newclient)) {
			return false;
		} else {
			return true;
		}
	}

	public static void disconnect(ClientProcessor client) {
		disconnect(client, true);
	}

	public static synchronized void disconnect(ClientProcessor client,
			boolean toRemoveFromList) {
		try {
			connectInfoList.add(client.clientIP + "disconnect");
			ChatServer.activeConnects--; // decrease the number of connections by 1
			onlinesum = ChatServer.activeConnects;
			client.send(new StringBuffer(Constants.QUIT_IDENTIFER));
			for (int i = 0; i < totaluser; i++) {
				if (users[i].getName().equals(client.name)) {
					users[i].setOnline(false);
				}
			}
			sendMsgToClients(new StringBuffer(Constants.STATISTICS
					+ Constants.SEPERATOR + onlinesum));
			// client.socket.close();
		} catch (Exception e) {
			System.out.println("Error:" + e);
		} finally {
			if (toRemoveFromList) {
				clientProcessors.removeElement(client);
				client.toStop();
			}
		}
	}

	class ClientProcessor extends Thread {
		Socket socket;
		String name;
		String clientIP;
		String clientInfo = null;
		BufferedReader br;
		PrintStream ps;
		boolean running = true;

		public ClientProcessor(String clientInfo, PrintStream ps, String name,
				BufferedReader br) {
			// socket = s;
			this.clientInfo = clientInfo;
			this.ps = ps;
			this.name = name;
			this.br = br;
			try {
				// br = new BufferedReader(new InputStreamReader(
				// socket.getInputStream()));
				// ps = new PrintStream(socket.getOutputStream());

				StringTokenizer stinfo = new StringTokenizer(clientInfo,
						Constants.SEPERATOR);
				String head = stinfo.nextToken();
				if (head.equals(Constants.INTENT_LOG)) {
					if (stinfo.hasMoreTokens()) {
						name = stinfo.nextToken();
					}
					if (stinfo.hasMoreTokens()) {
						psd = stinfo.nextToken();
					}
					String result = checkLog(name, psd);
					send(new StringBuffer(result));
				} else if (head.equals(Constants.INTENT_REG)) {
					if (stinfo.hasMoreTokens()) {
						name = stinfo.nextToken();
					}
					if (stinfo.hasMoreTokens()) {
						psd = stinfo.nextToken();
					}
					String result = checkName(name, psd);
					send(new StringBuffer(result));
				}

				else {
					if (stinfo.hasMoreTokens()) {
						name = stinfo.nextToken();
					}
					if (stinfo.hasMoreTokens()) {
						clientIP = stinfo.nextToken();
					}
				}
			} catch (Exception e) {
				System.out.println("Error:" + e);
			}
		}

		public String checkLog(String uname, String upsd) {
			String result = "false";
			for (int i = 0; i < users.length; i++) {
				if (uname.equals(users[i].getName())
						&& upsd.equals(users[i].getPsd())
						&& !users[i].isOnline()) {
					result = "true";
					users[i].setOnline(true);
					break;
				}
			}
			return Constants.INTENT_LOG + Constants.SEPERATOR + result;
		}

		public String checkName(String uname, String upsd) {
			String result = "true";
			int i = 0;
			for (i = 0; i < users.length; i++) {
				if (uname.equals(users[i].getName())) {
					result = "false";
					break;
				}
			}
			if (result.equals("true")) {
				int length = users.length;
				User[] temp = new User[length + 1];

				for (i = 0; i < length; i++) {
					temp[i] = new User();
					temp[i].setName(users[i].getName());
					temp[i].setPsd(users[i].getPsd());
					temp[i].setOnline(users[i].isOnline());
				}
				temp[i] = new User();
				temp[i].setName(uname);
				temp[i].setPsd(upsd);
				temp[i].setOnline(false);
				users = new User[length + 1];
				for (i = 0; i < length + 1; i++) {
					users[i] = new User();
					users[i].setName(temp[i].getName());
					users[i].setPsd(temp[i].getPsd());
					users[i].setOnline(temp[i].isOnline());
				}
			}
			return new String(Constants.INTENT_REG + Constants.SEPERATOR
					+ result);
		}

		public void send(StringBuffer msg) {
			ps.println(msg);
			ps.flush();
		}

		public void run() {
			while (running) {
				String line = null;
				try {
					line = br.readLine();
				} catch (IOException e) {
					System.out.println("Error" + e);
					ChatServer.disconnect(this);
					ChatServer.notifyRoomPeople();
					return;
				}
				if (line == null) {
					ChatServer.disconnect(this);
					ChatServer.notifyRoomPeople();
					return;
				}

				StringTokenizer st = new StringTokenizer(line,
						Constants.SEPERATOR);
				String keyword = st.nextToken();
				if (keyword.equals(Constants.MSG_IDENTIFER)) {
					StringBuffer msg = new StringBuffer(Constants.MSG_IDENTIFER)
							.append(Constants.SEPERATOR);
					msg.append(name);
					msg.append(st.nextToken("\0"));
					ChatServer.sendMsgToClients(msg);
				} else if (keyword.equals(Constants.QUIT_IDENTIFER)) {
					ChatServer.disconnect(this);
					ChatServer.notifyRoomPeople();
					running = false;
				}
			}
		}

		public void toStop() {
			running = false;
		}

		public boolean equals(Object obj) {
			if (obj instanceof ClientProcessor) {
				ClientProcessor obj1 = (ClientProcessor) obj;
				if (obj1.clientIP.equals(this.clientIP)
						&& (obj1.name.equals(this.name))) {
					return true;
				}
			}
			return false;
		}

		public int hashCode() {
			return (this.clientIP + Constants.SEPERATOR + this.name).hashCode();
		}
	}
}