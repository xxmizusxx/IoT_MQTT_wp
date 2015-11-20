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

import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

public class AppTest {

	private MqttHandler handler;

	/**
	 * @param args
	 */
//	public static void main(String[] args) {
//		new AppTest().doApp();
//	}


	/**
	 * Run the app
	 */
	public void doApp() {
		//Read properties from the conf file
		Properties props = MqttUtil.readProperties("./MyData/app.conf");

		String org = props.getProperty("org");
		String id = props.getProperty("appid");
		String authmethod = props.getProperty("key");
		String authtoken = props.getProperty("token");
		//isSSL property
		String sslStr = props.getProperty("isSSL");
		boolean isSSL = false;
		if (sslStr.equals("T")) {
			isSSL = true;
		}
		
		System.out.println("org: " + org);
		System.out.println("id: " + id);
		System.out.println("authmethod: " + authmethod);
		System.out.println("authtoken" + authtoken);
		System.out.println("isSSL: " + isSSL);

		//Format: a:<orgid>:<app-id>
		String clientId = "a:" + org + ":" + id;
		String serverHost = org + MqttUtil.SERVER_SUFFIX;

		handler = new AppMqttHandler();
		handler.connect(serverHost, clientId, authmethod, authtoken, isSSL);

//		handler.subscribe("iot-2/type/" + MqttUtil.DEFAULT_DEVICE_TYPE + "/id/+/mon", 0);
//		handler.subscribe("iot-2/type/" + MqttUtil.DEFAULT_DEVICE_TYPE + "/id/+/mon", 0);

		//Subscribe Device Events
		//iot-2/type/<type-id>/id/<device-id>/evt/<event-id>/fmt/<format-id>
//		handler.subscribe("iot-2/type/" + MqttUtil.DEFAULT_DEVICE_TYPE + "/id/+/evt/" + MqttUtil.DEFAULT_EVENT_ID + "/fmt/json", 0);
		handler.subscribe("iot-2/type/sensor/id/+/evt/" + MqttUtil.DEFAULT_EVENT_ID + "/fmt/json", 0);
	}

	/**
	 * This class implements as the application MqttHandler
	 *
	 */
	private class AppMqttHandler extends MqttHandler {
		
		//Pattern to check whether the events comes from a device for an event
		Pattern pattern = Pattern.compile("iot-2/type/" + MqttUtil.DEFAULT_DEVICE_TYPE + "/id/(.+)/evt/"
				+ MqttUtil.DEFAULT_EVENT_ID + "/fmt/json");

		/**
		 * Once a subscribed message is received
		 */
		@Override
		public void messageArrived(String topic, MqttMessage mqttMessage)
				throws Exception {
			super.messageArrived(topic, mqttMessage);
//
//			Matcher matcher = pattern.matcher(topic);
//			if (matcher.matches()) {
//				String deviceid = matcher.group(1);
//				String payload = new String(mqttMessage.getPayload());
//
//				//Parse the payload in Json Format
//				JSONObject jsonObject = new JSONObject(payload);
//				JSONObject contObj = jsonObject.getJSONObject("d");
//				int count = contObj.getInt("count");
//				System.out.println("Receive count " + count + " from device "
//						+ deviceid);
//
//				//If count >=4, start a new thread to reset the count
//				if (count >= 4) {
//					new ResetCountThread(deviceid, 0).start();
//				}
//			}
		}
	}

	/**
	 * A thread to reset the count
	 *
	 */
	private class ResetCountThread extends Thread {
		private String deviceid = null;
		private int count = 0;

		public ResetCountThread(String deviceid, int count) {
			this.deviceid = deviceid;
			this.count = count;
		}

		public void run() {
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("cmd", "reset");
				jsonObj.put("count", count);
				jsonObj.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			System.out.println("Reset count for device " + deviceid);
			
			//Publish command to one specific device
			//iot-2/type/<type-id>/id/<device-id>/cmd/<cmd-id>/fmt/<format-id>
			handler.publish("iot-2/type/" + MqttUtil.DEFAULT_DEVICE_TYPE + "/id/" + deviceid + "/cmd/" + MqttUtil.DEFAULT_CMD_ID
					+ "/fmt/json", jsonObj.toString(), false, 0);
		}
	}

}
