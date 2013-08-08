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

import com.ccindex.interfaceI.MonkeyListenerI;
import com.ccindex.operator.DataChange;
import com.ccindex.record.RecordToFile;
import com.ccindex.tool.ParseCmd;
import com.ccindex.warn.MonkeyOut;
import com.ccindex.warn.SendMail;

/**
 * 
 * @ClassName: MonkeyListenerForGetChildOfGetData
 * @Description: TODO(这里用一句话描述这个类的作用)用于监测获取命令后,处理有变化的命令路径,用于服务器端
 * @author tianyu.yang
 * @date 2013-3-14 下午5:11:59
 * 
 */
public class MonkeyListenerForGatherClientResult implements
		MonkeyListenerI<List<String>> {

	private ZooKeeper zk = null;
	//
	private Watcher watcher;

	private String cmd;

	private String result;

	private static Pattern hostRuningR = Pattern.compile("^((\\S+-){3}\\w+)$");
	private static Pattern hostRuningE = Pattern
			.compile("^((\\S+-){3}\\S+)-\\[(\\S+)\\]_\\S+$");

	private volatile boolean isSucceed = false;

	public boolean isSucceed() {
		return isSucceed;
	}

	public void setSucceed(boolean isSucceed) {
		this.isSucceed = isSucceed;
	}

	public void waitForEnd() {
		while (!isSucceed()) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private ParseCmd parseCmd;

	public void init() {
		setSucceed(false);
		timer = null;
		isTimer = false;
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
	public MonkeyListenerForGatherClientResult(ZooKeeper zk, Watcher watcher,
			String cmd, String result, ParseCmd parseCmd) {
		// TODO Auto-generated constructor stub
		this.zk = zk;
		this.watcher = watcher;
		this.cmd = cmd;
		this.result = result;
		this.parseCmd = parseCmd;
	}

	private Timer timer = null;
	private volatile boolean isTimer = false;

	private void dialList(List<String> t) {
		Matcher matchR = null, matchE = null;
		for (String tt : t) {
			matchE = hostRuningE.matcher(tt);
			matchR = hostRuningR.matcher(tt);

			// 正在运行的设备
			if (matchR.find()) {

				if (parseCmd.isHostName(matchR.group(1))) {

					parseCmd.addHostNameTaskRunning(matchR.group(1));

					String value = "Now Running host: ["
							+ parseCmd.getHostNameTaskRunning().size() + "/"
							+ parseCmd.getTotal() + "] " + tt;

					debugRecord(value);
				}
			}
			// 结束的设备
			if (matchE.find()) {
				String state = matchE.group(3);
				if (state.equals("OK")) {
					// 摘取hostName
					parseCmd.addHostNameTaskSucceed(matchE.group(1));

					String value = "Now Running Ok host: ["
							+ parseCmd.getHostNameTaskSucceed().size() + "/"
							+ parseCmd.getTotal() + "] " + tt;

					debugRecord(value);

				} else {

					parseCmd.addHostNameTaskFailed(matchE.group(1));

					String value = "Now Running Error host: ["
							+ parseCmd.getHostNameTaskFailed().size() + "/"
							+ parseCmd.getTotal() + "] " + tt;

					debugRecord(value);
				}

				// 显示返回的信息
				dispReturnMsg(tt);
			}

		}

		// 需要定时器
		if (parseCmd.getTimeout() > 0 && timer == null) {
			dialTimer();
		}
		// 定时器超时
		if (isTimer) {
			return;
		}
		// 成功失败个数相加==总数时候,退出,以失败结束或者全部失败时候
		int failedSize = parseCmd.getHostNameTaskFailed().size();
		int endSize = parseCmd.getHostNameTaskSucceed().size();

		if (parseCmd.getTotal() == failedSize
				|| (parseCmd.getTotal() <= (failedSize + endSize) && failedSize != 0)) {
			if (timer != null)
				timer.cancel();
			debugResult();
			debugRecord("Error..to..run...");
			parseCmd.setSucceed(true);
			setSucceed(true);
			return;
		} else if (parseCmd.getTotal() <= endSize) {
			if (timer != null)
				timer.cancel();
			debugResult();
			debugRecord("Succeed to run...");
			parseCmd.setSucceed(true);
			setSucceed(true);
		}

	}

	private void dialTimer() {
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				isTimer = true;
				debugRecord("Time out.....");
				// 满足最少成功数,算成功
				int endSize = parseCmd.getHostNameTaskSucceed().size();
				if (parseCmd.getLess() <= endSize) {
					debugResult();
					parseCmd.setSucceed(true);
				} else {
					debugResult();
					debugRecord("Error..to..run...");
					parseCmd.setSucceed(false);
				}

				// 设置标志位退出
				setSucceed(true);

			}

		}, parseCmd.getTimeout() * 1000);
	}

	private void dispReturnMsg(String tt) {
		String reusltDisp = result + "/" + tt;
		try {
			byte[] values = zk.getData(reusltDisp, false, null);
			if (values != null) {
				String[] arr = new String(values).split("\n");
				for (String ar : arr) {
					String value = "\t\t" + ar;

					debugRecord(value);
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

	private void debugRecord(String value) {
		RecordToFile.record(value);
		MonkeyOut.debug(getClass(), value);
		SendMail.packageMail(SendMail.getTitle(), value);
		System.out.println(value);
	}

	private void debugResult() {

		debugRecord("All     tasks: " + parseCmd.getTotal());
		debugRecord("Less    tasks: " + parseCmd.getLess());
		debugRecord("Succeed tasks: "
				+ parseCmd.getHostNameTaskSucceed().size());
		debugRecord("Failed  tasks: " + parseCmd.getHostNameTaskFailed().size());

		SendMail.packageMail(SendMail.getTitle(),
				"All     tasks: " + parseCmd.getTotal());
		SendMail.packageMail(SendMail.getTitle(),
				"Less    tasks: " + parseCmd.getLess());
		SendMail.packageMail(SendMail.getTitle(), "Succeed tasks: "
				+ parseCmd.getHostNameTaskSucceed().size());
		SendMail.packageMail(SendMail.getTitle(), "Failed  tasks: "
				+ parseCmd.getHostNameTaskFailed().size());
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
	 * @see com.ccindex.interfaceI.MonkeyListenerI#exists(java.lang.Object)
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

}
