package com.ccindex.main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.zookeeper.KeeperException;
import com.ccindex.constant.Constant;
import com.ccindex.interfaceI.MonkeyMainI;
import com.ccindex.tool.ParseArgs;
import com.ccindex.warn.MonkeyOut;
import com.ccindex.zookeeper.MonkeyClient;

/**
 * 
 * @ClassName: Client
 * @Description: TODO(这里用一句话描述这个类的作用)客户端入口
 * @author tianyu.yang
 * @date 2013-3-5 下午5:17:51
 * 
 */
public class Client implements MonkeyMainI {

	private ParseArgs args;
	private String ipPort;
	// 执行shell脚本的脚本
	private String shell;

	private void setHostName() {
		InetAddress ia;
		try {
			ia = InetAddress.getLocalHost();
			Constant.setHostname(ia.getHostName());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void init(ParseArgs args) throws Exception {
		// TODO Auto-generated method stub
		this.args = args;
		ipPort = args.getIpPort();
		shell = args.getShell();

		setHostName();
	}

	@Override
	public void run() throws KeeperException, IOException {
		// TODO Auto-generated method stub
		MonkeyOut.info(getClass(), "Begin...");
		new MonkeyClient(ipPort, shell, args.getRetryTimesDefault(),
				args.getTimeout()).run();

		MonkeyOut.info(getClass(), "End...");
	}
}
