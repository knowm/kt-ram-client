package com.knowm.kt_ram_client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.knowm.kt_ram_client.business.Activation;
import com.knowm.kt_ram_client.business.CalibrationProfile;
import com.knowm.kt_ram_client.business.ErrorMessage;
import com.knowm.kt_ram_client.business.ProductInfo;
import com.knowm.kt_ram_client.business.ReadFormat;
import com.knowm.kt_ram_client.utils.SSLUtils;

public class KTRAMServerClient {

	private static Client[] clients;
	private static int z = 0;

	/**
	 * Initializes the client with the specified number of clients in the client
	 * pool. If you will be using more than one thread, set numClientsInPool to the
	 * number of threads.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static void initClientPool(int numClientsInPool, String username, String password)
			throws NoSuchAlgorithmException, KeyManagementException {

		SSLContext ctx = SSLUtils.bypassSignedCertificate();
		// SSLContext ctx = SSLUtils.loadKnowmCert();

		clients = new Client[numClientsInPool];
		HttpAuthenticationFeature auth = HttpAuthenticationFeature.basic(username, password);

		for (int i = 0; i < clients.length; i++) {
			clients[i] = ClientBuilder.newBuilder().register(JacksonJsonProvider.class).register(MultiPartFeature.class)
					.register(auth).sslContext(ctx).build();

		}
	}

	/** shuts down all the clients in the client pool. */
	public static void shutdownClient() {

		for (int i = 0; i < clients.length; i++) {
			clients[i].close();
		}
	}

	/**
	 * returns a client from the client pool.
	 *
	 * @return
	 */
	public static synchronized Client getClient() {

		return clients[z++ % clients.length];
	}

	public static ProductInfo getBoardInfo(String host) {

		String path = "get-board-info/";

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
			return null;
		}
		throwServerError(response);
		return response.readEntity(ProductInfo.class);

	}

	public static ProductInfo getModuleInfo(int moduleID, String host) {

		String path = "get-module-info/" + moduleID;

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();

		if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
			return null;
		}

		throwServerError(response);

		return response.readEntity(ProductInfo.class);

	}

	public static ProductInfo getDriverInfo(String host) {

		String path = "get-driver-info/";

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
			return null;
		}
		throwServerError(response);
		return response.readEntity(ProductInfo.class);
	}

	public static List<CalibrationProfile> getAllCalibrationProfiles(String host) {

		String path = "get-all-calibration-profiles/";

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();

		System.out.println(response);

		throwServerError(response);
		return response.readEntity(new GenericType<List<CalibrationProfile>>() {
		});

	}

	public static CalibrationProfile getCalibrationProfile(String host, float amplitude, float width,
			float seriesResistance, float senseGain) {

		String path = "get-calibration-profile/" + amplitude + "/" + width + "/" + seriesResistance + "/" + senseGain;

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		throwServerError(response);
		return response.readEntity(CalibrationProfile.class);

	}

	public static boolean pulseWrite(float amplitude, float width, float seriesResistance, int numPulses, String host) {

		String path = "pulse-write/" + amplitude + "/" + width + "/" + seriesResistance + "/" + numPulses;

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		throwServerError(response);
		return response.readEntity(Boolean.class);
	}

	public static boolean pulseWriteActivations(float amplitude, float width, float seriesResistance, int numPulses,
			List<Activation> activations, boolean isPattern, String host) {

		String path = "pulse-write-activations/" + amplitude + "/" + width + "/" + seriesResistance + "/" + numPulses
				+ "/" + isPattern;

		WebTarget webTarget = getClient().target(host).path(path);
		try (Response response = webTarget.request().post(Entity.entity(activations, MediaType.APPLICATION_JSON))) {
			throwServerError(response);
			return response.readEntity(Boolean.class);
		}
	}

	public static float[] pulseRead(float amplitude, float width, float seriesResistance, float senseGain,
			int numPulses, ReadFormat readFormat, String host) {

		String path = "pulse-read/" + amplitude + "/" + width + "/" + seriesResistance + "/" + senseGain + "/"
				+ numPulses + "/" + readFormat;

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		throwServerError(response);
		return response.readEntity(float[].class);
	}

	public static List<float[]> pulseReadActivations(float amplitude, float width, float seriesResistance,
			float senseGain, int numPulses, ReadFormat readFormat, List<Activation> activations, boolean isPattern,
			String host) {

		String path = "pulse-read-activations/" + amplitude + "/" + width + "/" + seriesResistance + "/" + senseGain
				+ "/" + numPulses + "/" + readFormat + "/" + isPattern;

		WebTarget webTarget = getClient().target(host).path(path);
		try (Response response = webTarget.request().post(Entity.entity(activations, MediaType.APPLICATION_JSON))) {
			throwServerError(response);
			return response.readEntity(new GenericType<List<float[]>>() {
			});
		}
	}

	public static boolean set(int module, int unit, int array, int column, int row, String host) {

		String path = "set/" + module + "/" + unit + "/" + array + "/" + column + "/" + row;

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		throwServerError(response);
		return response.readEntity(Boolean.class);
	}

	public static boolean set(int module, int unit, int array, int[] columns, int[] rows, String host) {

		String path = "set/" + module + "/" + unit + "/" + array + "/" + intArray2String(columns) + "/"
				+ intArray2String(rows);

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		throwServerError(response);
		return response.readEntity(Boolean.class);
	}

	public static boolean clear(String host) {

		String path = "clear/";

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		throwServerError(response);
		return response.readEntity(Boolean.class);
	}

	private static String intArray2String(int[] a) {

		StringBuilder b = new StringBuilder();
		for (int i = 0; i < a.length; i++) {
			b.append(a[i]);
			if (i < a.length - 1) {
				b.append(",");
			}
		}
		return b.toString();

	}

	private static void throwServerError(Response response) {
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
			WebApplicationException e = new WebApplicationException(errorMessage.getMessage());
			throw e;
		}
	}
}
