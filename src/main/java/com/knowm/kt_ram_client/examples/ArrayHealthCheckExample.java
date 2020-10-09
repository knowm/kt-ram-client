package com.knowm.kt_ram_client.examples;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.utils.ArrayDoctor;
import com.knowm.kt_ram_client.utils.ArrayUtils;

/*
 * Performs an erase-read-write-read operation across all the elements of an array, 
 * measuring the low resistance state (LRS) and the high resistance state (HRS) for
 * each memristor across the array. To reduce delay, the ArrayDoctor.checkup() method
 * utilizes the "pulse-read-activations" endpoint, performing read and write pulses
 * across all devices specified by an activation list. 
 */
public class ArrayHealthCheckExample extends HostInfo {

	public static void main(String[] args) {

		try {
			KTRAMServerClient.initClientPool(1, username, password);

			arrayHealthCheck(0, 0, 0);

			KTRAMServerClient.shutdownClient();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void arrayHealthCheck(int module, int unit, int array) {
		long start = System.currentTimeMillis();

		float[][][] r = ArrayDoctor.checkup(module, unit, array, host);

		long end = System.currentTimeMillis();

		System.out.println("LRS:");
		ArrayUtils.printArrayResistance(r[0]);
		System.out.println("HRS:");
		ArrayUtils.printArrayResistance(r[1]);
		System.out.println("HRS-LRS");
		ArrayUtils.printArrayResistance(r[2]);

		System.out.println("duration: " + ((end - start) / 1000.0) + " seconds");

	}

}
