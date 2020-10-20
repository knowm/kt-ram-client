package com.knowm.kt_ram_client.examples;

import java.util.ArrayList;
import java.util.List;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.business.ReadFormat;
import com.knowm.kt_ram_client.utils.ArrayDoctor;
import com.knowm.kt_ram_client.utils.ArrayUtils;
import com.knowm.kt_ram_client.utils.AveMaxMinVar;

public class PulseProgramming extends HostInfo {

	// the device address (you must have permission for the array)-->
	static int module = 0;
	static int unit = 0;
	static int array = 0;
	static int column = 1;
	static int row = 2;

	static float writeSeriesResistance = 10_000;
	static float eraseSeriesResistance = 10_000;

	// this many total steps to record before terminating
	static int timeSteps = 50;

	// this many read samples to average for each reading
	static int numReadAve = 5;

	// the write and erase voltages to use for programming
	static float writeAmplitude = .3f;
	static float eraseAmplitude = -.15f;

	// the resistance you want to program device to
	static float targetResistance = 600_000;// ohms

	// the tolerance for programming. Programming will stop when device is measured
	// to be within this many ohm of target.
	static float tolerance = 10_000;

	// if you run lrsHrs() these value will be set. You should run it at least once
	// to get an idea of the LRS/HRS range.
	static float[] lrs_hrs = null;// new float[] { 400_000, 800_000 };

