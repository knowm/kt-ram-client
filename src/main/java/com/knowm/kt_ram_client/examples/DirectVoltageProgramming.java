package com.knowm.kt_ram_client.examples;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.business.ReadFormat;
import com.knowm.kt_ram_client.utils.ArrayUtils;
import com.knowm.kt_ram_client.utils.AveMaxMinVar;

/*
 * Uses the series resistor and an applied forward voltage to program the device. As the resistance of the 
 * device drops, so does the voltage drop across it (due to the series resistor circuit). This will continue
 * until the device reached its threshold. The lower the voltage, the higher the resistance. Due to the stochastic 
 * nature of resistance changes, this is a noisy method. 
 */
public class DirectVoltageProgramming extends HostInfo {

	public static void main(String[] args) {

		try {
			KTRAMServerClient client = new KTRAMServerClient(host, username, password);

			getCurve(client, 0, 0, 0, 4, 0);

			client.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void getCurve(KTRAMServerClient client, int module, int unit, int array, int column, int row) {

		// select the device
		client.clear();
		client.set(module, unit, array, column, row);

		double[] programmingVoltages = new double[30];//
		for (int i = 0; i < programmingVoltages.length; i++) {
			programmingVoltages[i] = .2 + .01 * i;
		}

		XYChart chart = new XYChartBuilder()
				.width(800).height(600).title("Direct Voltage Programming Profile for Device: " + module + ":" + unit
						+ ":" + array + ":" + column + ":" + row)
				.xAxisTitle("Applied Write Voltage").yAxisTitle("Resistance").build();

		System.out.println("Amplitude : Programmed Resistance : Initial Resistance");
		double[] y = new double[programmingVoltages.length];
		double[] y_init = new double[programmingVoltages.length];

		for (int i = 0; i < programmingVoltages.length; i++) {
			double v = programmingVoltages[i];

			// erase
			client.pulseWrite(-.75f, 500, 20_000, 5);

			// read initial resistance
			float[] read = client.pulseRead(.15f, 500, 50_000, 30, 5, ReadFormat.RESISTANCE);
			AveMaxMinVar stats = new AveMaxMinVar(read);
			y_init[i] = stats.getAve();

			// write pulses with high series resistance
			client.pulseWrite((float) v, 500, 50_000, 10);

			// read
			read = client.pulseRead(.15f, 500, 50_000, 30, 5, ReadFormat.RESISTANCE);
			stats = new AveMaxMinVar(read);
			y[i] = stats.getAve();

			System.out.println(ArrayUtils.formatV(9, (float) v) + " : " + ArrayUtils.formatR(21, (float) y[i]) + " : "
					+ ArrayUtils.formatR(18, (float) y_init[i]));

		}

		client.clear();

		XYSeries series = chart.addSeries("Programmed Resistance", programmingVoltages, y);
		series.setMarker(SeriesMarkers.NONE);

		XYSeries series_init = chart.addSeries("Initial Resistance", programmingVoltages, y_init);
		series_init.setMarker(SeriesMarkers.NONE);

		new SwingWrapper(chart).displayChart();

	}

}
