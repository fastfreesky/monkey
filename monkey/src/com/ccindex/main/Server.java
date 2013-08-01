package com.ccindex.main;

import org.apache.zookeeper.ZooKeeper;

import com.ccindex.constant.Debug;
import com.ccindex.tool.ParseArgs;
import com.ccindex.zookeeper.MonkeyServer;

/**
 * 
 * @ClassName: Server
 * @Description: TODO(这里用一句话描述这个类的作用)服务端入口
 * @author tianyu.yang
 * @date 2013-3-5 下午5:18:05
 * 
 */
public class Server implements MonkeyMainI {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//
		// String aa= "set {monkey --only D:\\home -c\"ls -al /\"}";
		// String bb=aa.replaceAll("\\\\", "\\\\\\\\");
		// System.out.println(bb);
		// System.out.println(aa.replaceAll("D:\\\\home",
		// "-------------------"));

		if (args.length < 1) {
			System.out
					.println("Params:\n\t[1]--hostport:IP:Port(Eg:127.0.0.1:2181);\n");
			System.exit(2);
		}
		Debug.info(Server.class, "Begin...");

		// 主机及端口, 监控的路径,心跳时间(暂不对外提供)
		String hostPort = args[0];

		try {
			new MonkeyServer(hostPort).run();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Debug.info(Server.class, "End...");
	}

	private String ipPort = null;

	@Override
	public void init(ParseArgs args) {
		// TODO Auto-generated method stub
		ipPort = args.getIpPort();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
}
