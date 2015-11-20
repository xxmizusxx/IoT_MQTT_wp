package com.ibm.bluemixmqtt;

import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
import org.eclipse.paho.client.mqttv3.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by final-work on 19.11.15.
 */
public class Test {

    private MqttHandler handler = null;
    private MqttClient mqttClient = null;

    private int count = 0;
    public static void main(String[] args) {
        new Test().doDevice();
    }

    /**
         * Run the device
         */
        public void doDevice() {
            //Read properties from the conf file
            Properties props = MqttUtil.readProperties("./MyData/device.conf");

            String org = props.getProperty("org");
            String id = props.getProperty("deviceid");
            String authmethod = "use-token-auth";
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
            System.out.println("authtoken: " + authtoken);
            System.out.println("isSSL: " + isSSL);

            String serverHost = org + MqttUtil.SERVER_SUFFIX;

            //Format: d:<orgid>:<type-id>:<divice-id>
            String clientId = "d:" + org + ":" + MqttUtil.DEFAULT_DEVICE_TYPE + ":" + id;

            handler = new DeviceMqttHandler();
            handler.connect(serverHost, clientId, authmethod, authtoken, isSSL);


            while (count < 10) {

                //TODO Format the Json String
                JSONObject contObj = new JSONObject();
                JSONObject jsonObj = new JSONObject();
                try {
                    contObj.put("count", count);
                    contObj.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(new Date()));
                    jsonObj.put("d", contObj);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

//                handler.subscribe("iot-2/cmd/status/fmt/json", 0);
                handler.publish("iot-2/evt/status/fmt/json", jsonObj.toString(), false, 0);



//                handler.subscribe("iot-2/cmd/Mqtt/id/evt/status/fmt/json", 0);
//              handler.subscribe("cmd", 0);

                System.out.println();

                count++;

                try {
                    Thread.sleep(1 * 1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            handler.disconnect();
    }



    private class DeviceMqttHandler extends MqttHandler {

//        @Override
//        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
////            super.messageArrived(topic, mqttMessage);
////
////            //Check whether the event is a command event from app
////            if (topic.equals("iot-2/cmd/" + MqttUtil.DEFAULT_CMD_ID
////                    + "/fmt/json")) {
////                String payload = new String(mqttMessage.getPayload());
////                JSONObject jsonObject = new JSONObject(payload);
////                String cmd = jsonObject.getString("cmd");
////                //Reset the count
////                if (cmd != null && cmd.equals("reset")) {
////                    int resetcount = jsonObject.getInt("count");
//////                    count = resetcount;
////                    System.out.println("Count is reset to " + resetcount);
////                }
////            }
//            System.out.println(mqttMessage.getPayload());
//        }

    }

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

            Matcher matcher = pattern.matcher(topic);
            if (matcher.matches()) {
                String deviceid = matcher.group(1);
                String payload = new String(mqttMessage.getPayload());

                //Parse the payload in Json Format
                JSONObject jsonObject = new JSONObject(payload);
                JSONObject contObj = jsonObject.getJSONObject("d");
                int count = contObj.getInt("count");
                System.out.println("Receive count " + count + " from device "
                        + deviceid);

                //If count >=4, start a new thread to reset the count
                if (count >= 4) {
                    new ResetCountThread(deviceid, 0).start();
                }
            }
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
            handler.publish("iot-2/type/" + MqttUtil.DEFAULT_DEVICE_TYPE
                    + "/id/" + deviceid + "/cmd/" + MqttUtil.DEFAULT_CMD_ID
                    + "/fmt/json", jsonObj.toString(), false, 0);
        }
    }

}





