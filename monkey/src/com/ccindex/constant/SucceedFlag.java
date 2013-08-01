package com.ccindex.constant;

/**
 * 
 * @ClassName: SucceedFlag
 * @Description: TODO(这里用一句话描述这个类的作用)各种成功的标志位
 * @author tianyu.yang
 * @date 2013-5-28 下午5:48:51
 * 
 */
public class SucceedFlag {

	// 服务端程序是否运行完成且正确的标志位
	private volatile static boolean isServer = false;

	public static void initServer() {
		isServer = false;
	}

	public static boolean isServer() {
		return isServer;
	}

	public static void setServer(boolean isServer) {
		SucceedFlag.isServer = isServer;
	}

}
