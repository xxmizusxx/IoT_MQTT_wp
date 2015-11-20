package com.ibm.bluemix.step;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by mizus on 19.11.15.
 */
public class ReadMessage {

    private static ReadMessage instance;

    private ReadMessage(){

    }

    public static ReadMessage getInstance(){
        if (instance == null) instance = new ReadMessage();
        return instance;
    }

    private static String json = "\"d\":{" +
            "\"gyro_x\":\"0,00\"," +
            "\"compass_y\":\"0,00\"," +
            "\"humidity\":\"68,27\"," +
            "\"acc_y\":\"-0,00\"," +
            "\"reed_relay\":\"0\"," +
            "\"object_temp\":\"16,59\"," +
            "\"acc_x\":\"0,00\"," +
            "\"light\":\"32,17\"," +
            "\"key_2\":\"0\"," +
            "\"key_1\":\"0\"," +
            "\"gyro_z\":\"0,00\"," +
            "\"compass_x\":\"0,00\"," +
            "\"ambient_temp\":\"23,56\"," +
            "\"air_pressure\":\"889,04\"," +
            "\"gyro_y\":\"0,00\"," +
            "\"compass_z\":\"0,00\"," +
            "\"acc_z\":\"-0,00\"" +
            "}" ;

    private static String json2 = "\"d\":{" +
            "\"gyro_x\":\"0,00\"," +
            "\"compass_y\":\"0,00\"," +
            "\"humidity\":\"68,27\"," +
            "\"acc_y\":\"-0,00\"," +
            "\"reed_relay\":\"0\"," +
            "\"object_temp\":\"16,59\"," +
            "\"acc_x\":\"7,00\"," +
            "\"light\":\"32,17\"," +
            "\"key_2\":\"0\"," +
            "\"key_1\":\"0\"," +
            "\"gyro_z\":\"0,00\"," +
            "\"compass_x\":\"0,00\"," +
            "\"ambient_temp\":\"23,56\"," +
            "\"air_pressure\":\"889,04\"," +
            "\"gyro_y\":\"0,00\"," +
            "\"compass_z\":\"0,00\"," +
            "\"acc_z\":\"-0,00\"" +
            "}" ;

    public AccelSensorValue readMessage(String str){

        if (str.contains("SensorTag"))  return new AccelSensorValue();

        Gson gson = new Gson();

        String json =  "{" + str.replaceAll("\"d\":", "").replaceAll("}","").replace("{","").replaceAll("\n","") +"}";
        System.out.println("A " + json);
        JsonObject t = gson.fromJson(json, JsonElement.class).getAsJsonObject();

        JsonElement element_X = t.get("acc_x");
        JsonElement element_Y = t.get("acc_y");
        JsonElement element_Z = t.get("acc_z");


        float x = getVal(element_X.getAsString().replaceAll(",", "."));
        float y = getVal(element_Y.getAsString().replaceAll(",", "."));
        float z = getVal(element_Z.getAsString().replaceAll(",", "."));
        return new AccelSensorValue(x, y, z);
    }

    private float getVal(String a){
        try{
            return Float.valueOf(a).floatValue();
        } catch (NumberFormatException e){
            System.out.println("bingo");
            return 0.0f;
        }
    }


    public static void main(String[] args) {
        AccelSensorValue g = ReadMessage.getInstance().readMessage(ReadMessage.json);
        boolean temr = StepDetector.getInstance().detectStep(g);

        System.out.println("===> " + temr);

        g = ReadMessage.getInstance().readMessage(ReadMessage.json2);
        temr = StepDetector.getInstance().detectStep(g);

        System.out.println("===> " + temr);
    }

}
