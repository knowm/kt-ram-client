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

	private String host;

	private Client[] clients;
	private int z = 0;

	public KTRAMServerClient(String host, String username, String password)
			throws KeyManagementException, NoSuchAlgorithmException {

		this.host = host;

		initClientPool(1, host, username, password);

	}

	/**
	 * Initializes the client with the specified number of clients in the client
	 * pool. If you will be using more than one thread, set numClientsInPool to the
	 * number of threads.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public void initClientPool(int numClientsInPool, String host, String username, String password)
			throws NoSuchAlgorithmException, KeyManagementException {

		if (host.contains("https")) {
			SSLContext ctx;

			if (host.contains("knowm.ai")) {
				ctx = SSLUtils.loadKnowmCert();
			} else {
				ctx = SSLUtils.bypassSignedCertificate();
			}

			clients = new Client[numClientsInPool];
			HttpAuthenticationFeature auth = HttpAuthenticationFeature.basic(username, password);

			for (int i = 0; i < clients.length; i++) {
				clients[i] = ClientBuilder.newBuilder().register(JacksonJsonProvider.class)
						.register(MultiPartFeature.class).register(auth).sslContext(ctx).build();

			}
		} else {
			clients = new Client[numClientsInPool];
			HttpAuthenticationFeature auth = HttpAuthenticationFeature.basic(username, password);

			for (int i = 0; i < clients.length; i++) {
				clients[i] = ClientBuilder.newBuilder().register(JacksonJsonProvider.class)
						.register(MultiPartFeature.class).register(auth).build();

			}
		}

	}

	/** shuts down all the clients in the client pool. */
	public void shutdown() {

		for (int i = 0; i < clients.length; i++) {
			clients[i].close();
		}
	}

	/**
	 * returns a client from the client pool.
	 *
	 * @return
	 */
	public synchronized Client getClient() {

		return clients[z++ % clients.length];
	}

	public ProductInfo getBoardInfo() {

		String path = "get-board-info/";

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
			return null;
		}
		throwServerError(response);
		return response.readEntity(ProductInfo.class);

	}

	public ProductInfo getModuleInfo(int moduleID) {

		String path = "get-module-info/" + moduleID;

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();

		if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
			return null;
		}

		throwServerError(response);

		return response.readEntity(ProductInfo.class);

	}

	public ProductInfo getDriverInfo() {

		String path = "get-driver-info/";

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
			return null;
		}
		throwServerError(response);
		return response.readEntity(ProductInfo.class);
	}

	public List<CalibrationProfile> getAllCalibrationProfiles() {

		String path = "get-all-calibration-profiles/";

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();

		System.out.println(response);

		throwServerError(response);
		return response.readEntity(new GenericType<List<CalibrationProfile>>() {
		});

	}

	public CalibrationProfile getCalibrationProfile(float amplitude, float width, float seriesResistance,
			float senseGain) {

		String path = "get-calibration-profile/" + amplitude + "/" + width + "/" + seriesResistance + "/" + senseGain;

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		throwServerError(response);
		return response.readEntity(CalibrationProfile.class);

	}

	public boolean pulseWrite(float amplitude, float width, float seriesResistance, int numPulses) {

		String path = "pulse-write/" + amplitude + "/" + width + "/" + seriesResistance + "/" + numPulses;

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		throwServerError(response);
		return response.readEntity(Boolean.class);
	}

	public boolean pulseWriteActivations(float amplitude, float width, float seriesResistance, int numPulses,
			List<Activation> activations, boolean isPattern) {

		String path = "pulse-write-activations/" + amplitude + "/" + width + "/" + seriesResistance + "/" + numPulses
				+ "/" + isPattern;

		WebTarget webTarget = getClient().target(host).path(path);
		try (Response response = webTarget.request().post(Entity.entity(activations, MediaType.APPLICATION_JSON))) {
			throwServerError(response);
			return response.readEntity(Boolean.class);
		}
	}

	public float[] pulseRead(float amplitude, float width, float seriesResistance, float senseGain, int numPulses,
			ReadFormat readFormat) {

		String path = "pulse-read/" + amplitude + "/" + width + "/" + seriesResistance + "/" + senseGain + "/"
				+ numPulses + "/" + readFormat;

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		throwServerError(response);
		return response.readEntity(float[].class);
	}

	public List<float[]> pulseReadActivations(float amplitude, float width, float seriesResistance, float senseGain,
			int numPulses, ReadFormat readFormat, List<Activation> activations, boolean isPattern) {

		String path = "pulse-read-activations/" + amplitude + "/" + width + "/" + seriesResistance + "/" + senseGain
				+ "/" + numPulses + "/" + readFormat + "/" + isPattern;

		WebTarget webTarget = getClient().target(host).path(path);
		try (Response response = webTarget.request().post(Entity.entity(activations, MediaType.APPLICATION_JSON))) {
			throwServerError(response);
			return response.readEntity(new GenericType<List<float[]>>() {
			});
		}
	}

	public boolean set(Activation activation) {

		String path = "set/" + activation.module + "/" + activation.unit + "/" + activation.array + "/"
				+ activation.column + "/" + activation.row;

		WebTarget webTarget = getClient().target(host).path(path);

		// System.out.println(webTarget);

		Response response = webTarget.request().get();
		throwServerError(response);
		return response.readEntity(Boolean.class);
	}

	public boolean set(int module, int unit, int array, int column, int row) {

		String path = "set/" + module + "/" + unit + "/" + array + "/" + column + "/" + row;

		WebTarget webTarget = getClient().target(host).path(path);

		// System.out.println(webTarget);

		Response response = webTarget.request().get();
		throwServerError(response);
		return response.readEntity(Boolean.class);
	}

//	public boolean set(int module, int unit, int array, int[] columns, int[] rows) {
//
//		String path = "set/" + module + "/" + unit + "/" + array + "/" + intArray2String(columns) + "/"
//				+ intArray2String(rows);
//
//		WebTarget webTarget = getClient().target(host).path(path);
//		Response response = webTarget.request().get();
//		throwServerError(response);
//		return response.readEntity(Boolean.class);
//	}

	public boolean clear() {

		String path = "clear/";

		WebTarget webTarget = getClient().target(host).path(path);
		Response response = webTarget.request().get();
		throwServerError(response);
		return response.readEntity(Boolean.class);
	}

//	private String intArray2String(int[] a) {
//
//		StringBuilder b = new StringBuilder();
//		for (int i = 0; i < a.length; i++) {
//			b.append(a[i]);
//			if (i < a.length - 1) {
//				b.append(",");
//			}
//		}
//		return b.toString();
//
//	}

	private void throwServerError(Response response) {
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {

			System.out.println(response);

			ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
			WebApplicationException e = new WebApplicationException(errorMessage.getMessage());
			throw e;
		}
	}
}
