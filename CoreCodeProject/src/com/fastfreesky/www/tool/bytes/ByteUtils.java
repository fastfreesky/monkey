package com.fastfreesky.www.tool.bytes;

/**
 * 参考：
 * http://oss-dataturbine.googlecode.com/svn/trunk/dev/j2me-rbnb/src/com/rbnb
 * /utility/ByteConvert.java
 * 
 * @author user
 */
public class ByteUtils {

	// byte2Double method - extracts doubles from byte array
	public static final double[] byte2Double(byte[] inData, boolean byteSwap) {
		int j = 0, upper, lower;
		int length = inData.length / 8;
		double[] outData = new double[length];
		if (!byteSwap)
			for (int i = 0; i < length; i++) {
				j = i * 8;
				upper = (((inData[j] & 0xff) << 24)
						+ ((inData[j + 1] & 0xff) << 16)
						+ ((inData[j + 2] & 0xff) << 8) + ((inData[j + 3] & 0xff) << 0));
				lower = (((inData[j + 4] & 0xff) << 24)
						+ ((inData[j + 5] & 0xff) << 16)
						+ ((inData[j + 6] & 0xff) << 8) + ((inData[j + 7] & 0xff) << 0));
				outData[i] = Double.longBitsToDouble((((long) upper) << 32)
						+ (lower & 0xffffffffl));
			}
		else
			for (int i = 0; i < length; i++) {
				j = i * 8;
				upper = (((inData[j + 7] & 0xff) << 24)
						+ ((inData[j + 6] & 0xff) << 16)
						+ ((inData[j + 5] & 0xff) << 8) + ((inData[j + 4] & 0xff) << 0));
				lower = (((inData[j + 3] & 0xff) << 24)
						+ ((inData[j + 2] & 0xff) << 16)
						+ ((inData[j + 1] & 0xff) << 8) + ((inData[j] & 0xff) << 0));
				outData[i] = Double.longBitsToDouble((((long) upper) << 32)
						+ (lower & 0xffffffffl));
			}

		return outData;
	}

	// byte2Float method - extracts floats from byte array
	public static final float[] byte2Float(byte[] inData, boolean byteSwap) {
		int j = 0, value;
		int length = inData.length / 4;
		float[] outData = new float[length];
		if (!byteSwap)
			for (int i = 0; i < length; i++) {
				j = i * 4;
				value = (((inData[j] & 0xff) << 24)
						+ ((inData[j + 1] & 0xff) << 16)
						+ ((inData[j + 2] & 0xff) << 8) + ((inData[j + 3] & 0xff) << 0));
				outData[i] = Float.intBitsToFloat(value);
			}
		else
			for (int i = 0; i < length; i++) {
				j = i * 4;
				value = (((inData[j + 3] & 0xff) << 24)
						+ ((inData[j + 2] & 0xff) << 16)
						+ ((inData[j + 1] & 0xff) << 8) + ((inData[j] & 0xff) << 0));
				outData[i] = Float.intBitsToFloat(value);
			}

		return outData;
	}

	// EMF 9/12/00
	// byte2Int method - extracts ints from byte array
	public static final int[] byte2Int(byte[] inData, boolean byteSwap) {
		int j = 0;
		int length = inData.length / 4;
		int[] outData = new int[length];
		if (!byteSwap)
			for (int i = 0; i < length; i++) {
				j = i * 4;
				outData[i] = (((inData[j] & 0xff) << 24)
						+ ((inData[j + 1] & 0xff) << 16)
						+ ((inData[j + 2] & 0xff) << 8) + ((inData[j + 3] & 0xff) << 0));
			}
		else
			for (int i = 0; i < length; i++) {
				j = i * 4;
				outData[i] = (((inData[j + 3] & 0xff) << 24)
						+ ((inData[j + 2] & 0xff) << 16)
						+ ((inData[j + 1] & 0xff) << 8) + ((inData[j] & 0xff) << 0));
			}

		return outData;
	}

