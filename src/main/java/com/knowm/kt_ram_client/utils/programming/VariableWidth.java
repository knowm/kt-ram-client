package com.knowm.kt_ram_client.utils.programming;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.utils.ArrayUtils;

public class VariableWidth extends DeviceProgrammer {

	// the write and erase voltages to use for programming
	private float writeAmplitude = .45f;
	// private float eraseAmplitude = -.125f;
	private float eraseAmplitude = -.2f;

	private float writeSeriesResistance = 20_000;
	private float eraseSeriesResistance = 10_000;

	private float e_comp = 1000;
	private float w_comp = 1000;

	public VariableWidth(KTRAMServerClient client) {
		super(client);

	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	/*
	 * 
	 * 
	 * Applies a pulse with a width that is proportional to difference between
	 * target and measured resistance. Increase pulse width each time target
	 * threshold is not passed ("compensation")
	 * 
	 * 
	 */

	@Override
	public void programStep(float measuredResistance, float difference, int step) {

		if (difference > 0) {// write
			float width = getWidth(difference, w_comp);
			println(ArrayUtils.formatPulses(10, 1, writeAmplitude, width));
			client.pulseWrite(writeAmplitude, width, writeSeriesResistance, 1);
			w_comp *= 2;
			e_comp = 1000;

		} else {// erase
			float width = getWidth(difference, e_comp);
			println(ArrayUtils.formatPulses(10, 1, eraseAmplitude, width));
			client.pulseWrite(eraseAmplitude, width, eraseSeriesResistance, 1);
			w_comp = 1000;
			e_comp *= 2;
		}

	}

	private float getWidth(float difference, float compensation) {
		float width = ((Math.abs(difference) + compensation) / (float) 10000.0);
		width = width > 500 ? 500 : width;
		width = width < 2.5f ? 2.5f : width;
		return width;
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
