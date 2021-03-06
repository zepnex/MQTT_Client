package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;

import application.Singleton;

public class Gui {

	public Gui gui = this;
	Singleton singleton = Singleton.getInstance();
	public JFrame frame;
	private JTextField txtIP;
	private JTextField txtPort;
	public boolean encryptedCon = false;
	public Thread connThread;
	private String temp = "";
	public JTextPane textPane;
	JPanel graphPanel;
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui window = new Gui();
					window.frame.setVisible(true);
					window.setInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	public Gui() {
		initialize();
	}

	public void initialize() {
		frame = firstFrame();
		frame.setVisible(true);
	}

	void setInstance() {
		singleton.gui = this.gui;
	}

	public JFrame firstFrame() {
		JFrame f1 = new JFrame();
		f1.setBounds(screenSize.width / 5, screenSize.height / 5, 800, 500);
		f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f1.getContentPane().setLayout(null);

		// Connection frame which will hide after clicking Up Scotty
		final JPanel connect = new JPanel();
		connect.setSize(f1.getSize());
		connect.setLayout(null);
		f1.getContentPane().add(connect);

		JLabel lblBroker = new JLabel("Broker-IP:");
		lblBroker.setFont(new Font("Arial", Font.BOLD, 12));
		lblBroker.setBounds(f1.getWidth() / 3, 30, 63, 25);
		connect.add(lblBroker);

		JLabel lblPort = new JLabel("Broker-Port:");
		lblPort.setFont(new Font("Arial", Font.BOLD, 12));
		lblPort.setBounds(f1.getWidth() / 3 - 12, 70, 82, 25);
		connect.add(lblPort);

		final JButton btnCon = new JButton("Connect");

		txtIP = new JTextField("192.168.0.100");
		txtIP.setFont(new Font("Arial", Font.BOLD, 12));
		txtIP.setBounds((f1.getWidth() / 3) + 100, 30, 140, 25);
		connect.add(txtIP);

		txtPort = new JTextField("1883");
		txtPort.setFont(new Font("Arial", Font.BOLD, 12));
		txtPort.setBounds((f1.getWidth() / 3) + 100, 70, 140, 25);
		txtPort.setEditable(false);
		connect.add(txtPort);

		// Button to establish connection with the broker just class the method
		// connection in App.java

		btnCon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				connThread = new Thread() {
					public void run() {
						singleton.connect(txtIP.getText(), txtPort.getText(), encryptedCon);
						connect.setVisible(false);
						frame.dispose();
						frame = secondFrame();
						frame.setVisible(true);
					}
				};
				connThread.start();
			}
		});
		btnCon.setFont(new Font("Arial", Font.BOLD, 12));
		btnCon.setBounds((f1.getWidth() / 2) + 50, 400, 200, 23);
		connect.add(btnCon);

		// Icons for the encrypted btn
		ImageIcon lock = new ImageIcon("resources/lock.png");
		ImageIcon unlock = new ImageIcon("resources/unlock.png");

		// Toggle Button -> if u want encrypted connection or not will just change the
		// boolean encryptedCon
		final JToggleButton tb = new JToggleButton(unlock);
		tb.setFont(new Font("Arial", Font.BOLD, 12));
		tb.setBounds((f1.getWidth() / 2) - 60, 200, 104, 104);
		tb.setBorderPainted(false);
		tb.setContentAreaFilled(false);
		tb.setSelectedIcon(lock);
		connect.add(tb);

		tb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton tBtn = (JToggleButton) e.getSource();
				// Toggles the boolean
				if (tBtn.isSelected()) {
					txtPort.setText("8883");
					encryptedCon = true;
				} else {
					txtPort.setText("1883");
					encryptedCon = false;
				}
			}
		});

		// Quit Button in Connect Panel
		JButton btnQuit = new JButton("Quit");
		btnQuit.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				if (connThread != null)
					connThread.stop();
				else
					frame.dispose();
				System.exit(1);
			}
		});
		btnQuit.setBounds((f1.getWidth() / 2) - 250, 400, 200, 23);
		connect.add(btnQuit);

		return f1;
	}

	JFrame secondFrame() {
		String[] topics = { "Temperature", "Pressure", "Humidity", "Accelleration", "Gyrodata", "Magdata" };

		final JFrame f2 = new JFrame();
		f2.setBounds(0, 0, screenSize.width - 40, screenSize.height - 40);
		f2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f2.getContentPane().setLayout(null);

		// whole pane for displaying all informations
		final JPanel datapane = new JPanel();
		datapane.setSize(screenSize);
		datapane.setVisible(true);
		datapane.setLayout(null);

		// create array of radiobutton
		// subscribe per click and vice versa
		final ButtonGroup btnGroup = new ButtonGroup();
		final ArrayList<JRadioButton> jRBtn = new ArrayList<JRadioButton>();
		final ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String topic = ((JRadioButton) e.getSource()).getText();
				if (topic.equals(temp)) {
					// when double click on the same topic
					btnGroup.clearSelection();
					singleton.unsubscribe(topic);
					temp = "";
				} else {
					if (!temp.isEmpty()) {
						singleton.unsubscribe(temp);
						singleton.data.series.clear();
						singleton.data.collection.clear();
					}
					temp = topic;
					singleton.subscribe(topic);
				}
				singleton.chart.lineChart(topic);
			}
		};

		// whole radiobutton scroll pane
		final JPanel subscriberPanel = new JPanel();
		subscriberPanel.setBounds(f2.getWidth() - 216, 0, 255, f2.getHeight() - 70);
		subscriberPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		subscriberPanel.setLayout(null);
		datapane.add(subscriberPanel);

		for (int i = 0; i < topics.length; i++) {
			// initialize btn and add it to pane
			jRBtn.add(new JRadioButton(topics[i]));
			jRBtn.get(i).setBounds(2, (i * 50) + 1, subscriberPanel.getWidth() - 100, 40);
			jRBtn.get(i).setFont(new Font("Arial", Font.BOLD, 12));
			subscriberPanel.add(jRBtn.get(i));
			jRBtn.get(i).addActionListener(listener);
			btnGroup.add(jRBtn.get(i));
		}

		f2.getContentPane().add(datapane);

		graphPanel = new JPanel();
		graphPanel.setBounds(0, 0, f2.getWidth() - 215, (f2.getHeight() / 2) + 10);
		graphPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		graphPanel.setLayout(null);
		datapane.add(graphPanel);

		final JPanel textPanel = new JPanel();
		textPanel.setBounds(0, (screenSize.height / 2) - 11, screenSize.width - 255, (screenSize.height / 2) - 99);
		textPanel.setLayout(null);
		datapane.add(textPanel);

		textPane = new JTextPane();
		textPane.setFont(new Font("Arial", Font.BOLD, 12));
		textPane.setSize(textPanel.getSize());
		textPane.setBorder(BorderFactory.createLineBorder(Color.black));
		textPane.setVisible(true);
		textPanel.add(textPane);

		JPanel btnPanel = new JPanel();
		btnPanel.setBounds(f2.getWidth() - 216, f2.getHeight() - 71, 216, 70);
		btnPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		datapane.add(btnPanel);

		JPanel controlPanel = new JPanel();
		controlPanel.setBounds(0, screenSize.height - 111, screenSize.width - 255, 41);
		controlPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		controlPanel.setLayout(new FlowLayout());
		datapane.add(controlPanel);
		final ActionListener addListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog(f2, "Topic:", "add new topic", JOptionPane.PLAIN_MESSAGE);
				boolean existing = false;
				if (input == null || input.isEmpty()) {
					JOptionPane.showMessageDialog(f2, "Topic can't be empty", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					// check if topic already exists
					for (JRadioButton btn : jRBtn) {
						if (btn.getText().equals(input)) {
							existing = true;
							JOptionPane.showMessageDialog(f2, "Topic already exists", "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
					if (!existing) {
						jRBtn.add(new JRadioButton(input));
						jRBtn.get(jRBtn.size() - 1).addActionListener(listener);
						refreshTopics(jRBtn, subscriberPanel, listener, btnGroup);
						btnGroup.add(jRBtn.get(jRBtn.size() - 1));
						f2.repaint();
					}
				}
			}
		};
		JButton add = new JButton("add");
		add.setSize(100, 40);
		controlPanel.add(add);
		add.addActionListener(addListener);

		ActionListener removeListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog("Topic:");
				for (int j = 0; j <= jRBtn.size() - 1; j++) {
					if (input == null || input.isEmpty()) {
						break;
					} else {
						if (input.equals(jRBtn.get(j).getText())) {
							jRBtn.remove(j);
							refreshTopics(jRBtn, subscriberPanel, listener, btnGroup);
							singleton.unsubscribe(input);
							f2.repaint();
							break;
						} else {
							if (j == jRBtn.size() - 1) {
								JOptionPane.showMessageDialog(f2, "Error: No sutch topic", "Error",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
		};

		JButton remove = new JButton("remove");
		remove.setSize(100, 40);
		controlPanel.add(remove);
		remove.addActionListener(removeListener);

		JButton disconnect = new JButton("Disconnect");
		disconnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!temp.equals(""))
					singleton.unsubscribe(temp);
				singleton.quitConnection();
				f2.dispose();
				frame = firstFrame();
				initialize();
			}
		});
		btnPanel.add(disconnect);
		f2.getContentPane().add(datapane);
		return f2;

	}

	void refreshTopics(ArrayList<JRadioButton> list, JPanel subscriberPanel, ActionListener listener,
			ButtonGroup btnGroup) {
		subscriberPanel.removeAll();
		// refresh panel with topics
		for (int i = 0; i <= list.size() - 1; i++) {
			list.get(i).setBounds(2, i * 50, subscriberPanel.getWidth() - 100, 40);
			list.get(i).setFont(new Font("Arial", Font.BOLD, 12));
			subscriberPanel.add(list.get(i));
		}
	}

}