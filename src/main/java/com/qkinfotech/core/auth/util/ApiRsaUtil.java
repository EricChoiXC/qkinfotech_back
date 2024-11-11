package com.qkinfotech.core.auth.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Properties;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.codec.binary.Base64;
public final class ApiRsaUtil {

	private static RSAPublicKey PUBLIC_KEY = null;
	private static RSAPrivateKey PRIVATE_KEY = null;

	/**
	 * RSA公钥加密
	 *
	 * @param str 加密字符串
	 * @return 密文
	 * @throws Exception 加密过程中的异常信息
	 */
	public static String encrypt(String str) throws Exception {

		if (PUBLIC_KEY == null)
			loadRSAKey();

		// RSA加密
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, PUBLIC_KEY);

		StringBuilder buffer = new StringBuilder();

		int MAX_ENCRYPT_BLOCK = 117;

		byte[] inputBytes = str.getBytes(StandardCharsets.UTF_8);

		byte[] outputBytes = getBytes(inputBytes, cipher, MAX_ENCRYPT_BLOCK);

		return Base64.encodeBase64String(outputBytes);
	}

	/**
	 * RSA私钥解密
	 *
	 * @param str 加密字符串
	 * @return 明文
	 * @throws Exception 解密过程中的异常信息
	 */
	public static String decrypt(String str) throws Exception {

		if (PRIVATE_KEY == null)
			loadRSAKey();

		// 64位解码加密后的字符串
		byte[] inputBytes = Base64.decodeBase64(str.getBytes(StandardCharsets.UTF_8));

		// RSA解密
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, PRIVATE_KEY);

		int MAX_DECRYPT_BLOCK = 128;

		byte[] outputBytes = getBytes(inputBytes, cipher, MAX_DECRYPT_BLOCK);

		return new String(outputBytes, StandardCharsets.UTF_8);

	}

	private static byte[] getBytes(byte[] inputBytes, Cipher cipher, int maxCryptLength)
			throws IllegalBlockSizeException, BadPaddingException {
		byte[] outputBytes;
		int inputOffSet = 0, inputLength = inputBytes.length, outputOffSet = 0, outputLength = 0;
		if (inputLength <= maxCryptLength) {
			outputBytes = cipher.doFinal(inputBytes, inputOffSet, inputLength);
		} else {

			ArrayList<byte[]> cryptBytesList = new ArrayList<>();

			for (; inputOffSet < inputLength; inputOffSet += maxCryptLength) {

				int cryptLenth = inputLength - inputOffSet;
				if (cryptLenth > maxCryptLength)
					cryptLenth = maxCryptLength;

				byte[] cryptBytes = cipher.doFinal(inputBytes, inputOffSet, cryptLenth);

				outputLength += cryptBytes.length;
				cryptBytesList.add(cryptBytes);

			}

			outputBytes = new byte[outputLength];
			for (byte[] cryptBytes : cryptBytesList) {
				System.arraycopy(cryptBytes, 0, outputBytes, outputOffSet, cryptBytes.length);
				outputOffSet += cryptBytes.length;
			}
		}
		return outputBytes;
	}

	static void loadRSAKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		Properties prop = new Properties();
		InputStream in = ApiRsaUtil.class.getResourceAsStream("ApiRsaUtil.properties");
		try {
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String publicKey = prop.getProperty("PublicKey").trim();
		// base64编码的公钥
		byte[] encoded = Base64.decodeBase64(publicKey);

		PUBLIC_KEY = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));

		String privateKey = prop.getProperty("PrivateKey").trim();

		// base64编码的私钥
		byte[] decoded = Base64.decodeBase64(privateKey);
		PRIVATE_KEY = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));

	}

}
