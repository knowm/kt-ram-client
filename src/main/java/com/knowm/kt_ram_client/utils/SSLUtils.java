package com.knowm.kt_ram_client.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class SSLUtils {
	public static SSLContext bypassSignedCertificate() {
		TrustManager[] trustAllCerts = new X509TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("TLS");// or SSL
			ctx.init(null, trustAllCerts, null);
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
		}

		HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		SSLContext.setDefault(ctx);
		return ctx;

	}

	public static SSLContext loadKnowmCert() {

		String CA_FILE = "knowm_ai.crt";
		FileInputStream fis;
		try {
			fis = new FileInputStream(CA_FILE);

			BufferedInputStream bis = new BufferedInputStream(fis);

			X509Certificate ca = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(bis);

			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(null, null);
			ks.setCertificateEntry(Integer.toString(1), ca);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);

			fis.close();
			bis.close();

			return context;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

}
