package com.knowm.kt_ram_client.business;

public class CalibrationProfile {
	private float readVoltage = .2f;
	private float readPulseWidth = 300;
	private float gain = 35;
	private float seriesResistor = 20_000;
	private float[][] calibrationData;
	private float[] curveFitParameters = new float[] { 5521f, 55f, 79E-4f, 2f };

	public CalibrationProfile() {

	}

	public float interpolateCurrent(float v) {
		float r = interpolateResistance(v);
		float combinedResistance = seriesResistor + r;
		float current = readVoltage / combinedResistance;
		return current;
	}

	public float interpolateConductance(float v) {
		float r = interpolateResistance(v);
		return 1 / r;
	}

	public float interpolateResistance(float vRead) {
		return curve(vRead, curveFitParameters);
	}

	public static float curve(float v, float[] params) {
		return params[0] * (params[1] / (float) Math.pow(v, params[3]) + params[2]);
	}

	public CalibrationProfile(float voltage, float pulseWidth, float gain, float seriesResistor) {
		this.readVoltage = voltage;
		this.readPulseWidth = pulseWidth;
		this.gain = gain;
		this.seriesResistor = seriesResistor;

	}

	public float getReadVoltage() {
		return readVoltage;
	}

	public void setReadVoltage(float readVoltage) {
		this.readVoltage = readVoltage;
	}

	public float getReadPulseWidth() {
		return readPulseWidth;
	}

	public void setReadPulseWidth(float readPulseWidth) {
		this.readPulseWidth = readPulseWidth;
	}

	public float getGain() {
		return gain;
	}

	public void setGain(float gain) {
		this.gain = gain;
	}

	public float getSeriesResistor() {
		return seriesResistor;
	}

	public void setSeriesResistor(float seriesResistor) {
		this.seriesResistor = seriesResistor;
	}

	public float[][] getCalibrationData() {
		return calibrationData;
	}

	public void setCalibrationData(float[][] calibrationData) {
		this.calibrationData = calibrationData;
	}

	public float[] getCurveFitParameters() {
		return curveFitParameters;
	}

	public void setCurveFitParameters(float[] curveFitParameters) {
		this.curveFitParameters = curveFitParameters;
	}

}
