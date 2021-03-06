/*
 * Copyright 2014 IBM Corp. All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.bluemixmqtt;

import com.ibm.bluemix.step.AccelSensorValue;
import com.ibm.bluemix.step.ReadMessage;
import com.ibm.bluemix.step.StepDetector;
import org.eclipse.paho.client.mqttv3.*;

import java.text.SimpleDateFormat;

public class MqttHandler implements MqttCallback {
	private final static String DEFAULT_TCP_PORT = "1883";
	private final static String DEFAULT_SSL_PORT = "8883";

	private MqttClient client = null;

	StepDetector stepdetector = new StepDetector();

	public MqttHandler() {

	}

	@Override
	public void connectionLost(Throwable throwable) {
		if (throwable != null) {
			throwable.printStackTrace();
		}
	}

	/**
	 * One message is successfully published
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
		System.out.println(".deliveryComplete() entered");
	}

	/**
	 * Received one subscribed message
	 */
	@Override
	public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
		String payload = new String(mqttMessage.getPayload());

		AccelSensorValue ac = ReadMessage.getInstance().readMessage(payload);
		boolean step  = stepdetector.detectStep(ac);
		int stepCount = stepdetector.getSteps();

		SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DeviceTest deviceTest = new DeviceTest();
		deviceTest.doDevice(step, timestamp, stepCount);

//		System.out.println("TMP " + step);
//
//		System.out.println(".messageArrived - Message received on topic "
//				+ topic + ": message is " + payload);



//		//TODO Format the Json String
//		JSONObject contObj = new JSONObject();
//		JSONObject jsonObj = new JSONObject();
//		try {
//			contObj.put("step", steps);
//			contObj.put("bool", tmp);
//			contObj.put("time", timestamp
//					.format(new Date()));
//			jsonObj.put("d", contObj);
//		} catch (JSONException e1) {
//			e1.printStackTrace();
//		}

//		System.out.println("Json " + jsonObj.toString());
//		publish("iot-2/evt/status/fmt/json", jsonObj.toString(), false, 0);
//		publish("iot-2/evt/" + MqttUtil.DEFAULT_EVENT_ID + "/fmt/json", jsonObj.toString(), false, 0);
//		subscribe("iot-2/type/sensor/id/+/evt/" + MqttUtil.DEFAULT_EVENT_ID + "/fmt/json", 0);
	}
//
//	public void getMessage(MqttMessage mqttMessage) throws Exception  {
//		String payload = new String(mqttMessage.getPayload());
//		AccelSensorValue ac = ReadMessage.getInstance().readMessage(payload);
//		boolean tmp  = StepDetector.getInstance().detectStep(ac);
//		System.out.println("===> Step: ===> " + tmp);
//	}

	public void connect(String serverHost, String clientId, String authmethod, String authtoken, boolean isSSL) {
		// check if client is already connected
		if (!isMqttConnected()) {
			String connectionUri = null;
			
			//tcp://<org-id>.messaging.internetofthings.ibmcloud.com:1883
			//ssl://<org-id>.messaging.internetofthings.ibmcloud.com:8883
			if (isSSL) {
				connectionUri = "ssl://" + serverHost + ":" + DEFAULT_SSL_PORT;
			} else {
				connectionUri = "tcp://" + serverHost + ":" + DEFAULT_TCP_PORT;
			}

			if (client != null) {
				try {
					client.disconnect();
				} catch (MqttException e) {
					e.printStackTrace();
				}
				client = null;
			}

			try {
				client = new MqttClient(connectionUri, clientId);
			} catch (MqttException e) {
				e.printStackTrace();
			}

			client.setCallback(this);

			// create MqttConnectOptions and set the clean session flag
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(true);

			options.setUserName(authmethod);
			options.setPassword(authtoken.toCharArray());

			//If SSL is used, do not forget to use TLSv1.2
			if (isSSL) {
				java.util.Properties sslClientProps = new java.util.Properties();
				sslClientProps.setProperty("com.ibm.ssl.protocol", "TLSv1.2");
				options.setSSLProperties(sslClientProps);
			}

			try {
				// connect
				client.connect(options);
				System.out.println("Connected to " + connectionUri);
			} catch (MqttException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * Disconnect MqttClient from the MQTT server
	 */
	public void disconnect() {

		// check if client is actually connected
		if (isMqttConnected()) {
			try {
				// disconnect
				client.disconnect();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Subscribe MqttClient to a topic
	 * 
	 * @param topic
	 *            to subscribe to
	 * @param qos
	 *            to subscribe with
	 */
	public void subscribe(String topic, int qos) {

		// check if client is connected
		if (isMqttConnected()) {
			try {
				client.subscribe(topic, qos);
				System.out.println("Subscribed: " + topic);

			} catch (MqttException e) {
				e.printStackTrace();
			}
		} else {
			connectionLost(null);
		}
	}

	/**
	 * Unsubscribe MqttClient from a topic
	 * 
	 * @param topic
	 *            to unsubscribe from
	 */
	public void unsubscribe(String topic) {
		// check if client is connected
		if (isMqttConnected()) {
			try {

				client.unsubscribe(topic);
			} catch (MqttException e) {
				e.printStackTrace();
			}
		} else {
			connectionLost(null);
		}
	}

	/**
	 * Publish message to a topic
	 * 
	 * @param topic
	 *            to publish the message to
	 * @param message
	 *            JSON object representation as a string
	 * @param retained
	 *            true if retained flag is requred
	 * @param qos
	 *            quality of service (0, 1, 2)
	 */
	public void publish(String topic, String message, boolean retained, int qos) {
		// check if client is connected
		if (isMqttConnected()) {
			// create a new MqttMessage from the message string
			MqttMessage mqttMsg = new MqttMessage(message.getBytes());
			// set retained flag
			mqttMsg.setRetained(retained);
			// set quality of service
			mqttMsg.setQos(qos);
			try {
				client.publish(topic, mqttMsg);
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		} else {
			connectionLost(null);
		}
	}

	/**
	 * Checks if the MQTT client has an active connection
	 * 
	 * @return True if client is connected, false if not.
	 */
	private boolean isMqttConnected() {
		boolean connected = false;
		try {
			if ((client != null) && (client.isConnected())) {
				connected = true;
			}
		} catch (Exception e) {
			// swallowing the exception as it means the client is not connected
		}
		return connected;
	}

}
