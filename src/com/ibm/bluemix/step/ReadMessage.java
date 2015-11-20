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

}
