package com.ibm.bluemix.step;

import java.util.LinkedList;
import java.util.List;

public class Filter {
	private final int FILTER_SIZE = 1;
	private List<AccelSensorValue> filter;
	private AccelSensorValue filter_res;
	
	public Filter() {
		filter = new LinkedList<AccelSensorValue>();
		filter_res = new AccelSensorValue();
	}
	
	/*
	 * takes a new pair of x,y,z-values
	 * returns a filtered pair of x,y,z-values
	 */
	public AccelSensorValue addValue(AccelSensorValue new_val) {
		float tmp_x_value = 0;
		float tmp_y_value = 0;
		float tmp_z_value = 0;
		
		// add element at head position
		filter.add(0, new_val);
		
		// remove tail if too many elements are present
		if(filter.size() > FILTER_SIZE) {
			filter.remove(FILTER_SIZE);
		}
		
		// sum up all elements
		for(AccelSensorValue v: filter) {
			tmp_x_value += v.getX_value();
			tmp_y_value += v.getY_value();
			tmp_z_value += v.getZ_value();
		}
		
		//  build averages
		if(filter.size() == FILTER_SIZE) {
			filter_res = new AccelSensorValue(tmp_x_value/FILTER_SIZE, tmp_y_value/FILTER_SIZE, tmp_z_value/FILTER_SIZE);
		} else {
			filter_res = new AccelSensorValue(tmp_x_value/filter.size(), tmp_y_value/filter.size(), tmp_z_value/filter.size());
		}
		
		return filter_res;
	}
}