	public static void main(String[] args) {

		try {
			KTRAMServerClient client = new KTRAMServerClient(host, username, password);
			ArrayDoctor doc = new ArrayDoctor(client, module, unit, array);

			/*
			 * @formatter:off
			 * 
			 * Form the device (write/erase cycle it at a higher voltage that normal) to reduce the LRS
			 * and increase the dynamic range. This will affect other device on the array.
			 * Only works if the device has never seen higher voltages before.
			 * 
			 * Example 1:
			 * 
			 * Before Forming (.3V write -.15v erase) 
			 * 		LRS/HRS = 1MΩ / 1.11MΩ 
			 * 
			 * After Forming (.3V write -.15v erase) 
			 * 		LRS/HRS = 400 kΩ / 1.07MΩ
			 * 
			 * Example 2:
			 * 
			 * Before Forming (.3V write -.15v erase) 
			 * 		LRS/HRS = 884.1 kΩ / 919.2 kΩMΩ 
			 * 
			 * After Forming (.3V write -.15v erase) 
			 * 		LRS/HRS = 689.2 kΩ / 1.05 MΩ
			 * 
			 * 
			 * @formatter:on
			 */

			// doc.form(column, row, true);

			/*
			 * apply a strong negative amplitude pulse train. Due to high magnitude, this
			 * will affect other devices on the array.
			 */
			// doc.hardReset(column, row, true);
			// doc.softReset(column, row, true);
			/*
			 * 
			 * @formatter:off
			 * run lrsHrs() to get and idea of what the available LRS and HRS are given the
			 * write and erase amplitudes. Its good to start with a hard-reset and measured
			 * the LRS/HRS from that point.
			 * 
			 * Examples
			 * 
			 * hardReset()-->lrsHrs()
			 * 
			 * Write | Erase | LRS   | HRS   |
			 *   .4v | -.15  | 84kΩ  | 556kΩ |
			 *  .35v | -.15  | 250kΩ | 578kΩ |
			 *  .3v  | -.15  | 500kΩ | 1.12MΩ|
			 * .28v  | -.15  | 650kΩ | 1.14MΩ|
			 *
			 * 
			 * 
			 * Note that these values can change depending on how the device has been used
			 * ('formed') in the past.
			 * 
			 * @formatter:on
			 */

			lrs_hrs = doc.measureLrsHrs(writeAmplitude, writeSeriesResistance, eraseAmplitude, eraseSeriesResistance,
					column, row, true);

			/*
			 * One (of many possible) routine for programming the resistance of a memristor
			 * given a target and tolerance.
			 */

			// programTheThing(client);

			client.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void programTheThing(KTRAMServerClient client) {

		// select the device
		client.clear();
		client.set(module, unit, array, column, row);

		// erase device to start (or not)
		// client.pulseWrite(-1f, 500, 20_000, 5);

		boolean targetAquired = false;

		List<Double> resistanceHistory = new ArrayList<Double>();
		String targetAquiredOnStep = "target not aquired";

		float e_comp = 1000;
		float w_comp = 1000;

		int eNum = 1;
		int wNum = 1;

		for (int i = 1; i < timeSteps; i++) {

			// perform multiple reads and average the results
			float[] reads = client.pulseRead(.15f, 500, 40_000, 40, numReadAve, ReadFormat.RESISTANCE);
			AveMaxMinVar stat = new AveMaxMinVar(reads);

			resistanceHistory.add((double) stat.getAve());
			float dif = stat.getAve() - targetResistance;

			System.out.println("i=" + i + ", measured=" + ArrayUtils.formatR(5, stat.getAve()) + ", target="
					+ ArrayUtils.formatR(5, targetResistance) + ",  Δ=" + ArrayUtils.formatR(5, dif));

			if (!targetAquired && Math.abs(dif) <= tolerance) {
				targetAquired = true;
				targetAquiredOnStep = "target aquired on step " + i;
				System.out.println("TARGET AQUIRED ON STEP " + i);
			} else if (targetAquired) {
				continue;
			}

			if (dif > 0) {
				float width = ((Math.abs(dif) + w_comp) / (float) 5000.0);
				width = width > 500 ? 500 : width;
				width = width < 2.5f ? 2.5f : width;

				if (width == 500 && wNum <= 10) {
					wNum++;
				}

				System.out.println(ArrayUtils.formatPulses(10, wNum, writeAmplitude, width));

				client.pulseWrite(writeAmplitude, width, writeSeriesResistance, wNum);
				w_comp *= 2;
				e_comp = 1000;
				eNum = 1;
			} else {
				float width = ((Math.abs(dif) + e_comp) / (float) 5000.0);

				width = width > 500 ? 500 : width;
				width = width < 2.5f ? 2.5f : width;

				if (width == 500 && eNum <= 10) {
					eNum++;
				}

				System.out.println(ArrayUtils.formatPulses(10, eNum, eraseAmplitude, width));
				client.pulseWrite(eraseAmplitude, width, eraseSeriesResistance, eNum);
				w_comp = 1000;
				e_comp *= 2;
				wNum = 1;
			}

		}

		client.clear();

		XYChart chart = new XYChartBuilder().width(800).height(600)
				.title("Target=" + ArrayUtils.formatR(5, targetResistance) + ", Device=" + module + ":" + unit + ":"
						+ array + ":" + column + ":" + row + ", " + targetAquiredOnStep)
				.xAxisTitle("Pulse Number").yAxisTitle("Resistance").build();

		XYSeries series = chart.addSeries("Resistance vs Time", null, resistanceHistory);
		series.setMarker(SeriesMarkers.NONE);

		XYSeries targetSeries = chart.addSeries("Target", new double[] { 0, resistanceHistory.size() - 1 },
				new double[] { targetResistance, targetResistance });
		targetSeries.setMarker(SeriesMarkers.NONE);

		if (lrs_hrs != null) {
			XYSeries LRSSeries = chart.addSeries("LRS(" + ArrayUtils.formatV(5, writeAmplitude) + ")",
					new double[] { 0, resistanceHistory.size() - 1 }, new double[] { lrs_hrs[0], lrs_hrs[0] });
			LRSSeries.setMarker(SeriesMarkers.NONE);

			XYSeries HRSSeries = chart.addSeries("HRS(" + ArrayUtils.formatV(5, eraseAmplitude) + ")",
					new double[] { 0, resistanceHistory.size() - 1 }, new double[] { lrs_hrs[1], lrs_hrs[1] });
			HRSSeries.setMarker(SeriesMarkers.NONE);

			AveMaxMinVar stat = new AveMaxMinVar(resistanceHistory);

			if (stat.getMin() < lrs_hrs[0]) {
				chart.getStyler().setYAxisMin(stat.getMin() - 10_000.0);
			} else {
				chart.getStyler().setYAxisMin((double) lrs_hrs[0] - 10_000);
			}

			if (stat.getMax() > lrs_hrs[1]) {
				chart.getStyler().setYAxisMax(stat.getMax() + 10_000.0);
			} else {
				chart.getStyler().setYAxisMax((double) lrs_hrs[1] + 10_000.0);
			}

		} else {
			System.out.println("Dont forget to check the LRS and HRS!");
		}

		new SwingWrapper(chart).displayChart();

	}

}
