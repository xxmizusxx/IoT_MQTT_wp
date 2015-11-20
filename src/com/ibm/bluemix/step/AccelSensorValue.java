package com.ibm.bluemix.step;

public class AccelSensorValue {
	private float x_value; 
	private float y_value; 
	private float z_value;
	
	public AccelSensorValue() {
		x_value = 0;
		y_value = 0;
		z_value = 0;
	}
	
	public AccelSensorValue(float x_value, float y_value, float z_value) {
		this.x_value = x_value;
		this.y_value = y_value;
		this.z_value = z_value;
	}
	
	public float getX_value() {
		return x_value;
	}
	public void setX_value(float x_value) {
		this.x_value = x_value;
	}
	
	public float getY_value() {
		return y_value;
	}
	public void setY_value(float y_value) {
		this.y_value = y_value;
	}
	
	public float getZ_value() {
		return z_value;
	}
	public void setZ_value(float z_value) {
		this.z_value = z_value;
	}
}
