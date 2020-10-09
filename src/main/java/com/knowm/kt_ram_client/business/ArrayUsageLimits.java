package com.knowm.kt_ram_client.business;

public class ArrayUsageLimits {

	private float maxNegVolts = 0;
	private float maxPosVolts = 0;
	private float minSeriesResistance = 0;

	public float getMaxNegVolts() {
		return maxNegVolts;
	}

	public void setMaxNegVolts(float maxNegVolts) {
		this.maxNegVolts = maxNegVolts;
	}

	public float getMaxPosVolts() {
		return maxPosVolts;
	}

	public void setMaxPosVolts(float maxPosVolts) {
		this.maxPosVolts = maxPosVolts;
	}

	public float getMinSeriesResistance() {
		return minSeriesResistance;
	}

	public void setMinSeriesResistance(float minSeriesResistance) {
		this.minSeriesResistance = minSeriesResistance;
	}

	public boolean isVoltagePermitted(float voltage) {

		if (voltage >= maxNegVolts && voltage <= maxPosVolts) {
			return true;
		}
		return false;
	}

	public boolean isSeriesResistorPermitted(float seriesResistor) {
		return seriesResistor >= this.minSeriesResistance;
	}

}
