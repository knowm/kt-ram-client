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
import com.knowm.kt_ram_client.utils.ArrayUtils;

/*
 * 
 * This example illustrates how reducing the pulse width and voltage of applied pulses can reduce 
 * the change in resistance. You may need to tune the amplitude and voltage. Note that the results are stochastic. 
 * Also note that while the changes are stochastic, a very large range of resistance states are possible. 
 */
public class PulseIncrementation extends HostInfo {

	static int timeSteps = 100;// this many total steps to record before terminating
	static int cyclePeriod = 20;// this many writes or erases before changing

	static float writeAmplitude = .4f;
	static float eraseAmplitude = -.3f;

	static float writePeriod = 50;// us
	static float erasePeriod = 10;// us

	public static void main(String[] args) {

		try {
			KTRAMServerClient client = new KTRAMServerClient(host, username, password);

			pulseIt(client, 0, 0, 0, 1, 0);

			client.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void pulseIt(KTRAMServerClient client, int module, int unit, int array, int column, int row) {

		// select the device
		client.clear();
		client.set(module, unit, array, column, row);

		XYChart chart = new XYChartBuilder().width(800).height(600)
				.title("Pulse Incrementation" + module + ":" + unit + ":" + array + ":" + column + ":" + row)
				.xAxisTitle("Pulse Number").yAxisTitle("Resistance").build();

		System.out.println("Amplitude : Programmed Resistance : Initial Resistance");

		// erase device to start
		client.pulseWrite(-.75f, 500, 20_000, 5);

		boolean isWrite = true;

		List<Double> resistanceHistory = new ArrayList<Double>();

		float read = client.pulseRead(.15f, 500, 50_000, 40, 1, ReadFormat.RESISTANCE)[0];

		resistanceHistory.add((double) read);

		for (int i = 1; i < timeSteps; i++) {

			if (isWrite) {
				client.pulseWrite(writeAmplitude, writePeriod, 50_000, 1);
			} else {
				client.pulseWrite(eraseAmplitude, erasePeriod, 50_000, 1);
			}

			read = client.pulseRead(.15f, 500, 50_000, 40, 1, ReadFormat.RESISTANCE)[0];
			resistanceHistory.add((double) read);

			System.out.println(
					"i=" + i + ", r=" + ArrayUtils.formatR(5, read) + (isWrite ? "   WRITE PULSE" : "   ERASE PULSE"));

			if (i % cyclePeriod == 0) {
				isWrite = !isWrite;
			}

		}

		client.clear();

		XYSeries series = chart.addSeries("Resistance vs Time", null, resistanceHistory);
		series.setMarker(SeriesMarkers.NONE);

		new SwingWrapper(chart).displayChart();

	}

}
