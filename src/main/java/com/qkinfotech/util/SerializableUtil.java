package com.qkinfotech.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class SerializableUtil {

	public final static Object unmarshall(String str) {
		byte[] bytes = Base64.getDecoder().decode(str);

		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {

			return in.readObject();
		} catch (Exception e) {
			return null;
		}
	}

	public final static String marshall(Object o) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
			out.writeObject(o);
			return Base64.getEncoder().encodeToString(bos.toByteArray());
		} catch (Exception e) {
			return null;
		}
	}

}
