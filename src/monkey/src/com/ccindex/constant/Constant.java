package com.ccindex.constant;

/**
 * 
 * @ClassName: Constant
 * @Description: TODO(这里用一句话描述这个类的作用)常用的变量
 * @author tianyu.yang
 * @date 2013-3-13 上午10:57:51
 * 
 */
public class Constant {

	// 主机
	private static String hostname = null;

	// 等待删除的路径
	private static String deletePath = null;

	public static String getDeletePath() {
		return deletePath;
	}

	public static void setDeletePath(String deletePath) {
		Constant.deletePath = deletePath;
	}

	public static String getHostname() {
		return hostname;
	}

	public static void setHostname(String hostname) {
		Constant.hostname = hostname;
	}
}
