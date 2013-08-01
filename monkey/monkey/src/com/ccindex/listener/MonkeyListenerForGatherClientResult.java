package com.ccindex.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.ccindex.constant.Constant;
import com.ccindex.constant.Debug;
import com.ccindex.operator.DataChange;
import com.ccindex.record.RecordToFile;
import com.ccindex.record.RetryFailedEvent;
import com.ccindex.tool.CmdSet;
import com.ccindex.warn.SendMail;
import com.ccindex.zookeeper.DialRequestThreads;

/**
 * 
 * @ClassName: MonkeyListenerForGetChildOfGetData
 * @Description: TODO(这里用一句话描述这个类的作用)用于监测获取命令后,处理有变化的命令路径,用于服务器端
 * @author tianyu.yang
 * @date 2013-3-14 下午5:11:59
 * 
 */
public class MonkeyListenerForGatherClientResult implements
		MonkeyListener<List<String>> {

	// 对结果进行比对,确定是否正确结束
	// 正在运行的机器 BGP-BJ-9-3m1
	private ArrayList<String> hostRuning = new ArrayList<String>();
	// 运行结束的机器 BGP-BJ-9-3m1-[OK]_0000000002
	private ArrayList<String> hostRunEnd = new ArrayList<String>();
	// 运行结束的机器 错误机器
	private ArrayList<String> hostRunEndError = new ArrayList<String>();

	private ZooKeeper zk = null;
	//
	private Watcher watcher;

	private String cmd;

	private String result;

	private static Pattern hostRuningR = Pattern.compile("^((\\S+-){3}\\w+)$");
	private static Pattern hostRuningE = Pattern
			.compile("^((\\S+-){3}\\S+)-\\[(\\S+)\\]_\\S+$");

	public volatile boolean isSucceed = false;

	private static int firstTime = 0;

	public void init() {
		hostRuning.clear();
		hostRunEnd.clear();
		isSucceed = false;
		firstTime = 0;
		timer = null;
	}

	public MonkeyListenerForGatherClientResult(ZooKeeper zk,
			Watcher watcher, String cmd) {
		// TODO Auto-generated constructor stub
		this.zk = zk;
		this.watcher = watcher;
		this.cmd = cmd;
	}

	/**
	 * 
	 * @Title: MonkeyListenerForGetChildOfGetDataServer.java
	 * @Description:
	 * @param zk
	 * @param watcher
	 * @param cmd
	 *            传入执行的命令的父目录
	 * @param result
	 *            传入结果返回的父目录
	 */
	public MonkeyListenerForGatherClientResult(ZooKeeper zk,
			Watcher watcher, String cmd, String result) {
		// TODO Auto-generated constructor stub
		this.zk = zk;
		this.watcher = watcher;
		this.cmd = cmd;
		this.result = result;
	}

	private Timer timer = null;

	private void dialList(List<String> t) {
		Matcher matchR = null, matchE = null;
		for (String tt : t) {
			matchE = hostRuningE.matcher(tt);
			matchR = hostRuningR.matcher(tt);

			if (matchR.find()) {
				if (CmdSet.hostNameTask.contains(matchR.group(1))) {
					hostRuning.add(matchR.group(1));
					String value = "Now Running host: [" + hostRuning.size()
							+ "/" + CmdSet.total + "] " + tt;

					debugRecord(value);

				}
			}
			if (matchE.find()) {
				String state = matchE.group(3);
				if (state.equals("OK")) {
					hostRunEnd.add(matchE.group(1));

					String value = "Now Running Ok host: [" + hostRunEnd.size()
							+ "/" + CmdSet.total + "] " + tt;

					// 摘取hostName
					RetryFailedEvent.setHostNameTaskSucceed(matchE.group(1));

					debugRecord(value);

				} else {
					hostRunEndError.add(matchE.group(1));

					String value = "Now Running Error host: ["
							+ hostRunEndError.size() + "/" + CmdSet.total
							+ "] " + tt;

					debugRecord(value);
				}

				String reusltDisp = result + "/" + tt;
				try {
					byte[] values = zk.getData(reusltDisp, false, null);
					if (values != null) {
						String[] arr = new String(values).split("\n");
						for (String ar : arr) {
							String value = "\t\t" + ar;
							Debug.debug(getClass(), value);
							System.out.println(value);
							RecordToFile.record(value);
						}
					}
				} catch (KeeperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		firstTime++;
		if (CmdSet.timeout > 0 && firstTime == 1) {
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					debugRecord("Time out.....");
					if (CmdSet.less <= hostRunEnd.size()) {
						debugResult();
						if (!CmdSet.setCmdPackage(zk, cmd)) {
							Constant.setSucceed(true);
							isSucceed = true;
						} else {
							init();
						}
					} else {
						debugResult();
						debugRecord("Error..to..run...");
						isSucceed = true;
					}

				}

			}, CmdSet.timeout * 1000);
		}
		// 成功失败个数相加==总数时候,退出,以失败结束或者全部失败时候
		if (CmdSet.total == hostRunEndError.size()
				|| (CmdSet.total <= (hostRunEndError.size() + hostRunEnd.size()) && hostRunEndError
						.size() != 0)) {
			if (timer != null)
				timer.cancel();
			debugResult();
			debugRecord("Error..to..run...");
			Constant.setSucceed(true);			
			isSucceed = true;
			return;
		}
		if (CmdSet.total == hostRunEnd.size()) {
			if (timer != null)
				timer.cancel();
			debugResult();
			if (!CmdSet.setCmdPackage(zk, cmd)) {
				Constant.setSucceed(true);
				isSucceed = true;
			} else {
				init();
			}
		} else {
			isSucceed = false;
		}

	}

	private void debugRecord(String value) {
		System.out.println(value);

		RecordToFile.record(value);

		Debug.debug(getClass(), value);
	}

	private void debugResult() {

		debugRecord("All     tasks: " + CmdSet.total);
		debugRecord("Less    tasks: " + CmdSet.less);
		debugRecord("Succeed tasks: " + hostRunEnd.size());
		debugRecord("Failed  tasks: " + hostRunEndError.size());

		SendMail.packageMail(SendMail.getTitle(), "All     tasks: "
				+ CmdSet.total);
		SendMail.packageMail(SendMail.getTitle(), "Less    tasks: "
				+ CmdSet.less);
		SendMail.packageMail(SendMail.getTitle(), "Succeed tasks: "
				+ hostRunEnd.size());
		SendMail.packageMail(SendMail.getTitle(), "Failed  tasks: "
				+ hostRunEndError.size());
	}

	/**
	 * 
	 * (非 Javadoc)
	 * 
	 * @Title: exists
	 * @Description:
	 * @param t传入的数据
	 *            ,一定和上一次不同
	 * @return
	 * @see com.ccindex.listener.MonkeyListener#exists(java.lang.Object)
	 */
	@Override
	public boolean exists(List<String> t) {
		// TODO Auto-generated method stub
		// 数据一定不为空
		// Debug.debug(getClass(), "Now Running host: "+t);
		// .debug("处理存在的数据....获取到子节点的变化 " + t);

		dialList(t);

		return false;
	}

	public static void main(String[] args) {
		String input = "BGP-BJ-9-3m1-[OK]_0000000001";
		// input = "BGP-BJ-9-3m1-[OK]_0000000002";
		ArrayList<String> arr = new ArrayList<String>();
		arr.add("BGP-BJ-9-3m1-[OK]_0000000001");
		arr.add("BGP-BJ-9-3m1");

		MonkeyListenerForGatherClientResult aa = new MonkeyListenerForGatherClientResult(
				null, null, null);
		aa.dialList(arr);

		Matcher match = hostRuningE.matcher(input);

		if (match.find()) {
			System.out.println("Find...." + match.group(1) + match.group(3));
		}
		match = hostRuningR.matcher(input);

		if (match.find()) {
			System.out.println("Find...." + match.group(1));
		}
	}

}
