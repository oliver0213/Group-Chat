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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * 
 * @author xuting 20140621
 */
public class mainWnd extends JFrame {

	public static boolean serverStarted = false;

	private JLabel jLabel1;
	private JButton startserver;
	private JButton startclient;
	private JButton exit;

	public mainWnd() {
		initComponents();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
	}

	private void initComponents() {

		jLabel1 = new javax.swing.JLabel();
		startserver = new javax.swing.JButton("Run-server");
		startclient = new javax.swing.JButton("Run-client");
		exit = new javax.swing.JButton("exit");

		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		//setTitle("\u9009\u62e9\u542f\u52a8\u9879");
		setSize(400, 300);
		setLocation(500, 200);
		setResizable(false);


		startserver.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		startclient.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		exit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton3ActionPerformed(evt);
			}
		});

		jLabel1.setBounds(200, 230, 200, 20);
		add(jLabel1);
		startserver.setBounds(100, 10, 200, 50);
		add(startserver);
		startclient.setBounds(100, 80, 200, 50);
		add(startclient);
		exit.setBounds(100, 150, 200, 50);
		add(exit);
		startclient.setEnabled(false);
		setLayout(null);

	}

	private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
		// exit
		int k = JOptionPane.showConfirmDialog(null, "Confirm exit?", "XT-tip",
				JOptionPane.OK_CANCEL_OPTION);
		if (k != JOptionPane.OK_OPTION)
			return;
		serverStarted = false;
		System.exit(EXIT_ON_CLOSE);
	}

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
		// Start Client
		Log log = new Log();
		log.setVisible(true);
	}

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
		// Start Server
		serverStarted = true;
		startserver.setText("-The server is already running-");
		startserver.setEnabled(false);
		ChatServer chatServer1 = new ChatServer();
		new Thread(chatServer1).start();
		startclient.setEnabled(true);
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				mainWnd mainwind = new mainWnd();
				mainwind.setVisible(true);
			}
		});
	}
}