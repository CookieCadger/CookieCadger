package com.mattslifebytes.cookiecadger.random;

import java.security.SecureRandom;

public class LocalRandomization {

	private String alphabet;
	private String localRandomization;
	private SecureRandom random;
	
	public LocalRandomization() {
		this.alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~_-";
		this.random = new SecureRandom();
		create(5 + random.nextInt(11));
	}
	
	public String get() {
		return localRandomization;
	}

	private void create(int count) {
		StringBuilder sb = new StringBuilder(count);
		SecureRandom random = new SecureRandom();
		for(int i = 0; i < count; i++) {
			sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
		}
		localRandomization = sb.toString();
	}
}
