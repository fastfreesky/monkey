package com.ccindex.main;

import java.io.FileNotFoundException;

import com.ccindex.interfaceI.MonkeyMainI;
import com.ccindex.tool.ParseArgs;

/**
 * 
 * @ClassName: Main
 * @Description: TODO(这里用一句话描述这个类的作用)调度程序的总入库
 * @author tianyu.yang
 * @date 2013-5-28 上午11:51:49
 * 
 */
public class Main {

	/**
	 * @throws Exception
	 * @throws FileNotFoundException
	 * 
	 * @Title: main
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param args
	 *            参数1:启动类型(客户端|服务端)
	 * @throws
	 */
	public static void main(String[] args) {

		// 解析传入参数类
		ParseArgs parseArgs;
		try {
			parseArgs = new ParseArgs(args);

			MonkeyMainI mainIn = (MonkeyMainI) parseArgs.getMainClassDefault();

			mainIn.init(parseArgs);

			mainIn.run();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}

		System.exit(0);
	}
}
