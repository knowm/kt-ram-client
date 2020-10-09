package com.knowm.kt_ram_client.examples;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.utils.ArrayDoctor;
import com.knowm.kt_ram_client.utils.ArrayUtils;

/*
 * 
 * Uses write pulses of progressively larger magnitude to determine a devices 
 * forward and reverse threshold.
 * 
 * Important Notes
 * 
 * When measuring the forward threshold, increasing voltage amplitude pulse will eventually cause 
 * an increase to the device conductance. Due to the effect of the series resistor, increased conductance 
 * of the memristor will cause a reduction in the voltage drop, thus "shutting off" the the change. It is thus
 * necessary to look for a change in resistance that is statistically greater than the measurement noise. When this is
 * found, the actual threshold will be slightly lower than measured, due to the "shut off" effect.
 *  
 *  
 * When measuring the reverse threshold we must account for another issue: an initial relaxation of resistance
 * to a higher value after the device is written. When a device is written to a lower resistance state their is
 *  an initial relaxation to a higher resistance value. Thus, the threshold for determining when
 * the reverse threshold has been hit in terms of change from initial resistance must be larger than this relaxation,
 *  or else you must wait a minute for the relaxation to occur before driving erase pulses. 
 * 
 * Due to the above effects combined with measurement noise, there will be variations in the measurements before 
 * one takes into account variations due to the memristors themselves.   
 * 
 */
public class DeviceThresholdMeasurmentExample extends HostInfo {

	public static void main(String[] args) {

		try {
			KTRAMServerClient.initClientPool(1, username, password);
			measureAdaptationThreshold(0, 0, 0, 1, 2);
			KTRAMServerClient.shutdownClient();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void measureAdaptationThreshold(int module, int unit, int array, int column, int row) {
		float vPos = ArrayDoctor.measureForwardThreshold(module, unit, array, column, row, host, true);
		float vNeg = ArrayDoctor.measureReverseThreshold(module, unit, array, column, row, host, true);

		System.out.println("Thresholds for device: " + module + ":" + unit + ":" + column + ":" + row);
		System.out.println("   Forward = " + ArrayUtils.formatV(5, vPos));
		System.out.println("   Reverse = " + ArrayUtils.formatV(5, vNeg));
	}

}
