package com.knowm.kt_ram_client.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashGenerator {

//	public static void main(String[] args) {
//
//		String s = "ahdkfhxd";
//		String md5 = md5(s);
//		String sha256 = sha256(s);
//		System.out.println(md5);
//		System.out.println(sha256);
//	}

	public static String md5(String s) {

		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(StandardCharsets.UTF_8.encode(s));
			return String.format("%032x", new BigInteger(1, md5.digest()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;

	}

	public static String sha256(String s) {

		try {
			MessageDigest md5 = MessageDigest.getInstance("SHA-256");
			md5.update(StandardCharsets.UTF_8.encode(s));
			return String.format("%032x", new BigInteger(1, md5.digest()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;

	}

}
