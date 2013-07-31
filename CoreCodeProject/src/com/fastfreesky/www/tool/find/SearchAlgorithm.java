package com.fastfreesky.www.tool.find;

public class SearchAlgorithm {

	/**
	 * * 二分查找算法 * *查找符合某一区间的值,即大于当前值,小于middle+1的值区间内
	 * 
	 * @param srcArray
	 *            有序数组 *
	 * @param des
	 *            查找元素 *
	 * @return des的数组下标，没找到返回-1
	 */
	public static int binarySearch(Long[] srcArray, Long des) {

		int low = 0;
		int high = srcArray.length - 1;
		while (low <= high) {
			int middle = (low + high) / 2;
			// 查找到最后的状态值了
			int status = des.compareTo(srcArray[middle]);
			if (status == 0) {
				return middle;

			} else if (status < 0) {
				high = middle - 1;
			} else {
				low = middle + 1;
			}

		}
		int middle = (low + high) / 2;
		int status = des.compareTo(srcArray[middle]);
		if (status > 0) {
			return middle;
		} else {
			return -1;
		}
	}


	public static int findIpInArea(Long[] srcArrayStart, Long[] srcArrayEnd,
			Long des) {

		int start = binarySearch(srcArrayStart, des);
		if (start == -1) {
			return -1;
		} else {

			int status = des.compareTo(srcArrayEnd[start]);
			if (status <= 0) {
				return start;
			} else {
				return -1;
			}
		}
	}
	public static void main(String[] args) {

		Long[] srcArray = { 1l, 2l, 3l, 4l, 5l, 6l, 7l, 8l, 9l, 12l };
		System.out.println(binarySearch(srcArray, 0l));
		System.out.println(binarySearch(srcArray, 10l));
		System.out.println(binarySearch(srcArray, 15l));
		// System.out.println(binarySearch(srcArray, 2l));
		// System.out.println(binarySearch(srcArray, 3l));
		// System.out.println(binarySearch(srcArray, 4l));
		// System.out.println(binarySearch(srcArray, 5l));
		// System.out.println(binarySearch(srcArray, 6l));
		// System.out.println(binarySearch(srcArray, 7l));
		// System.out.println(binarySearch(srcArray, 8l));
		// System.out.println(binarySearch(srcArray, 9l));
	}
}
