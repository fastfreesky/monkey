package com.ccindex.main;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;

import com.ccindex.constant.Debug;
import com.ccindex.zookeeper.MonkeyServerGetError;

/**
 * 
 * @ClassName: GetErrorTimes
 * @Description: TODO(这里用一句话描述这个类的作用)统计每天各设备失败次数文件导出
 * @author tianyu.yang
 * @date 2013-5-16 下午5:05:49
 * 
 */
public class GetErrorTimes {

	public static void main(String[] args) {

		if (args.length != 3) {
			System.out
					.println("Error Params:\n\t[1]--hostport:IP:Port(Eg:127.0.0.1:2181);\n\t[2]--date choice;\n\t[3]--out file path");
			return;
		}

		String ipPort = args[0];
		String date = args[1];
		String path = args[2];

		Debug.info(GetErrorTimes.class, "Begin..." + args[0]);
		try {
			new MonkeyServerGetError(ipPort, date, path).run();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Debug.info(GetErrorTimes.class, "End..." + args[0]);
	}
}