	// INB 11/7/00
	// byte2Long method - extracts longs from byte array
	public static final long[] byte2Long(byte[] inData, boolean byteSwap) {
		int j = 0;
		int length = inData.length / 8;
		long ff = 0xff;
		long[] outData = new long[length];
		if (!byteSwap)
			for (int i = 0; i < length; i++) {
				j = i * 8;
				outData[i] = (((inData[j] & ff) << 56)
						+ ((inData[j + 1] & ff) << 48)
						+ ((inData[j + 2] & ff) << 40)
						+ ((inData[j + 3] & ff) << 32)
						+ ((inData[j + 4] & ff) << 24)
						+ ((inData[j + 5] & ff) << 16)
						+ ((inData[j + 6] & ff) << 8) + ((inData[j + 7] & ff) << 0));

				// UCB 06/21/01-- finding a bug!
				// System.err.println("   Converting: ");
				// long result = 0;
				// long accume = 0;
				// for (int k = j+7, m = 0; k > -1; k--, m++) {
				// System.err.println("   inData[" + k + "]: " + inData[k]);
				// System.err.println("   inData & (long) ff: " + (inData[k] &
				// ff));
				// result = (inData[k] & ff) << (m*8);
				// System.err.println("   (inData & (long) ff) << " + (m*8) +
				// ": " +
				// result);
				// accume += result;
				// System.err.println("   So far: " + accume);
				// }

			}
		else
			for (int i = 0; i < length; i++) {
				j = i * 8;
				outData[i] = (((inData[j + 7] & 0xff) << 56)
						+ ((inData[j + 6] & 0xff) << 48)
						+ ((inData[j + 5] & 0xff) << 40)
						+ ((inData[j + 4] & 0xff) << 32)
						+ ((inData[j + 3] & 0xff) << 24)
						+ ((inData[j + 2] & 0xff) << 16)
						+ ((inData[j + 1] & 0xff) << 8) + ((inData[j] & 0xff) << 0));
			}

		return outData;
	}

	// byte2Short method - extracts short ints from byte array
	public static final short[] byte2Short(byte[] inData, boolean byteSwap) {
		// int j=0;
		int length = inData.length / 2;
		short[] outData = new short[length];
		if (!byteSwap)
			for (int i = 0, j = 0; i < length; i++, j += 2) {
				// j=i*2;
				// outData[i]=(short)( ((inData[j] & 0xff) << 8) + ((inData[j+1]
				// & 0xff) << 0 ) );
				outData[i] = (short) ((inData[j] << 8) + (inData[j + 1] & 0xff));
			}
		else
			for (int i = 0; i < length; i++) {
				int j = i * 2;
				outData[i] = (short) (((inData[j + 1] & 0xff) << 8) + ((inData[j] & 0xff) << 0));
			}

		return outData;
	}

	// double2Byte method - writes doubles to byte array
	public static final byte[] double2Byte(double[] inData) {
		int j = 0;
		int length = inData.length;
		byte[] outData = new byte[length * 8];
		for (int i = 0; i < length; i++) {
			long data = Double.doubleToLongBits(inData[i]);
			outData[j++] = (byte) (data >>> 56);
			outData[j++] = (byte) (data >>> 48);
			outData[j++] = (byte) (data >>> 40);
			outData[j++] = (byte) (data >>> 32);
			outData[j++] = (byte) (data >>> 24);
			outData[j++] = (byte) (data >>> 16);
			outData[j++] = (byte) (data >>> 8);
			outData[j++] = (byte) (data >>> 0);
		}
		return outData;
	}

	// float2Byte method - writes floats to byte array
	public static final byte[] float2Byte(float[] inData) {
		int j = 0;
		int length = inData.length;
		byte[] outData = new byte[length * 4];
		for (int i = 0; i < length; i++) {
			int data = Float.floatToIntBits(inData[i]);
			outData[j++] = (byte) (data >>> 24);
			outData[j++] = (byte) (data >>> 16);
			outData[j++] = (byte) (data >>> 8);
			outData[j++] = (byte) (data >>> 0);
		}
		return outData;
	}

