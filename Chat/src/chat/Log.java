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
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class Log extends JFrame {

	private JLabel label1;
	private JTextField txt1;
	private JLabel label2;
	private JLabel label3;
	private JTextField txt2;
	private JButton btnlog;
	private JButton btnreg;
	private JButton btncnl;
	private JFrame wnd = null;
	Socket soc = null;
	PrintStream ps = null;
	LogListener listener = null;

	public Log() {
		InitWnd();
		wnd = this;
	}

	public void InitWnd() {

		setLayout(null);
		label1 = new JLabel("User：");
		label2 = new JLabel(" Password：");	
		txt1 = new JTextField();
		txt2 = new JTextField();
		btnlog = new JButton("log in");
		btnreg = new JButton("sign up");
		btncnl = new JButton("cancel");

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("-log in-");    // title of client window
		setSize(300, 200);      //the size of client window
		setLocation(525, 200); // location of client window
		setResizable(false);  // window can't be resized by the user

		label1.setBounds(15, 30, 55, 25);  //location of User label
		label2.setBounds(5, 65, 55, 25);   //location of Password label               		
		txt1.setBounds(85, 30, 140, 25);  
		txt2.setBounds(85, 65, 140, 25);
		btnreg.setBounds(25, 105, 80, 35);//location of three buttons (log in, sign up and cancel)
		btnlog.setBounds(105, 105, 80, 35);
		btncnl.setBounds(185, 105, 80, 35);

		add(label1);
		add(label2);
		add(txt1);
		add(txt2);
		add(btnlog);
		add(btnreg);
		add(btncnl);
                //sign up botton action
		btnreg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reg();
			}
		});
                //log in botton action
		btnlog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log();
			}
		});
                //cancel button action
		btncnl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cnl();
			}
		});
	}
        //method of sign up botton
	public void reg() {
		String name = txt1.getText();
		String psd = txt2.getText();
		if (null == name || ("").equals(name)) {
			JOptionPane.showConfirmDialog(null, "input username！", "promption：",
					JOptionPane.OK_OPTION);
			return;
		}
		if (null == psd || ("").equals(psd)) {
			JOptionPane.showConfirmDialog(null, "input password！", "promption：",
					JOptionPane.OK_OPTION);
			return;
		}

		if (soc == null) {
			try {
				soc = new Socket(InetAddress.getLocalHost(),
						Constants.SERVER_PORT);
				System.out.println(soc);
				ps = new PrintStream(soc.getOutputStream());
				StringBuffer info = new StringBuffer(Constants.INTENT_REG)
						.append(Constants.SEPERATOR);
				String userinfo = name + Constants.SEPERATOR + psd
						+ Constants.SEPERATOR
						+ InetAddress.getLocalHost().getHostAddress();
				ps.println(info.append(userinfo));
				ps.flush();
				listener = new LogListener(this, name, soc);
				new Thread(listener).start();

			} catch (IOException e) {
				System.out.println("Error:client reg" + e);
				disconnect();
			}
		}

	}

	public void log() {
		String name = txt1.getText();
		String psd = txt2.getText();
		if (null == name || ("").equals(name)) {
			JOptionPane.showConfirmDialog(null, "input username！", "promption：",
					JOptionPane.OK_OPTION);
			return;
		}
		if (null == psd || ("").equals(psd)) {
			JOptionPane.showConfirmDialog(null, "input password！", "promption：",
					JOptionPane.OK_OPTION);
			return;
		}

		if (soc == null) {
			try {
				soc = new Socket(InetAddress.getLocalHost(),
						Constants.SERVER_PORT);
				System.out.println(soc);
				ps = new PrintStream(soc.getOutputStream());
				StringBuffer info = new StringBuffer(Constants.INTENT_LOG)
						.append(Constants.SEPERATOR);
				String userinfo = name + Constants.SEPERATOR + psd
						+ Constants.SEPERATOR
						+ InetAddress.getLocalHost().getHostAddress();
				ps.println(info.append(userinfo));
				ps.flush();
				listener = new LogListener(this, name, soc);
				new Thread(listener).start();

			} catch (IOException e) {
				System.out.println("Error:" + e);
				disconnect();
			}
		}

	}

	public void cnl() {
		wnd.dispose();
	}
        // the botton of disconnect
	public void disconnect() {
		if (soc != null) {
			try {
				ps.println(Constants.QUIT_IDENTIFER);
				ps.flush();
				soc.close();
				listener.toStop();
				soc = null;
			} catch (IOException e) {
				System.out.println("Error:client disconnect" + e);
			}
		}
	}

	class LogListener extends Thread {
		String name = null;
		BufferedReader br = null;
		PrintStream ps = null;
		Socket socket = null;
		Log parent = null;
		boolean running = true;

		public LogListener(Log p, String n, Socket s) {
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
					running = false;
					return;
				}

				StringTokenizer st = new StringTokenizer(msg,
						Constants.SEPERATOR);
				String keyword = st.nextToken();
				if (keyword.equals(Constants.INTENT_LOG)) {
					String result = st.nextToken();
					if (null != result && !"".equals(result)) {
						if ("true".equals(result)) {
							toStop();
							wnd.dispose();
							new ChatClient(name).setVisible(true);
							return;
						} else {
							toStop();
							wnd.dispose();
							JOptionPane.showMessageDialog(null, "wrong user name or password!",
									"Login failed", JOptionPane.OK_OPTION);
						}
					}
				}
				if (keyword.equals(Constants.INTENT_REG)) {
					String result = st.nextToken();
					if (null != result && !"".equals(result)) {
						if ("true".equals(result)) {
							toStop();
							wnd.dispose();
							JOptionPane.showMessageDialog(null, "Congratulations, registration is successful！",
									"registration success", JOptionPane.OK_CANCEL_OPTION);
							new Log().setVisible(true);
							return;
						} else {
							toStop();
							wnd.dispose();
							JOptionPane.showMessageDialog(null, "Username already exists!",
									"registration failed", JOptionPane.WARNING_MESSAGE);
						}
					}
				}
				if (keyword.equals(Constants.PEOPLE_IDENTIFER)) {
					while (st.hasMoreTokens()) {
						String str = st.nextToken();
					}
				} else if (keyword.equals(Constants.MSG_IDENTIFER)) {
					String usr = st.nextToken();
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
					}
					break;
				}
			}
		}
	}

}

