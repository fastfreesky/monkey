package com.ccindex.zookeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event;

import com.ccindex.record.RegisterErrorRecordToServer;
import com.ccindex.warn.MonkeyOut;
import com.ccindex.watcher.WatcherImpl;

public class MonkeyCheck extends WatcherImpl implements Runnable {

	// 对结果进行比对,确定是否正确结束
	// 正在运行的机器 BGP-BJ-9-3m1
	private ArrayList<String> hostRuning = new ArrayList<String>();
	// 运行结束的机器 BGP-BJ-9-3m1-[OK]_0000000002
	private ArrayList<String> hostRunEnd = new ArrayList<String>();
	// 运行结束的机器 错误机器
	private ArrayList<String> hostRunEndError = new ArrayList<String>();

	private static Pattern hostRuningR = Pattern.compile("^((\\S+-){3}\\w+)$");
	private static Pattern hostRuningE = Pattern
			.compile("^((\\S+-){3}\\S+)-\\[(\\S+)\\]_\\S+$");

	private ZooKeeper zk = null;

	private String checkCmd;

	public MonkeyCheck(String hostPort, String checkCmd) throws IOException {

		ZookeeperFactory.init(hostPort, 20000, this);

		zk = ZookeeperFactory.getZookeeper();

		this.checkCmd = checkCmd;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			List<String> list = zk.getChildren(checkCmd, false);

			if (list == null) {
				System.out.println("Not Such Task: " + checkCmd);
				return;
			}
			Matcher matchR = null, matchE = null;
			hostRuning.clear();
			hostRunEnd.clear();
			hostRunEndError.clear();
			for (String tt : list) {
				matchE = hostRuningE.matcher(tt);
				matchR = hostRuningR.matcher(tt);
				if (matchR.find()) {
					hostRuning.add(matchR.group(1));
				}

				if (matchE.find()) {
					String state = matchE.group(3);
					if (state.equals("OK")) {
						hostRunEnd.add(matchE.group(1));
					} else {
						hostRunEndError.add(matchE.group(1));
					}
				}
			}

			System.out.println("Task    : \n\t" + hostRuning.toString());
			System.out.println("Succeed : \n\t" + hostRunEnd.toString());
			System.out.println("Failed  : \n\t" + hostRunEndError.toString());

			hostRuning.removeAll(hostRunEnd);
			hostRuning.removeAll(hostRunEndError);

			System.out.println("Running : \n\t" + hostRuning.toString());

		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ZookeeperFactory.close();
		}

	}
}
