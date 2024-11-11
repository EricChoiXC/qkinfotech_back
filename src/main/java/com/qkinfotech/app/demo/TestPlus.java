package com.qkinfotech.app.demo;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import sun.misc.Unsafe;

public class TestPlus {
	private static Unsafe U;
	static {
		Field field;
		try {
			field = Unsafe.class.getDeclaredField("theUnsafe");
	        field.setAccessible(true);
	        U = (Unsafe)field.get(null);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		long count = 1000000000;
		long result = 0;

		String s = "6011575820";
		long start = System.currentTimeMillis();
		for(int i=0; i < count; ++i) {
			result = Long.parseLong(s);
		}
		System.out.println(result);
		System.out.println("采用 Long.parseLong(s) cost:" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		byte[] bytes= s.getBytes();
		for(int i=0; i < count; ++i) {
			result = toLong(bytes, 0, bytes.length);
		}
		System.out.println(result);
		System.out.println("使用位移转换， byte数组取数据 cost:" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		bytes= s.getBytes();
		ByteBuffer buffer2 = ByteBuffer.allocate(bytes.length);
		for(int i=0; i < bytes.length; ++i) {
			buffer2.put(bytes[i]);
		}
		for(int i=0; i < count; ++i) {
			buffer2.position(0);
			result = toLong(buffer2, 0, bytes.length);
		}
		System.out.println(result);
		System.out.println("使用 ByteBuffer 读取数据 cost:" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		bytes= s.getBytes();
		ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
		for(int i=0; i < bytes.length; ++i) {
			buffer.put(bytes[i]);
		}
		for(int i=0; i < count; ++i) {
			buffer.position(0);
			result = toLong(buffer, 0, bytes.length);
		}
		System.out.println(result);
		System.out.println("使用 DirectByteBuffer 读取数据 cost:" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		bytes= s.getBytes();
		long address = U.allocateMemory(bytes.length);
		for(int i=0; i < bytes.length; ++i) {
			U.putByte(address + i, bytes[i]);
		}
		for(int i=0; i < count; ++i) {
			result = toLong(address, 0, bytes.length);
		}
		U.freeMemory(address);
		System.out.println(result);
		System.out.println("使用 Unsafe 读取数据 cost:" + (System.currentTimeMillis() - start));
	}

	private static long toLong(long address, int i, int j) {
		
		byte b;
		
		long result = 0;
		while(true) {
			result = (result << 1) + (result << 3);
			result += U.getByte(address++) - 48;
			if(++i == j) {
				break;
			}
		}
		return result;
	}
	
	private static long toLong(ByteBuffer bytes, int i, int j) {
		
		byte b;
		
		long result = 0;
		while(true) {
			result = (result << 1) + (result << 3);
			result += bytes.get() - 48;
			if(++i == j) {
				break;
			}
		}
		return result;
	}

	private static long toLong(byte[] bytes, int i, int j) {
		
		byte b;
		
		long result = 0;
		while(true) {
			result = (result << 1) + (result << 3);
			result += bytes[i++] - 48;
			if(i == j) {
				break;
			}
		}
		return result;
	}
	

}
