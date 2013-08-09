package com.ccindex.main;

import com.ccindex.interfaceI.MonkeyMainI;
import com.ccindex.tool.ParseArgs;
import com.ccindex.zookeeper.MonkeyCheck;

public class Check implements MonkeyMainI {

	private String checkCmd;
	private String ipPort;

	@Override
	public void init(ParseArgs args) throws Exception {
		// TODO Auto-generated method stub
		checkCmd = args.getCheckCmd();
		ipPort = args.getIpPort();
	}

	@Override
	public void run() throws Exception {
		// TODO Auto-generated method stub
		new MonkeyCheck(ipPort, checkCmd).run();
	}

}
