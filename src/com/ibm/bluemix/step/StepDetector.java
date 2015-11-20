package com.ibm.bluemix.step;

public class StepDetector {	
	private Filter samples;
	private AccelSensorValue sample_result;
	private AccelSensorValue sample_new;
	private AccelSensorValue sample_old;
	
	private float new_x_min;
	private float new_x_max;
	private float x_min;
	private float x_max;
	private float x_treshold;
	private float x_peak_to_peak;
	
	private float new_y_min;
	private float new_y_max;
	private float y_min;
	private float y_max;
	private float y_treshold;
	private float y_peak_to_peak;
	
	private float new_z_min;
	private float new_z_max;
	private float z_min;
	private float z_max;
	private float z_treshold;
	private float z_peak_to_peak;
	
	private final float PRECISION = 0.1f;
	private int rescale_counter;
	private final int CYCLES_TO_RESCALE = 10; 
	
	private int steps;
	private static StepDetector instance;

	public static StepDetector getInstance(){
		if(instance == null) instance = new StepDetector();
		return instance;
	}

	public StepDetector() {
		samples = new Filter();
		sample_new = new AccelSensorValue();
		sample_old = new AccelSensorValue();
		
		x_min = 0;
		x_max = 0;
		x_treshold = 0;
		x_peak_to_peak = 0;
		
		y_min = 0;
		y_max = 0;
		y_treshold = 0;
		y_peak_to_peak = 0;
		
		z_min = 0;
		z_max = 0;
		z_treshold = 0;
		z_peak_to_peak = 0;
		
		steps = 0;
		rescale_counter = 0;
	}

	private boolean checkStep(float newStep, float oldStep){
		return     (((newStep >= 0) && (oldStep >= 0)) && ((oldStep - newStep)          >= PRECISION))
				|| (((newStep <  0) && (oldStep >= 0)) && ((oldStep + Math.abs(newStep) >= PRECISION))
				|| (((newStep >= 0) && (oldStep <  0)) && ((Math.abs(oldStep) + newStep) >= PRECISION))
				|| (((newStep <  0) && (oldStep <  0)) && ((oldStep - newStep)          >= PRECISION)));
	}


	public boolean detectStep(AccelSensorValue val) {
		float tmp = 0;

		// add new x,y,z-value to filter
		sample_result = samples.addValue(val);

		// only do something if the difference of any x,y,z-value
		// compared to the last cycle is >= the precision value

		if (checkStep(sample_result.getX_value(), sample_old.getX_value()) ||
				checkStep(sample_result.getY_value(), sample_old.getY_value()) ||
				checkStep(sample_result.getZ_value(),sample_old.getZ_value())){

			new_x_min = Math.min(new_x_min, sample_result.getX_value());
			new_y_min = Math.min(new_y_min, sample_result.getY_value());
			new_z_min = Math.min(new_z_min, sample_result.getZ_value());

			new_x_max = Math.max(new_x_max, sample_result.getX_value());
			new_y_max = Math.max(new_y_max, sample_result.getY_value());
			new_z_max = Math.max(new_z_max, sample_result.getZ_value());

			// rescale min/max/avg
			if(rescale_counter > CYCLES_TO_RESCALE) {
				x_min = new_x_min;
				x_max = new_x_max;
				x_treshold = (x_max + x_min) / 2;
				x_peak_to_peak = x_max + Math.abs(x_min);

				y_min = new_y_min;
				y_max = new_y_max;
				y_treshold = (y_max + y_min) / 2;
				y_peak_to_peak = y_max + Math.abs(y_min);

				z_min = new_z_min;
				z_max = new_z_max;
				z_treshold = (z_max + z_min) / 2;
				z_peak_to_peak = z_max + Math.abs(z_min);

				tmp = new_x_min;
				new_x_min = new_x_max;
				new_x_max = tmp;

				tmp = new_y_min;
				new_y_min = new_y_max;
				new_y_max = tmp;

				tmp = new_z_min;
				new_z_min = new_z_max;
				new_z_max = tmp;

				rescale_counter = 0;
			}
			rescale_counter++;


			sample_old = sample_new;
			sample_new = sample_result;

			float maxVal = Math.max(Math.max(x_peak_to_peak, y_peak_to_peak), z_peak_to_peak);
			if (x_peak_to_peak == maxVal && (sample_new.getX_value() < sample_old.getX_value()) && (sample_new.getX_value() <= x_treshold)){
				// x-axis has largest peak-to-peak value
				// step ?
				steps++;
				return true;
			} else if (y_peak_to_peak == maxVal && ((sample_new.getY_value() < sample_old.getY_value()) && (sample_new.getY_value() <= y_treshold))){
				steps++;
				return true;
			} else if(z_peak_to_peak == maxVal && (sample_new.getZ_value() < sample_old.getZ_value()) && (sample_new.getZ_value() <= z_treshold)) {
				// z-axis has largest peak-to-peak value
				// step ?
				steps++;
				return true;

			}
			return false;
		}
		return false;
	}
	
	public int getSteps() {
		return steps;
	}
}
