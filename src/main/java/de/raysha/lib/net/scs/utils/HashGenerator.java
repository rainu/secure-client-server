package de.raysha.lib.net.scs.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This is a utility class for creating hashes.
 *
 * @author rainu
 */
public class HashGenerator {

	/**
	 * Create a MD5-hash for the given string.
	 *
	 * @param string The string.
	 * @return The MD5-Hash.
	 */
	public static String toMD5(String string) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(string.getBytes());

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < hash.length; ++i) {
				sb.append(Integer.toHexString((hash[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("This code should never reached!", e);
		}
	}

}
