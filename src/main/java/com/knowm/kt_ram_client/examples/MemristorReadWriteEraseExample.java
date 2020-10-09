package com.knowm.kt_ram_client.examples;

import java.util.List;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.business.Activation;
import com.knowm.kt_ram_client.business.ProductInfo;
import com.knowm.kt_ram_client.business.ReadFormat;
import com.knowm.kt_ram_client.utils.ArrayUtils;
import com.knowm.kt_ram_client.utils.AveMaxMinVar;

public class MemristorReadWriteEraseExample extends HostInfo {

	static float readAmplitude = .15f;
	static float readWidth = 500;
	static float readSeriesResistance = 50_000;
	static float writeSeriesResistance = 20_000;
	static float readSenseGain = 30;

	public static void main(String[] args) {

		try {
			KTRAMServerClient.initClientPool(1, username, password);

			// readEraseReadWriteSingleDevice();
			readWholeArray();

			KTRAMServerClient.shutdownClient();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * This example uses an activation list to mitigate latency of http requests
	 * when performing similar read or write operations across many devices
	 * seperatly. Rather than send separate API calls for each element, a single
	 * request is sent for all elements listed in the activation list.
	 * 
	 * Note: you must have permission to access the specified array for this to
	 * work.
	 */
	public static void readWholeArray() {
		int module = 0;
		int unit = 0;
		int array = 0;

		// get the array size
		ProductInfo productInfo = KTRAMServerClient.getModuleInfo(module, host);
		int[] arraySize = ArrayUtils.getArraySizeFromProductInfo(productInfo);// [cols,rows]

		// get activations for each element that are to be read
		List<Activation> activations = ArrayUtils.getActivationsForWholeArray(module, unit, array, productInfo);

		// read over all activations
		List<float[]> reads = KTRAMServerClient.pulseReadActivations(readAmplitude, readWidth, readSeriesResistance,
				readSenseGain, 1, ReadFormat.RESISTANCE, activations, false, host);

		float[][] readMatrix = new float[arraySize[0]][arraySize[1]];
		ArrayUtils.fillArrayFromActivations(activations, reads, readMatrix);
		ArrayUtils.printArrayResistance(readMatrix);

	}

	/*
	 * read, write and erase a single device.
	 * 
	 * Note: you must have permission to access the specified array for this to
	 * work.
	 * 
	 */
	public static void readEraseReadWriteSingleDevice() {

		int module = 0;
		int unit = 0;
		int array = 0;
		int column = 1;
		int row = 0;

		KTRAMServerClient.clear(host);
		KTRAMServerClient.set(module, unit, array, column, row, host);

		read("Initial:     ", 10, ReadFormat.RESISTANCE);

		// erase
		KTRAMServerClient.pulseWrite(-.5f, 500, writeSeriesResistance, 1, host);

		read("After Erase: ", 10, ReadFormat.RESISTANCE);

		// write

		int numWritePulses = 100;
		KTRAMServerClient.pulseWrite(.75f, 500, writeSeriesResistance, numWritePulses, host);
		read("After " + numWritePulses + " Write Pulses: ", 10, ReadFormat.RESISTANCE);

	}

	private static void read(String s, int numReads, ReadFormat format) {
		// perform 10 read operations, print average and standard deviation
		float[] reads = KTRAMServerClient.pulseRead(readAmplitude, readWidth, readSeriesResistance, readSenseGain, 10,
				format, host);
		AveMaxMinVar readStatistics = new AveMaxMinVar(reads);
		System.out.println(s + readStatistics.getAve() + " ± " + readStatistics.getStd());
	}

}
