package com.fastfreesky.www.generics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 
 * @ClassName: FindValueInArea
 * @Description: TODO(这里用一句话描述这个类的作用)查找某一值是否存在某一区间,若存在,取出相应的值
 * @author tianyu.yang
 * @date 2013-7-31 下午4:22:15
 * 
 * @param <K>区间数据,为Long,Integer等
 * @param <T>值类型
 */
public abstract class FindValueInArea<K, T> {

	protected HashMap<K, T> hashMapIp;
	protected K[] keyArrayStart;
	protected K[] keyArrayEnd;
	protected HashMap<K, K> hashMapIPStartEnd;

	public FindValueInArea(int size) {
		hashMapIp = new HashMap<K, T>(size);
		hashMapIPStartEnd = new HashMap<K, K>(size);
	}

	public FindValueInArea() {
		hashMapIp = new HashMap<K, T>();
		hashMapIPStartEnd = new HashMap<K, K>();
	}

	/**
	 * 
	 * @Title: addData
	 * @Description: TODO(这里用一句话描述这个方法的作用)新增数据区间
	 * @param start
	 *            起始
	 * @param end
	 *            终止
	 * @param value
	 *            对应的值 void
	 * @throws
	 */
	public void addData(K start, K end, T value) {
		hashMapIp.put(start, value);
		hashMapIPStartEnd.put(start, end);
	}

	public void addData(K key, T value) {
		hashMapIp.put(key, value);
		hashMapIPStartEnd.put(key, key);
	}

	/**
	 * 
	 * @Title: initKSize
	 * @Description: TODO(这里用一句话描述这个方法的作用)初始化泛型K值的空间
	 * @param length
	 *            需要初始化的数组长度 void
	 * @throws
	 */
	protected abstract void initKSize(int length);

	/**
	 * 
	 * @Title: readey
	 * @Description: TODO(这里用一句话描述这个方法的作用)当数据新增完成后,调用该方法,实现初始化,之后就可以使用get方法 void
	 * @throws
	 */
	public void readey() {
		int length = hashMapIPStartEnd.size();

		initKSize(length);

		Iterator<K> itr = hashMapIPStartEnd.keySet().iterator();
		int i = 0;
		while (itr.hasNext()) {
			keyArrayStart[i++] = itr.next();
		}
		Arrays.sort(keyArrayStart);

		i = 0;
		for (Object a : keyArrayStart) {
			keyArrayEnd[i++] = hashMapIPStartEnd.get(a);
		}
	}

	/**
	 * 
	 * @Title: get
	 * @Description: TODO(这里用一句话描述这个方法的作用)通过输入key值,确定是否在区间内
	 * @param key
	 * @return T
	 * @throws
	 */
	public abstract T get(K key);
}