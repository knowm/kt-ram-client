/**
 * Copyright (c) 2013-2020 Knowm Inc. www.knowm.org
 *
 * <p>Proprietary and confidential and subject to Gov. SBIR Data Rights. Unauthorized copying of
 * this file, via any medium is strictly prohibited.
 *
 * <p>The Government’s rights to use, modify, reproduce, release, perform, display, or disclose
 * technical data or computer software marked with this legend are restricted during the period
 * shown as provided in paragraph (b)(4) of the Rights in Noncommercial Technical Data and Computer
 * Software-Small Business Innovation Research (SBIR) Program clause (DFARS 252.227-7018) contained
 * in the above identified contract. No restrictions apply after the expiration date shown above.
 * Any reproduction of technical data, computer software, or portions thereof marked with this
 * legend must also reproduce the markings.
 *
 * <p>Contract FA8750-17-C-0293 M. Alexander Nugent Consulting 22B Stacy Rd, Santa Fe NM 87505
 * Expiration of SBIR/STTR Data Rights: 10/29/2023, subject to section 8 of the SBA SBIR/STTR policy
 * directive.
 */
package com.knowm.kt_ram_client.utils;

import java.util.List;

/** @author alexnugent */
public class AveMaxMinVar {

	private float max = 0;
	private int max_idx = 0;

	private float min = 0;
	private int min_idx = 0;

	private float ave = 0;
	private float var = 0;

	private int numSamples;

	public AveMaxMinVar() {

	}

	public AveMaxMinVar(List<Double> values) {

		float[] floatValues = new float[values.size()];
		for (int i = 0; i < floatValues.length; i++) {
			floatValues[i] = values.get(i).floatValue();
		}

		init(floatValues);
	}

	public AveMaxMinVar(int[] intValues) {

		float[] v = new float[intValues.length];
		for (int i = 0; i < v.length; i++) {
			v[i] = (float) intValues[i];
		}
		init(v);
	}

	public AveMaxMinVar(float[] values) {

		init(values);
	}

	private void init(float[] values) {

		numSamples = values.length;

		max = values[0];
		min = values[0];

		for (int i = 0; i < values.length; i++) {

			if (values[i] > max) {
				max = values[i];
				max_idx = i;
			}

			if (values[i] < min) {
				min = values[i];
				min_idx = i;
			}

			ave += values[i];
		}
		ave /= values.length;

		for (int i = 0; i < values.length; i++) {
			var += Math.pow(values[i] - ave, 2);
		}
		var /= values.length;
	}

	/** @return the max */
	public float getMax() {

		return max;
	}

	/** @return the min */
	public float getMin() {

		return min;
	}

	/** @return the ave */
	public float getAve() {

		return ave;
	}

	/** @return the var */
	public float getVar() {

		return var;
	}

	public float getStd() {

		return (float) Math.sqrt(getVar());
	}

	/** @return the max_idx */
	public int getMaxIndex() {

		return max_idx;
	}

	/** @return the min_idx */
	public int getMinIndex() {

		return min_idx;
	}

	public int getNumSamples() {
		return numSamples;
	}

	public void setMax(float max) {
		this.max = max;
	}

	public void setMin(float min) {
		this.min = min;
	}

	public void setAve(float ave) {
		this.ave = ave;
	}

	public void setVar(float var) {
		this.var = var;
	}

	public void setNumSamples(int numSamples) {
		this.numSamples = numSamples;
	}
}
