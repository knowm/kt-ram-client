package com.knowm.kt_ram_client.utils.programming;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.business.Activation;
import com.knowm.kt_ram_client.business.ReadFormat;
import com.knowm.kt_ram_client.utils.ArrayUtils;
import com.knowm.kt_ram_client.utils.AveMaxMinVar;

public abstract class DeviceProgrammer {

	protected KTRAMServerClient client;

	protected Activation activation;

	// this many total steps to try before giving up
	protected int maxSteps = 50;

	// this many read samples to average for each reading between programming steps
	protected int numReadAve = 5;

	// the tolerance for programming. Programming will stop when device is measured
	// to be within this many ohm of target.
	protected float tolerance = 10_000;

	// the resistance you want to program device to
	protected float targetResistance = 600_000;// ohms

	protected int reqSuccessCount = 3;
	protected boolean verbose = true;

	protected float monitorTotalTimeInSeconds = 60;
	protected float monitorSamplePeriod = 10;
	protected double targetHitTime = 0;// seconds from start

	protected List<Double> resistance;
	protected List<Double> time;
	protected long startTime;

	protected void set(Activation activation) {
		client.clear();
		client.set(activation);
		println("Set device " + activation);

	}

	public abstract void init();

	public abstract void programStep(float measuredResistance, float difference, int step);

	public DeviceProgrammer(KTRAMServerClient client) {
		this.client = client;

	}

	public boolean wasTargetAchieved() {
		return targetHitTime > 0;
	}

	protected void println(String s) {
		if (verbose) {
			System.out.println(s);
		}
	}

	public void plotHistory() {
		XYChart chart = new XYChartBuilder()
				.width(800).height(600).title("Routine: " + getClass().getSimpleName() + ", Target="
						+ ArrayUtils.formatR(5, targetResistance) + ", Device=" + activation)
				.xAxisTitle("Time (s)").yAxisTitle("Resistance").build();

		XYSeries series = chart.addSeries("Resistance", time, resistance);
		series.setMarker(SeriesMarkers.CIRCLE);

		XYSeries targetSeries = chart.addSeries("Target Resistance",
				new double[] { time.get(0), time.get(time.size() - 1) },
				new double[] { targetResistance, targetResistance });
		targetSeries.setMarker(SeriesMarkers.NONE);

		if (targetHitTime > 0) {
			XYSeries targetHitSeries = chart.addSeries("Target Hit", new double[] { targetHitTime },
					new double[] { targetResistance });
			targetHitSeries.setMarker(SeriesMarkers.DIAMOND);
			targetHitSeries.setMarkerColor(Color.red);
		}

		chart.getStyler().setYAxisMax(1_000_000.0);
		chart.getStyler().setYAxisMin(200_000.0);

		new SwingWrapper(chart).displayChart();
	}

	public List<Double> program() {

		println("Begin programming for device " + activation);
		println("Resistance Target = " + ArrayUtils.formatR(5, targetResistance));
		println("Routine = " + this.getClass().getSimpleName());

		init();

		resistance = new ArrayList<Double>();
		time = new ArrayList<Double>();

		int successCount = 0;
		startTime = System.currentTimeMillis();
		for (int i = 0; i < maxSteps; i++) {

			AveMaxMinVar r = read();
			float dif = r.getAve() - targetResistance;

			println("step = " + i + ", measured = " + ArrayUtils.formatR(5, r.getAve()) + ", target = "
					+ ArrayUtils.formatR(5, targetResistance) + ",  Δ = " + ArrayUtils.formatR(5, dif));

			if (Math.abs(dif) < tolerance) {// if three consecutive reading return within tolerance,
				successCount++;
				println(successCount + " of " + reqSuccessCount + " reads are within tolerance!");
				if (successCount >= reqSuccessCount) {
					println("DONE");

					targetHitTime = time();

					monitor();

					break;
				} else {
					continue;
				}
			} else {
				successCount = 0;
			}

			programStep(r.getAve(), dif, i);

		}

		return resistance;

	}

	private double time() {
		return (System.currentTimeMillis() - startTime) / 1000.0;
	}

	private void monitor() {

		if (monitorTotalTimeInSeconds > 0) {
			println("Initiating resistance monitoring");
		}

		long startTime = System.currentTimeMillis();
		double elapsedTime = 0;
		while (elapsedTime < monitorTotalTimeInSeconds) {

			try {
				Thread.sleep((long) (monitorSamplePeriod * 1000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			AveMaxMinVar r = read();
			float dif = r.getAve() - targetResistance;
			println("Δt = " + ArrayUtils.formatSec(5, (float) elapsedTime) + ", R(t)="
					+ ArrayUtils.formatR(5, r.getAve()) + ", ΔR=" + ArrayUtils.formatR(5, dif));

			elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;
		}

	}

	private AveMaxMinVar read() {

		float[] reads = client.pulseRead(.2f, 500, 50_000, 40, numReadAve, ReadFormat.RESISTANCE);
		AveMaxMinVar r = new AveMaxMinVar(reads);

		resistance.add((double) r.getAve());

		// resistance.add((double) reads[reads.length - 1]);

		time.add(time());
		return r;
	}

	public Activation getActivation() {
		return activation;
	}

	public void setActivation(Activation activation) {
		this.activation = activation;
	}

	public int getMaxSteps() {
		return maxSteps;
	}

	public void setMaxSteps(int maxSteps) {
		this.maxSteps = maxSteps;
	}

	public int getNumReadAve() {
		return numReadAve;
	}

	public void setNumReadAve(int numReadAve) {
		this.numReadAve = numReadAve;
	}

	public float getTolerance() {
		return tolerance;
	}

	public void setTolerance(float tolerance) {
		this.tolerance = tolerance;
	}

	public float getTargetResistance() {
		return targetResistance;
	}

	public void setTargetResistance(float targetResistance) {
		this.targetResistance = targetResistance;
	}

	public int getReqSuccessCount() {
		return reqSuccessCount;
	}

	public void setReqSuccessCount(int reqSuccessCount) {
		this.reqSuccessCount = reqSuccessCount;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public float getMonitorSamplePeriod() {
		return monitorSamplePeriod;
	}

	public void setMonitorSamplePeriod(float seconds) {
		this.monitorSamplePeriod = seconds;
	}

	public double getTargetHitTime() {
		return targetHitTime;
	}

	public List<Double> getResistance() {
		return resistance;
	}

	public float getMonitorTotalTimeInSeconds() {
		return monitorTotalTimeInSeconds;
	}

	public void setMonitorTotalTimeInSeconds(float monitorTotalTimeInSeconds) {
		this.monitorTotalTimeInSeconds = monitorTotalTimeInSeconds;
	}

}
