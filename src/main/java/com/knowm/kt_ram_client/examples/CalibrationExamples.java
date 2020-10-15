package com.knowm.kt_ram_client.examples;

import java.util.ArrayList;
import java.util.List;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.business.CalibrationProfile;

public class CalibrationExamples extends HostInfo {

	public static void main(String[] args) {

		try {
			KTRAMServerClient client = new KTRAMServerClient(host, username, password);

			viewProfile(client);
			// viewAllProfiles(client);

			client.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * Retrieves all the calibration profiles that are available on the server.
	 * Calls to "pulse-read" endpoint can only be made for values of { amplitude,
	 * width, seriesResistance, senseGain} that have previously been calibration by
	 * the system admin.
	 */
	public static void viewAllProfiles(KTRAMServerClient client) {

		List<CalibrationProfile> profiles = client.getAllCalibrationProfiles();

		System.out.println("Retrieved " + profiles.size() + " calibration profiles.");

		XYChart chart = new XYChartBuilder().width(800).height(600).title("Calibration Profiles").xAxisTitle("Voltage")
				.yAxisTitle("Resistance").build();

		for (int i = 0; i < profiles.size(); i++) {

			CalibrationProfile profile = profiles.get(i);

			ArrayList<Double> x = new ArrayList<>();
			ArrayList<Double> y = new ArrayList<>();

			for (double v = .4f; v < 4.5; v += .05f) {
				x.add(v);
				y.add((double) profile.interpolateResistance((float) v));
			}

			String seriesName = profile.getReadVoltage() + "V, " + profile.getReadPulseWidth() + "μS, "
					+ profile.getSeriesResistor() + "Ω, " + profile.getGain() + "X";

			XYSeries fitSeries = chart.addSeries(seriesName, x, y);

			fitSeries.setMarker(SeriesMarkers.NONE);
		}

		new SwingWrapper(chart).displayChart();

	}

	/*
	 * Retrieves a single calibration profile.
	 * 
	 */
	public static void viewProfile(KTRAMServerClient client) {

		float amplitude = .15f;
		float width = 500;
		float seriesResistance = 50000;
		float senseGain = 30;

		CalibrationProfile calibrationProfile = client.getCalibrationProfile(amplitude, width, seriesResistance,
				senseGain);

		plotCalibrationProfile(calibrationProfile);

	}

	public static void plotCalibrationProfile(CalibrationProfile calibrationProfile) {

		XYChart chart = new XYChartBuilder().width(800).height(600).title("Calibration Profile").xAxisTitle("Voltage")
				.yAxisTitle("Resistance").build();

		XYSeries rawSeries = chart.addSeries("measured", calibrationProfile.getCalibrationData()[0],
				calibrationProfile.getCalibrationData()[1]);

		ArrayList<Double> x = new ArrayList<>();
		ArrayList<Double> y = new ArrayList<>();

		for (double v = .5f; v < 5; v += .05f) {
			x.add(v);
			y.add((double) calibrationProfile.interpolateResistance((float) v));
		}

		XYSeries fitSeries = chart.addSeries("fit", x, y);
		fitSeries.setMarker(SeriesMarkers.NONE);

		new SwingWrapper(chart).displayChart();

	}

}
