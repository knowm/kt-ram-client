package com.knowm.kt_ram_client.examples;

import java.util.List;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.business.Activation;
import com.knowm.kt_ram_client.business.ProductInfo;
import com.knowm.kt_ram_client.business.ReadFormat;
import com.knowm.kt_ram_client.utils.ArrayUtils;
import com.knowm.kt_ram_client.utils.AveMaxMinVar;

public class MemristorReadWriteEraseExample extends HostInfo {

	private KTRAMServerClient client;
	private int module;
	private int unit;
	private int array;

	private ProductInfo productInfo;
	private int[] arraySize;;// [cols,rows]

	private float readAmplitude = .15f;
	private float readWidth = 500;
	private float readSeriesResistance = 50_000;
	private float writeSeriesResistance = 50_000;
	private float readSenseGain = 50;
	private float eraseAmplitude = -.75f;

	public static void main(String[] args) {

		try {

			KTRAMServerClient client = new KTRAMServerClient(host, username, password);
			int module = 0;
			int unit = 0;
			int array = 0;

			MemristorReadWriteEraseExample example = new MemristorReadWriteEraseExample(client, module, unit, array);

			// example.readEraseReadWriteSingleDevice(3, 3);

//			example.writeSingleDevice(.4f, 500, 10, 1, 3);
//			example.writeSingleDevice(.4f, 500, 10, 3, 3);
//			example.writeSingleDevice(.4f, 500, 10, 5, 3);
//			example.writeSingleDevice(.4f, 500, 10, 7, 3);

			// example.writeSingleDevice(-.1f, 500, 10, 1, 3);

			// example.writeWholeArray(-.75f, 1);
			example.readWholeArray();

			client.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public MemristorReadWriteEraseExample(KTRAMServerClient client, int module, int unit, int array) {

		this.client = client;
		this.module = module;
		this.unit = unit;
		this.array = array;

		productInfo = client.getModuleInfo(module);
		arraySize = ArrayUtils.getArraySizeFromProductInfo(productInfo);// [cols,rows]

	}

	/*
	 * This example uses an activation list to mitigate latency of http requests
	 * when performing similar read or write operations across many devices
	 * separately. Rather than send separate API calls for each element, a single
	 * request is sent for all elements listed in the activation list.
	 * 
	 * Note: You must have permission to access the specified array for this to
	 * work.
	 */
	public void readWholeArray() {

		// get the array size
		ProductInfo productInfo = client.getModuleInfo(module);
		int[] arraySize = ArrayUtils.getArraySizeFromProductInfo(productInfo);// [cols,rows]

		// get activations for each element that are to be read
		List<Activation> activations = ArrayUtils.getActivationsForWholeArray(module, unit, array, productInfo);

		// read over all activations
		List<float[]> reads = client.pulseReadActivations(readAmplitude, readWidth, readSeriesResistance, readSenseGain,
				1, ReadFormat.RESISTANCE, activations, false);

		float[][] readMatrix = new float[arraySize[0]][arraySize[1]];
		ArrayUtils.fillArrayFromActivations(activations, reads, readMatrix);
		ArrayUtils.printArrayResistance(readMatrix);

	}

	/*
	 * This example uses an activation list to mitigate latency of http requests
	 * when performing similar read or write operations across many devices
	 * separately. Rather than send separate API calls for each element, a single
	 * request is sent for all elements listed in the activation list.
	 * 
	 * Note: you must have permission to access the specified array for this to
	 * work.
	 */
	public void writeWholeArray(float amplitude, int numPulses) {

		ProductInfo productInfo = client.getModuleInfo(module);

		// get activations for each element that are to be read
		List<Activation> activations = ArrayUtils.getActivationsForWholeArray(module, unit, array, productInfo);

		// read over all activations
		client.pulseWriteActivations(eraseAmplitude, readWidth, writeSeriesResistance, 1, activations, false);

		System.out.println("Array " + module + ":" + unit + ":" + array + " has been written with " + numPulses
				+ " pulses @ " + ArrayUtils.formatV(5, amplitude));

	}

	/*
	 * read, write and erase a single device. Note: you must have permission to
	 * access the specified array for this to work.
	 * 
	 */
	public void readEraseReadWriteSingleDevice(int column, int row) {

		client.clear();
		client.set(module, unit, array, column, row);

		readAverage(client, "Initial: ", 10, ReadFormat.RESISTANCE);

		// erase
		client.pulseWrite(-.5f, 500, writeSeriesResistance, 1);

		readAverage(client, "After 1 erase pulse: ", 10, ReadFormat.RESISTANCE);

		// write

		int numWritePulses = 100;
		client.pulseWrite(.5f, 500, writeSeriesResistance, numWritePulses);
		readAverage(client, "After " + numWritePulses + " write pulses: ", 10, ReadFormat.RESISTANCE);

	}

	public void writeSingleDevice(float amplitude, float pulseWidth, int numPulses, int column, int row) {
		client.clear();
		client.set(module, unit, array, column, row);
		client.pulseWrite(amplitude, pulseWidth, writeSeriesResistance, numPulses);
	}

	private void readAverage(KTRAMServerClient client, String s, int numReads, ReadFormat format) {
		// perform 10 read operations, print average and standard deviation
		float[] reads = client.pulseRead(readAmplitude, readWidth, readSeriesResistance, readSenseGain, 10, format);
		AveMaxMinVar readStatistics = new AveMaxMinVar(reads);
		System.out.println(s + ArrayUtils.formatR(5, readStatistics.getAve()) + " ± "
				+ ArrayUtils.formatR(5, readStatistics.getStd()));
	}

}
