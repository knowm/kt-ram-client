package com.knowm.kt_ram_client.utils;

import java.util.List;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.business.Activation;
import com.knowm.kt_ram_client.business.ProductInfo;
import com.knowm.kt_ram_client.business.ReadFormat;

public class ArrayDoctor {

	private float readAmplitude = .15f;
	private float readWidth = 500;
	private float readSeriesResistance = 50_000;
	private float writeSeriesResistance = 30_000;
	private float readSenseGain = 40;

	private float softResetVoltage = -.125f;
	private float hardResetVoltage = -1.25f;

	private float resetSeriesResistance = 10_000;

	private float formingVoltagePositive = .5f;
	private float formingVoltageNegative = -1.25f;
	private float formingSeriesResistance = 20_000;

	private KTRAMServerClient client;
	private int module;
	private int unit;
	private int array;

	public ArrayDoctor(KTRAMServerClient client, int module, int unit, int array) {
		this.client = client;
		this.module = module;
		this.unit = unit;
		this.array = array;
	}

	public void form(int column, int row, boolean verbose) {
		client.clear();
		client.set(module, unit, array, column, row);

		for (int i = 0; i < 10; i++) {
			client.pulseWrite(formingVoltagePositive, 500, formingSeriesResistance, 5);
			client.pulseWrite(formingVoltageNegative, 500, resetSeriesResistance, 5);
		}

		if (verbose) {
			System.out.println("Device has been formed at " + ArrayUtils.formatV(5, formingVoltagePositive) + " / "
					+ ArrayUtils.formatV(5, formingVoltageNegative));
		}

	}

	public float hardReset(int column, int row, boolean verbose) {

		client.clear();
		client.set(module, unit, array, column, row);
		client.pulseWrite(hardResetVoltage, 500, resetSeriesResistance, 10);

		float r = client.pulseRead(readAmplitude, readWidth, readSeriesResistance, readSenseGain, 1,
				ReadFormat.RESISTANCE)[0];

		if (verbose) {
			System.out.println("Resistance after hard reset: " + ArrayUtils.formatR(5, r));
		}

		return r;

	}

	public float softReset(int column, int row, boolean verbose) {

		client.clear();
		client.set(module, unit, array, column, row);
		client.pulseWrite(softResetVoltage, 500, resetSeriesResistance, 50);

		float r = client.pulseRead(readAmplitude, readWidth, readSeriesResistance, readSenseGain, 1,
				ReadFormat.RESISTANCE)[0];

		if (verbose) {
			System.out.println("Resistance after soft reset: " + ArrayUtils.formatR(5, r));
		}

		return r;

	}

	public float[] measureLrsHrs(float writeAmplitude, float writeSeriesResistance, float eraseAmplitude,
			float eraseSeriesResistance, int column, int row, boolean verbose) {

		// select the device
		client.clear();
		client.set(module, unit, array, column, row);
		client.pulseWrite(writeAmplitude, 500, writeSeriesResistance, 100);

		float[] lrs_hrs = new float[2];

		// perform multiple reads and average the results
		float[] reads = client.pulseRead(.15f, 500, 50_000, 50, 5, ReadFormat.RESISTANCE);
		AveMaxMinVar stat = new AveMaxMinVar(reads);

		lrs_hrs[0] = stat.getAve();

		if (verbose) {
			System.out.println("LRS (50@" + ArrayUtils.formatV(5, writeAmplitude) + "): "
					+ ArrayUtils.formatR(5, stat.getAve()) + " ± " + ArrayUtils.formatR(5, stat.getStd()));
		}

		client.pulseWrite(eraseAmplitude, 500, eraseSeriesResistance, 100);

		reads = client.pulseRead(.15f, 500, 50_000, 50, 5, ReadFormat.RESISTANCE);
		stat = new AveMaxMinVar(reads);

		System.out.println("HRS(50@" + ArrayUtils.formatV(5, eraseAmplitude) + "): "
				+ ArrayUtils.formatR(5, stat.getAve()) + " ± " + ArrayUtils.formatR(5, stat.getStd()));

		lrs_hrs[1] = stat.getAve();

		return lrs_hrs;

	}