	// EMF 9/13/00: added support for integers
	// int2Byte method - writes ints to byte array
	public static final byte[] int2Byte(int[] inData) {
		int j = 0;
		int length = inData.length;
		byte[] outData = new byte[length * 4];
		for (int i = 0; i < length; i++) {
			outData[j++] = (byte) (inData[i] >>> 24);
			outData[j++] = (byte) (inData[i] >>> 16);
			outData[j++] = (byte) (inData[i] >>> 8);
			outData[j++] = (byte) (inData[i] >>> 0);
		}
		return outData;
	}

	// UCB 7/19/01: added support for longs
	// long2Byte method - writes longs to byte array
	public static final byte[] long2Byte(long[] inData) {
		int j = 0;
		int length = inData.length;
		byte[] outData = new byte[length * 8];
		for (int i = 0; i < length; i++) {
			outData[j++] = (byte) (inData[i] >>> 56);
			outData[j++] = (byte) (inData[i] >>> 48);
			outData[j++] = (byte) (inData[i] >>> 40);
			outData[j++] = (byte) (inData[i] >>> 32);
			outData[j++] = (byte) (inData[i] >>> 24);
			outData[j++] = (byte) (inData[i] >>> 16);
			outData[j++] = (byte) (inData[i] >>> 8);
			outData[j++] = (byte) (inData[i] >>> 0);
		}
		return outData;
	}

	// short2Byte method - writes short ints to byte array
	public static final byte[] short2Byte(short[] inData) {
		int j = 0;
		int length = inData.length;
		byte[] outData = new byte[length * 2];
		for (int i = 0; i < length; i++) {
			outData[j++] = (byte) (inData[i] >>> 8);
			outData[j++] = (byte) (inData[i] >>> 0);
		}
		return outData;
	}

	/**
	 * Writes doubles to byte array, with optional byte swapping.
	 * 
	 * <p>
	 * 
	 * @author WHF
	 * @since V2.0B10
	 */
	public static final byte[] double2Byte(double[] inData, boolean makeLSB) {
		if (!makeLSB)
			return double2Byte(inData);
		int j = 0;
		int length = inData.length;
		byte[] outData = new byte[length * 8];
		for (int i = 0; i < length; i++) {
			long data = Double.doubleToLongBits(inData[i]);
			outData[j++] = (byte) (data >>> 0);
			outData[j++] = (byte) (data >>> 8);
			outData[j++] = (byte) (data >>> 16);
			outData[j++] = (byte) (data >>> 24);
			outData[j++] = (byte) (data >>> 32);
			outData[j++] = (byte) (data >>> 40);
			outData[j++] = (byte) (data >>> 48);
			outData[j++] = (byte) (data >>> 56);
		}
		return outData;
	}

	/**
	 * Writes floats to byte array, with optional byte swapping.
	 * 
	 * <p>
	 * 
	 * @author WHF
	 * @since V2.0B10
	 */
	public static final byte[] float2Byte(float[] inData, boolean makeLSB) {
		if (!makeLSB)
			return float2Byte(inData);
		int j = 0;
		int length = inData.length;
		byte[] outData = new byte[length * 4];
		for (int i = 0; i < length; i++) {
			int data = Float.floatToIntBits(inData[i]);
			outData[j++] = (byte) (data >>> 0);
			outData[j++] = (byte) (data >>> 8);
			outData[j++] = (byte) (data >>> 16);
			outData[j++] = (byte) (data >>> 24);
		}
		return outData;
	}

