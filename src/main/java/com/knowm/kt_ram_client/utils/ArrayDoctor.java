package com.knowm.kt_ram_client.utils;

import java.util.List;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.business.Activation;
import com.knowm.kt_ram_client.business.ProductInfo;
import com.knowm.kt_ram_client.business.ReadFormat;

public class ArrayDoctor {

	static float readAmplitude = .15f;
	static float readWidth = 500;
	static float readSeriesResistance = 50_000;
	static float writeSeriesResistance = 20_000;
	static float readSenseGain = 30;

	public static float measureForwardThreshold(int module, int unit, int array, int column, int row, String host,
			boolean verbose) {

		// select the device
		KTRAMServerClient.clear(host);
		KTRAMServerClient.set(module, unit, array, column, row, host);

		// erase the device
		KTRAMServerClient.pulseWrite(-.75f, 500, writeSeriesResistance, 10, host);

		float[] amplitude = new float[50];
		for (int i = 0; i < amplitude.length; i++) {
			amplitude[i] = i * .01f + .1f;
		}

		float initialResistance = readAverage(10, host);
		if (verbose) {
			System.out.println("Amplitude : Resistance : % Change");
		}

		float amp = 0;
		float r = initialResistance;

		for (int i = 0; i < amplitude.length; i++) {
			// apply write pulses
			KTRAMServerClient.pulseWrite(amplitude[i], 500, writeSeriesResistance, 10, host);

			// get resistance
			r = readAverage(10, host);

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

	public static float measureReverseThreshold(int module, int unit, int array, int column, int row, String host,
			boolean verbose) {

		// select the device
		KTRAMServerClient.clear(host);
		KTRAMServerClient.set(module, unit, array, column, row, host);

		// write the device
		KTRAMServerClient.pulseWrite(.75f, 500, writeSeriesResistance, 10, host);

		float[] amplitude = new float[75];
		for (int i = 0; i < amplitude.length; i++) {
			amplitude[i] = -i * .01f;
		}

		float initialResistance = readAverage(10, host);
		if (verbose) {
			System.out.println("Amplitude : Resistance : % Change");
		}

		float amp = 0;
		float r = initialResistance;

		for (int i = 0; i < amplitude.length; i++) {
			// apply erase pulses
			KTRAMServerClient.pulseWrite(amplitude[i], 500, writeSeriesResistance, 10, host);

			// get resistance
			r = readAverage(10, host);

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

	private static float readAverage(int numToAverage, String host) {
		float[] r = KTRAMServerClient.pulseRead(readAmplitude, readWidth, readSeriesResistance, readSenseGain, 10,
				ReadFormat.RESISTANCE, host);
		AveMaxMinVar stat = new AveMaxMinVar(r);
		return stat.getAve();
	}

	public static float[][][] checkup(int module, int unit, int array, String host) {

		System.out.println("ArrayDoctor.checkup: " + module + "m, " + unit + "u, " + array + "a");
		// get the array size
		ProductInfo productInfo = KTRAMServerClient.getModuleInfo(module, host);
		System.out.println(productInfo);
		int[] arraySize = ArrayUtils.getArraySizeFromProductInfo(productInfo);// [cols,rows]
		List<Activation> activations = ArrayUtils.getActivationsForWholeArray(module, unit, array, productInfo);

		// erase
		KTRAMServerClient.pulseWriteActivations(-.75f, 500, writeSeriesResistance, 10, activations, false, host);

		// read HRS
		List<float[]> reads_hrs = KTRAMServerClient.pulseReadActivations(readAmplitude, readWidth, readSeriesResistance,
				readSenseGain, 1, ReadFormat.RESISTANCE, activations, false, host);
		float[][] hrs = new float[arraySize[0]][arraySize[1]];
		ArrayUtils.fillArrayFromActivations(activations, reads_hrs, hrs);

		// write
		KTRAMServerClient.pulseWriteActivations(.75f, 500, writeSeriesResistance, 10, activations, false, host);

		// read LRS
		List<float[]> reads_lrs = KTRAMServerClient.pulseReadActivations(readAmplitude, readWidth, readSeriesResistance,
				readSenseGain, 1, ReadFormat.RESISTANCE, activations, false, host);
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
