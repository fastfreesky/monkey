package com.ccindex.main;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;

import com.ccindex.constant.Debug;
import com.ccindex.zookeeper.MonkeyServerGetServerState;

/**
 * 
 * @ClassName: GetErrorTimes
 * @Description: TODO(这里用一句话描述这个类的作用)获取当前哪些zookeeper已经宕机
 * @author tianyu.yang
 * @date 2013-5-16 下午5:05:49
 * 
 */
public class GetServerState {

	public static void main(String[] args) {

		if (args.length != 2) {
			System.out
					.println("Error Params:\n\t[1]--hostport:IP:Port(Eg:127.0.0.1:2181);\n\t[2]--match node file;\n\t");
			return;
		}

		String ipPort = args[0];
		String nodeFile = args[1];

		Debug.info(GetServerState.class, "Begin..." + args[0]);
		try {
			new MonkeyServerGetServerState(ipPort, nodeFile).run();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Debug.info(GetServerState.class, "End..." + args[0]);
	}
}
