package com.knowm.kt_ram_client.examples;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.business.ProductInfo;

public class GetProductInformationExample extends HostInfo {

	public static void main(String[] args) {

		try {
			KTRAMServerClient client = new KTRAMServerClient(host, username, password);

			getModuleInfo(client);
			getDriverInfo(client);
			getBoardInfo(client);

			client.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * Retrieves the product information for each module in array module ports 0
	 * through 5.
	 */
	public static void getModuleInfo(KTRAMServerClient client) {

		System.out.println("");
		System.out.println("Array Modules:");
		for (int moduleId = 0; moduleId < 6; moduleId++) {
			ProductInfo info = client.getModuleInfo(moduleId);
			System.out.println("Module " + moduleId + ": " + (info == null ? "empty" : info));

		}

	}

	/*
	 * Retrieves the product information for the pulse driver and sense module
	 */
	public static void getDriverInfo(KTRAMServerClient client) {
		System.out.println("");
		System.out.println("Diver Module:");
		ProductInfo info = client.getDriverInfo();
		System.out.println(info);
	}

	/*
	 * Retrieves the product information for main board.
	 */
	public static void getBoardInfo(KTRAMServerClient client) {
		System.out.println("");
		System.out.println("Main Board:");
		ProductInfo info = client.getBoardInfo();
		System.out.println(info);
	}

}
