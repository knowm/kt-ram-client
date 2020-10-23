package com.knowm.kt_ram_client.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.knowm.kt_ram_client.business.Activation;
import com.knowm.kt_ram_client.business.ProductInfo;

public class ArrayUtils {

	private static DecimalFormat fm_kOhms = new DecimalFormat("##.#kΩ");
	private static DecimalFormat fm_MOhms = new DecimalFormat("#.##MΩ");
	private static DecimalFormat fm_mVolts = new DecimalFormat("###mV");
	private static DecimalFormat fm_Volts = new DecimalFormat("#.##V");
	private static DecimalFormat fm_Percent = new DecimalFormat("#.#%");
	private static DecimalFormat fm_width = new DecimalFormat("###.#μS");
	private static DecimalFormat fm_seconds = new DecimalFormat("###.##s");

	public static void fillArrayFromActivations(List<Activation> activations, List<float[]> reads, float[][] array) {
		if (activations.size() != reads.size()) {
			throw new RuntimeException("activations and reads are not the same size!");
		}
		for (int i = 0; i < activations.size(); i++) {
			AveMaxMinVar stats = new AveMaxMinVar(reads.get(i));
			Activation a = activations.get(i);
			array[a.column][a.row] = stats.getAve();
		}
	}

	public static List<Activation> getActivationsForWholeArray(int module, int unit, int array,
			ProductInfo productInfo) {
		int[] arraySize = ArrayUtils.getArraySizeFromProductInfo(productInfo);// [cols,rows]

		// get activations for each element that are to be read
		List<Activation> activations = new ArrayList<>();
		for (int col = 0; col < arraySize[0]; col++) {
			for (int row = 0; row < arraySize[1]; row++) {
				Activation a = new Activation(module, unit, array, col, row);
				activations.add(a);
			}
		}
		return activations;

	}

	public static int[] getArraySizeFromProductInfo(ProductInfo productInfo) {

		String name = productInfo.getName();
		if (name.equalsIgnoreCase("Memristor Crossbar Module")) {
			if (productInfo.getVarient().contains("32x32x1")) {
				return new int[] { 32, 32 };
			} else if (productInfo.getVarient().contains("16x16x2")) {
				return new int[] { 16, 16 };
			} else if (productInfo.getVarient().contains("8x8x4")) {
				return new int[] { 8, 8 };
			} else if (productInfo.getVarient().contains("4x4x8")) {
				return new int[] { 4, 4 };
			} else if (productInfo.getVarient().contains("2x2x16")) {
				return new int[] { 2, 2 };
			}
		}
		throw new RuntimeException("Could not determine array size");

	}

	public static void printArrayResistance(float[][] c) {

		StringBuilder b = new StringBuilder();

		b.append(String.format("%21s", ""));
		for (int col = 0; col < c.length; col++) {
			String colHeader = "[col " + col + "]";
			String ss = String.format("%10s", colHeader);
			b.append(ss);
		}

		System.out.println(b);
		for (int row = 0; row < c[0].length; row++) {
			b = new StringBuilder();
			String rowHeader = "[row " + row + "]";
			b.append(String.format("%20s", rowHeader));
			for (int col = 0; col < c.length; col++) {
				String s1 = formatR(10, c[col][row]);
				// String ss = String.format("%10s", s1);
				b.append(s1);

			}
			System.out.println(b);
		}
	}

	public static String formatR(int padding, float resistance) {

		String s;
		if (resistance < 1_000_000) {
			s = fm_kOhms.format(resistance / 1000);
		} else {
			s = fm_MOhms.format(resistance / 1_000_000.0);
		}

		return String.format("%" + padding + "s", s);
	}

	public static String formatV(int padding, float voltage) {
		String s;

		if (voltage < 1) {
			s = fm_mVolts.format(voltage / .001);
		} else {
			s = fm_Volts.format(voltage);
		}

		return String.format("%" + padding + "s", s);
	}

	public static String formatP(int padding, float percent) {

		String s = fm_Percent.format(percent);
		return String.format("%" + padding + "s", s);
	}

	public static String formatSec(int padding, float seconds) {

		String s = fm_seconds.format(seconds);
		return String.format("%" + padding + "s", s);
	}

	public static String formatPulses(int padding, int numPulses, float amplitude, float width) {

		String s = "PULSE " + numPulses + "@(" + fm_mVolts.format(amplitude / .001) + " x " + fm_width.format(width)
				+ ")";
		return String.format("%" + padding + "s", s);
	}

}
