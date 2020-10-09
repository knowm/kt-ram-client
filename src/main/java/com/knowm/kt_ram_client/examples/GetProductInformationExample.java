package com.knowm.kt_ram_client.examples;

import com.knowm.kt_ram_client.KTRAMServerClient;
import com.knowm.kt_ram_client.business.ProductInfo;

public class GetProductInformationExample extends HostInfo {

	public static void main(String[] args) {

		try {
			KTRAMServerClient.initClientPool(1, username, password);

			getModuleInfo();
			getDriverInfo();
			getBoardInfo();

			KTRAMServerClient.shutdownClient();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * Retrieves the product information for each module in array module ports 0
	 * through 5.
	 */
	public static void getModuleInfo() {

		for (int moduleId = 0; moduleId < 6; moduleId++) {
			ProductInfo info = KTRAMServerClient.getModuleInfo(moduleId, host);
			System.out.println("Module " + moduleId + ": " + (info == null ? "empty" : info));

		}

	}

	/*
	 * Retrieves the product information for the pulse driver and sense module
	 */
	public static void getDriverInfo() {

		ProductInfo info = KTRAMServerClient.getDriverInfo(host);
		System.out.println(info);
	}

	/*
	 * Retrieves the product information for main board.
	 */
	public static void getBoardInfo() {

		ProductInfo info = KTRAMServerClient.getBoardInfo(host);
		System.out.println(info);
	}

}