	/**
	 * Writes ints to byte array, with optional byte swapping.
	 * 
	 * <p>
	 * 
	 * @author WHF
	 * @since V2.0B10
	 */
	public static final byte[] int2Byte(int[] inData, boolean makeLSB) {
		if (!makeLSB)
			return int2Byte(inData);
		int j = 0;
		int length = inData.length;
		byte[] outData = new byte[length * 4];
		for (int i = 0; i < length; i++) {
			int data = inData[i];
			outData[j++] = (byte) (data >>> 0);
			outData[j++] = (byte) (data >>> 8);
			outData[j++] = (byte) (data >>> 16);
			outData[j++] = (byte) (data >>> 24);
		}
		return outData;
	}

	/**
	 * Writes longs to byte array, with optional byte swapping.
	 * 
	 * <p>
	 * 
	 * @author WHF
	 * @since V2.0B10
	 */
	public static final byte[] long2Byte(long[] inData, boolean makeLSB) {
		if (!makeLSB)
			return long2Byte(inData);
		int j = 0;
		int length = inData.length;
		byte[] outData = new byte[length * 8];
		for (int i = 0; i < length; i++) {
			long data = inData[i];
			outData[j++] = (byte) (data >>> 0);
			outData[j++] = (byte) (data >>> 8);
			outData[j++] = (byte) (data >>> 16);
			outData[j++] = (byte) (data >>> 24);
			outData[j++] = (byte) (data >>> 32);
			outData[j++] = (byte) (data >>> 40);
			outData[j++] = (byte) (data >>> 48);
			outData[j++] = (byte) (data >>> 56);
		}
		return outData;
	}

	/**
	 * Writes shorts to byte array, with optional byte swapping.
	 * 
	 * <p>
	 * 
	 * @author WHF
	 * @since V2.0B10
	 */
	public static final byte[] short2Byte(short[] inData, boolean makeLSB) {
		if (!makeLSB)
			return short2Byte(inData);
		int j = 0;
		int length = inData.length;
		byte[] outData = new byte[length * 2];
		for (int i = 0; i < length; i++) {
			short data = inData[i];
			outData[j++] = (byte) (data >>> 0);
			outData[j++] = (byte) (data >>> 8);
		}
		return outData;
	}

	// 容器 , 容器 那个地址开始装载 , copybyte , 从copybyte start - end
	public static void byteInbyte(byte[] cont, int start, byte[] copy,
			int copy_start, int copy_end) {
		for (int i = copy_start; i < copy_end; i++) {
			cont[start + i] = copy[i];
		}
	}

	/**
	 * 可以得到 类似 : 3位都为1 的二进制 int return bit:111 > long:7
	 * 
	 */
	public static long getAllBitOne(int size) {
		long num = 0L;
		for (int i = 0; i < size; i++) {
			num |= 1L << i;
		}
		return num;
	}

	/**
	 * <pre>
	 * 可以得到  bit 最大1 在什么位置 
	 *     7  最大1位置  3 
	 *     255 最大位置 8
	 * 256 = 9
	 * 
	 * <pre>
	 */
	public static int getMaxOneSize(long num) {
		int size = 0;
		for (int i = 1; i <= Long.SIZE; i++) {
			if (num % 2 == 1)
				size = i;
			num = num / 2;
		}
		return size;
	}

	/**
	 * <pre>
	 * 可以得到  bit 最小1 在什么位置 
	 *     7  最大1位置  0
	 *     256 最大位置 2
	 * 
	 * <pre>
	 */
	public static int getMinOneSize(long num) {
		for (int i = 1; i <= Long.SIZE; i++) {
			if (num % 2 == 1){
				return i ;
			}
			num = num / 2;
		}
		return 0 ;
	}
	
	/**
	 * <pre>
	 * 可以得到  bit 最小0 在什么位置 
	 * <pre>
	 */
	public static int getMinZeroSize(long num) {
		for (int i = 1; i <= Long.SIZE; i++) {
			if (num % 2 == 0){
				return i ;
			}
			num = num / 2;
		}
		return 0 ;
	}

	
}