	public float measureForwardThreshold(int column, int row, boolean verbose) {

		// select the device
		client.clear();
		client.set(module, unit, array, column, row);

		// erase the device
		client.pulseWrite(-1f, 500, writeSeriesResistance, 10);

		float[] amplitude = new float[50];
		for (int i = 0; i < amplitude.length; i++) {
			amplitude[i] = i * .01f + .1f;
		}

		float initialResistance = readAverage(10);
		if (verbose) {
			System.out.println("Amplitude : Resistance : % Change");
		}

		float amp = 0;
		float r = initialResistance;

		for (int i = 0; i < amplitude.length; i++) {
			// apply write pulses
			client.pulseWrite(amplitude[i], 500, writeSeriesResistance, 10);

			// get resistance
			r = readAverage(10);

			// get change in resistance from initial
			float p = 1 - r / initialResistance;
			if (verbose) {
				System.out.println(ArrayUtils.formatV(9, amplitude[i]) + " : " + ArrayUtils.formatR(10, r) + " : "
						+ ArrayUtils.formatP(8, p));
			}

			// if change exceeds 5%, call this the threshold
			if (p > .1) {
				amp = amplitude[i];
				System.out.println("THRESHOLD FOUND");
				break;
			}
		}
		if (amp == 0) {
			throw new RuntimeException("Threshold could not be determined.");
		}

		// determine voltage drop across memristor
		float vMem = amp * (r / (readSeriesResistance + r));
		return vMem;

	}

	public float measureReverseThreshold(int column, int row, boolean verbose) {

		// select the device
		client.clear();
		client.set(module, unit, array, column, row);

		// write the device
		client.pulseWrite(.5f, 500, writeSeriesResistance, 10);

		float[] amplitude = new float[75];
		for (int i = 0; i < amplitude.length; i++) {
			amplitude[i] = -i * .01f;
		}

		float initialResistance = readAverage(10);
		if (verbose) {
			System.out.println("Amplitude : Resistance : % Change");
		}

		float amp = 0;
		float r = initialResistance;

		for (int i = 0; i < amplitude.length; i++) {
			// apply erase pulses
			client.pulseWrite(amplitude[i], 500, writeSeriesResistance, 10);

			// get resistance
			r = readAverage(10);

			// get change in resistance from initial
			float p = 1 - r / initialResistance;
			if (verbose) {
				System.out.println(ArrayUtils.formatV(9, amplitude[i]) + " : " + ArrayUtils.formatR(10, r) + " : "
						+ ArrayUtils.formatP(8, p));
			}

			// if change exceeds -5%, call this the threshold
			if (p < -.4) {
				amp = amplitude[i];
				System.out.println("THRESHOLD FOUND");
				break;
			}
		}

		if (amp == 0) {
			throw new RuntimeException("Threshold could not be determined.");
		}

		// determine voltage drop across memristor
		float vMem = amp * (r / (readSeriesResistance + r));
		return vMem;

	}

	private float readAverage(int numToAverage) {
		float[] r = client.pulseRead(readAmplitude, readWidth, readSeriesResistance, readSenseGain, numToAverage,
				ReadFormat.RESISTANCE);
		AveMaxMinVar stat = new AveMaxMinVar(r);
		return stat.getAve();
	}

	public float[][][] checkup() {

		System.out.println("ArrayDoctor.checkup: " + module + "m, " + unit + "u, " + array + "a");
		// get the array size
		ProductInfo productInfo = client.getModuleInfo(module);
		System.out.println(productInfo);
		int[] arraySize = ArrayUtils.getArraySizeFromProductInfo(productInfo);// [cols,rows]
		List<Activation> activations = ArrayUtils.getActivationsForWholeArray(module, unit, array, productInfo);

		// erase
		client.pulseWriteActivations(-.75f, 500, writeSeriesResistance, 10, activations, false);

		// read HRS
		List<float[]> reads_hrs = client.pulseReadActivations(readAmplitude, readWidth, readSeriesResistance,
				readSenseGain, 1, ReadFormat.RESISTANCE, activations, false);
		float[][] hrs = new float[arraySize[0]][arraySize[1]];
		ArrayUtils.fillArrayFromActivations(activations, reads_hrs, hrs);

		// write
		client.pulseWriteActivations(.75f, 500, writeSeriesResistance, 10, activations, false);

		// read LRS
		List<float[]> reads_lrs = client.pulseReadActivations(readAmplitude, readWidth, readSeriesResistance,
				readSenseGain, 1, ReadFormat.RESISTANCE, activations, false);
		float[][] lrs = new float[arraySize[0]][arraySize[1]];
		ArrayUtils.fillArrayFromActivations(activations, reads_lrs, lrs);

		float[][] dr = new float[arraySize[0]][arraySize[1]];
		for (int col = 0; col < lrs.length; col++) {
			for (int row = 0; row < lrs[col].length; row++) {
				dr[col][row] = hrs[col][row] - lrs[col][row];
			}
		}

		float[][][] result = new float[3][][];
		result[0] = lrs;
		result[1] = hrs;
		result[2] = dr;

		return result;
	}

}
