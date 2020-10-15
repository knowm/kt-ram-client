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
	private float writeSeriesResistance = 20_000;
	private float readSenseGain = 40;

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

	public float measureForwardThreshold(int column, int row, boolean verbose) {

		// select the device
		client.clear();
		client.set(module, unit, array, column, row);

		// erase the device
		client.pulseWrite(-.75f, 500, writeSeriesResistance, 10);

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
		client.pulseWrite(.75f, 500, writeSeriesResistance, 10);

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
