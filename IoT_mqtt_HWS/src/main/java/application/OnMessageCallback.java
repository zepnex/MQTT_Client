package application;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import utils.Message;
import utils.Revert;

public class OnMessageCallback implements MqttCallback {
	Singleton singleton = Singleton.getInstance();
	ArrayList<Message> list = new ArrayList<Message>();
	ArrayList<Message> reverted = new ArrayList<Message>();
	double start = 0;
	String topic;
	String content;

	public void connectionLost(Throwable cause) {
		// After the connection is lost, it usually reconnects here
		singleton.gui.frame.dispose();
		singleton.gui.frame = singleton.gui.firstFrame();
		singleton.abortCon();
		singleton.gui.initialize();
		System.out.println("disconnect，you can reconnect: " + cause);
		JOptionPane.showMessageDialog(singleton.getFrame(), "Whops, something went wrong check your connection.",
				"Error", JOptionPane.ERROR_MESSAGE);

	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		this.topic = topic;

		// convert MqttMessage to string
		content = new String(message.getPayload());
		// System.out.println("Received Message! Topic: " + topic + " | Message: " +
		// content);

		// adding message to ArrayList
		list.add(new Message(topic, content));
		reverted = new Revert().revert(list);
		if (list.size() == 11) {
			// update list if we get more then 10 messages
			list.remove(0);
		}
		singleton.displayText(reverted);
		try {
			JSONObject js = new JSONObject(content);
			singleton.data.addData(js, topic);
			singleton.chart.lineChart(topic);
		} catch (Exception e) {
			System.out.println("No Json formatt");
		}

	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("deliveryComplete---------" + token.isComplete());
	}

}