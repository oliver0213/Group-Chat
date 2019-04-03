package chat;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

public class ChatClient extends JFrame implements ActionListener {
	JLabel nameLabel = new JLabel();
	JLabel total = new JLabel();
	JTextField nameTextField = new JTextField(15);
	JButton connectButton = new JButton();
	JButton disConnectButton = new JButton();
	JTextArea chatContentTextArea = new JTextArea(9, 30);
	JButton sendMsgButton = new JButton();
	JTextField msgTextField = new JTextField(30);
	JLabel msglabel = new JLabel();
	java.awt.List peopleList = new java.awt.List(10);

	private String name = "";
	Socket soc = null;
	PrintStream ps = null;
	ClentListener listener = null;
//constructor
	public ChatClient(String name) {
		this.name = name;
		nameTextField.setText(name);
		nameTextField.setEditable(false);
		init();
		connect();
	}

	public void connect() {
		if (soc == null) {
			try {
				soc = new Socket(InetAddress.getLocalHost(),
						Constants.SERVER_PORT);
				System.out.println("Client socket :" + soc);
				ps = new PrintStream(soc.getOutputStream());
				StringBuffer info = new StringBuffer(
						Constants.CONNECT_IDENTIFER)
						.append(Constants.SEPERATOR);
				String userinfo = nameTextField.getText() + Constants.SEPERATOR
						+ InetAddress.getLocalHost().getHostAddress();
				ps.println(info.append(userinfo));
				ps.flush();
				listener = new ClentListener(this, nameTextField.getText(), soc);
				listener.start();
			} catch (IOException e) {
				System.out.println("Error:" + e);
				disconnect();
			}
		}
	}

	public void init() {
		this.setTitle("Chat Room");
		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		nameLabel.setText("welcome：" + name);
		total.setText("Online：");
		connectButton.setText("Connect.");
		connectButton.addActionListener(this);
		disConnectButton.setText("disconnect");
		disConnectButton.addActionListener(this);
		chatContentTextArea.setEditable(false);
		sendMsgButton.setText("send");
		sendMsgButton.addActionListener(this);
		msglabel.setText("input：");
		msgTextField.setText("");
		JPanel panel1 = new JPanel();
		panel1.setLayout(new FlowLayout());
		panel1.add(nameLabel);
		panel1.add(disConnectButton);
		panel1.add(total);

		JPanel panel2 = new JPanel();
		panel2.setLayout(new FlowLayout());
		JScrollPane pane1 = new JScrollPane(chatContentTextArea);
		JScrollPane pane2 = new JScrollPane(peopleList);
		pane2.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(
				Color.white, new Color(255, 255 , 255)), "Use List"));
		pane1.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(
				Color.white, new Color(0, 255, 255)), "Chat Content"));
		panel2.add(pane2);
		panel2.add(pane1);
		JPanel panel3 = new JPanel();
		panel3.setLayout(new FlowLayout());
		panel3.add(msglabel);
		panel3.add(msgTextField);
		panel3.add(sendMsgButton);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(panel1, BorderLayout.NORTH);
		this.getContentPane().add(panel2, BorderLayout.CENTER);
		this.getContentPane().add(panel3, BorderLayout.SOUTH);
		this.pack();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// protected void processWindowEvent(WindowEvent e) {
	// super.processWindowEvent(e);
	// if (e.getID() == WindowEvent.WINDOW_CLOSING) {
	// disconnect();
	// dispose();
	// System.exit(0);
	// }
	// }

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == disConnectButton) {
			disconnect();
			this.dispose();
		} else if (source == sendMsgButton) {
			if ("".equals(msgTextField.getText())
					|| null == msgTextField.getText())
				return;
			if (soc != null) {
				StringBuffer msg = new StringBuffer(Constants.MSG_IDENTIFER)
						.append(Constants.SEPERATOR);
				ps.println(msg.append(msgTextField.getText()));
				ps.flush();
				msgTextField.setText("");
				msgTextField.requestFocus();
			}
		}
	}

	public void disconnect() {
		if (soc != null) {
			try {
				ps.println(Constants.QUIT_IDENTIFER);
				ps.flush();
				soc.close();
				listener.toStop();
				soc = null;
			} catch (IOException e) {
				System.out.println("Error:" + e);
			}
		}
	}

	class ClentListener extends Thread {
		String name = null;
		BufferedReader br = null;
		PrintStream ps = null;
		Socket socket = null;
		ChatClient parent = null;
		boolean running = true;

		public ClentListener(ChatClient p, String n, Socket s) {
			parent = p;
			name = n;
			socket = s;
			try {
				br = new BufferedReader(new InputStreamReader(
						s.getInputStream()));
				ps = new PrintStream(s.getOutputStream());
			} catch (IOException e) {
				System.out.println("Error:" + e);
				parent.disconnect();
			}
		}

		public void toStop() {
			this.running = false;
		}

		public void run() {
			String msg = null;
			while (running) {
				msg = null;
				try {
					msg = br.readLine();
					System.out.println("receive msg: " + msg);
				} catch (IOException e) {
					System.out.println("Error:" + e);
					parent.disconnect();
				}
				if (msg == null) {
					parent.listener = null;
					parent.soc = null;
					parent.peopleList.removeAll();
					running = false;
					return;
				}

				StringTokenizer st = new StringTokenizer(msg,
						Constants.SEPERATOR);
				String keyword = st.nextToken();
				if (keyword.equals(Constants.STATISTICS)) {
					total.setText("Current Online number：" + st.nextToken());
				}
				if (keyword.equals(Constants.PEOPLE_IDENTIFER)) {
					parent.peopleList.removeAll();
					while (st.hasMoreTokens()) {
						String str = st.nextToken();
						parent.peopleList.add(str);
					}
				} else if (keyword.equals(Constants.MSG_IDENTIFER)) {
					String usr = st.nextToken();
					parent.chatContentTextArea.append(usr);
					parent.chatContentTextArea.append(st.nextToken("\0"));
					parent.chatContentTextArea.append("\r\n");
				} else if (keyword.equals(Constants.QUIT_IDENTIFER)) {
					System.out.println("Quit");
					try {
						running = false;
						parent.listener = null;
						parent.soc.close();
						parent.soc = null;
					} catch (IOException e) {
						System.out.println("Error:" + e);
					} finally {
						parent.soc = null;
						parent.peopleList.removeAll();
					}
					break;
				}
			}
			parent.peopleList.removeAll();
		}
	}
}

