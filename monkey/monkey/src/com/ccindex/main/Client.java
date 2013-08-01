package com.ccindex.main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;

import com.ccindex.constant.Constant;
import com.ccindex.constant.Debug;
import com.ccindex.record.RegisterErrorRecordToServer;
import com.ccindex.zookeeper.MonkeyClient;

/**
 * 
 * @ClassName: Client
 * @Description: TODO(这里用一句话描述这个类的作用)客户端入口
 * @author tianyu.yang
 * @date 2013-3-5 下午5:17:51
 * 
 */
public class Client {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Debug.info(Client.class, "Begin...");
		if (args.length < 2) {
			System.out
					.println("Params:\n\t[1]--hostport:IP:Port(Eg:127.0.0.1:2181);\n\t[2]--Perl");
			System.exit(2);
		}
		// 主机及端口, 监控的路径,心跳时间(暂不对外提供)
		String hostPort = args[0];
		String perPath = args[1];

		InetAddress ia;
		try {
			ia = InetAddress.getLocalHost();
			Constant.setHostname(ia.getHostName());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {

			MonkeyClient client = new MonkeyClient(hostPort, perPath);
			client.run();

		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


