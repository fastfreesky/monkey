package com.ccindex.interfaceI;

import com.ccindex.tool.ParseArgs;

public interface MonkeyMainI {

	/**
	 * 
	 * @Title: init
	 * @Description: TODO(这里用一句话描述这个方法的作用)初始化入口,服务端客户端均响应此接口
	 * @param args
	 *            void
	 * @throws
	 */
	public void init(ParseArgs args) throws Exception;

	public void run() throws Exception;
	
}
