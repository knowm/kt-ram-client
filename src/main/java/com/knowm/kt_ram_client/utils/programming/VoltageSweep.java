package com.knowm.kt_ram_client.utils.programming;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.utils.ArrayUtils;

public class VoltageSweep extends DeviceProgrammer {

	private float maxWriteAmplitude = .5f;

	private float writeAmplitude = .25f;
	private float eraseAmplitude = -.125f;

	private float writeSeriesResistance = 20_000;
	private float eraseSeriesResistance = 10_000;

	public VoltageSweep(KTRAMServerClient client) {
		super(client);

	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	/*
	 * 
	 * Applies a gradually increasing voltage until threshold is hit. If it
	 * overshoots, it applies erase pulses and tried again.
	 * 
	 * 
	 */

	@Override
	public void programStep(float measuredResistance, float difference, int step) {

		if (difference > 0) {// write
			println(ArrayUtils.formatPulses(10, 1, writeAmplitude, 500));
			client.pulseWrite(writeAmplitude, 500, writeSeriesResistance, 1);
			writeAmplitude += .005f;
		} else {// erase
			println(ArrayUtils.formatPulses(10, 1, eraseAmplitude, 500));
			client.pulseWrite(eraseAmplitude, 500, eraseSeriesResistance, 1);
			writeAmplitude -= .005f;
		}

		writeAmplitude = writeAmplitude > maxWriteAmplitude ? maxWriteAmplitude : writeAmplitude;

	}

	public float getWriteAmplitude() {
		return writeAmplitude;
	}

	public void setWriteAmplitude(float writeAmplitude) {
		this.writeAmplitude = writeAmplitude;
	}

	public float getEraseAmplitude() {
		return eraseAmplitude;
	}

	public void setEraseAmplitude(float eraseAmplitude) {
		this.eraseAmplitude = eraseAmplitude;
	}

	public float getWriteSeriesResistance() {
		return writeSeriesResistance;
	}

	public void setWriteSeriesResistance(float writeSeriesResistance) {
		this.writeSeriesResistance = writeSeriesResistance;
	}

	public float getEraseSeriesResistance() {
		return eraseSeriesResistance;
	}

	public void setEraseSeriesResistance(float eraseSeriesResistance) {
		this.eraseSeriesResistance = eraseSeriesResistance;
	}

}
