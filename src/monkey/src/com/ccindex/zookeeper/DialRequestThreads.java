package com.ccindex.zookeeper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.chainsaw.Main;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.AsyncCallback.DataCallback;

import com.ccindex.constant.Constant;

import com.ccindex.listener.MonkeyListenerForRunServerCmd;
import com.ccindex.listener.MonkeyListenerForGetServerCmd;
import com.ccindex.operator.DataChange;
import com.ccindex.tool.CmdSet;
import com.ccindex.tool.Hive;
import com.ccindex.tool.Monkey;
import com.ccindex.tool.ParseCmd;
import com.ccindex.warn.MonkeyOut;
import com.ccindex.watcher.MonkeyClientWatcher;

/**
 * 
 * @ClassName: DialRequestThreads
 * @Description: TODO(这里用一句话描述这个类的作用)多线程处理应用请求
 * @author tianyu.yang
 * @date 2013-3-12 下午1:46:48 Runnable
 */
public class DialRequestThreads implements StatCallback, Runnable {

	// 返回结果存储节点
	private String resultNode = null;
	// 待执行的perl脚本路径
	private String perl = null;
	// 返回结果的进度存储节点
	private String resultNodeProcess = null;
	// 返回结果的最终结果存储节点
	private String resultNodeLast = null;
	private String result = null;

	// 获取数据的node
	private String cmdNode;

	// 获取指定的cmd中的值
	private DataChange getCmdData;

	// 监听cmd值的监听函数
	private MonkeyListenerForGetServerCmd listenGetCmd;

	private MonkeyClientWatcher clientWathcer;

	// 判定是否为第一次录入数据
	private volatile int isFirstTime = 0;

	// 最终结果的收集
	private String lastResult = "OK";

	// 强制退出线程的次数限制
	int countKill = 0;

	/**
	 * 
	 * @Title: DialRequestThreads.java
	 * @Description:
	 * @param job
	 *            待执行的命令
	 */
	public DialRequestThreads(String cmdNode, String resultNode, String perl,
			Watcher watcher) {

		this.cmdNode = cmdNode;
		this.resultNode = resultNode;

		// this.zk = zk;
		this.perl = perl;
		listenGetCmd = new MonkeyListenerForGetServerCmd();

		getCmdData = new DataChange(ZookeeperFactory.getZookeeper(), cmdNode,
				watcher, listenGetCmd);

		this.clientWathcer = (MonkeyClientWatcher) watcher;
		clientWathcer.setGetCmdData(getCmdData);

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			while (!listenGetCmd.flagEnd) {
				// System.out.println("触发.....");
			}
			listenGetCmd.flagEnd = false;

			clientWathcer.removeCmdData(getCmdData);

			String cmd = listenGetCmd.getValue();

			MonkeyOut.info(getClass(), "Ready Cmd: " + cmd);

			if (cmd != null) {
				// 待执行的命令
				// // 获取将执行的指令,进行翻译
				ParseCmd parseCmd = new ParseCmd();
				try {
					parseCmd.initClientCmd(cmd);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// 该任务不在该设备运行,忽略
				if (!parseCmd.isHostName(Constant.getHostname())) {
					MonkeyOut.info(getClass(), "Error cmd " + cmd);
					return;
				}

				ArrayList<String> cmdList = parseCmd.getClientCmdList();

				// 返回执行命令的结果信息
				String resultMsg = "";
				for (String cm : cmdList) {
					try {
						// 结束标志,不在进行返回结果
						setRunningHostProcess("Create return Node\n");

						String cmdPackage = cm;
						// String cmdPackage = perl + " \""
						// + cm.replaceAll("\"", "\\\\\"") + "\"";
						if (Monkey.isMonkeySelfCmd(cmdPackage)) {
							cmdPackage = Monkey.packageMonkeyCmd(cmdPackage);
						}

						MonkeyOut
								.info(getClass(), "Running job: " + cmdPackage);

						Hive.isRedirectOut(cmdPackage);

						// boolean a = false;
						// if (a) {
						Process child = Runtime.getRuntime().exec(
								new String[] { "/bin/sh", "-c", cmdPackage },
								null, null);
						child.waitFor();

						resultMsg += DebugResult(child);
						// }
						// 测试命令
						// lastResult = "ERROR";
						// Thread.sleep(6000);

						// 将执行结果返回
						result = Constant.getHostname() + " Running cmd [" + cm
								+ "] " + lastResult + " \n";
						setRunningHostProcess(result);

						if (lastResult != null && lastResult.equals("ERROR")) {
							setCompleteValue(lastResult, resultMsg);
							MonkeyListenerForRunServerCmd.runningProcess
									.remove(cmdNode);
							MonkeyOut.debug(getClass(), "Task Over : "
									+ cmdNode);
							return;
						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						lastResult = "ERROR";
						setCompleteValue(lastResult, e.toString());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						lastResult = "ERROR";
						setCompleteValue(lastResult, e.toString());
					}

				}

				// 全部指令结束,告诉服务器,数据完成,设置cmd值
				if (lastResult != null && lastResult.equals("ERROR")) {

				} else {
					setCompleteValue(lastResult, resultMsg);
					// 永远不进行remove,remove之后,造成,如果任务重启,会再次运行的悲剧,目前暂时如此解决
					// MonkeyListenerForRunServerCmd.runningProcess
					// .remove(cmdNode);
					return;
				}
			} else {
				return;

			}

		}

	}

	/**
	 * 
	 * @Title: setCompleteValue
	 * @Description: TODO(这里用一句话描述这个方法的作用)设置最后的完成状态
	 * @param state
	 * @param result
	 *            void
	 * @throws
	 */
	private void setCompleteValue(String state, String result) {

		ZookeeperFactory.exists(resultNode);
		String tmpPath = CmdSet.packagePath(resultNode, Constant.getHostname()
				+ "-[" + state + "]_");
		resultNodeLast = ZookeeperFactory.create(tmpPath, result.getBytes(),
				Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);

		MonkeyOut.debug(getClass(), "Task Over : " + resultNodeLast);
	}

	/**
	 * 
	 * @Title: setRunningHostProcess
	 * @Description: TODO(这里用一句话描述这个方法的作用)设置正在运行该任务的进度状况
	 * @param result
	 *            void
	 * @throws
	 */

	private void setRunningHostProcess(String result) {
		isFirstTime++;
		/**
		 * 第一次进入,需要创建路径,存储数据
		 */
		if (isFirstTime == 1) {
			String tmpPath = CmdSet.packagePath(resultNode,
					Constant.getHostname());

			resultNodeProcess = ZookeeperFactory.create(tmpPath,
					result.getBytes(), Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
		} else {

			SetData(resultNodeProcess, result);

		}
	}

	private String DebugResult(Process child) {

		InputStreamReader ir = null;

		// 判断任务是否成功

		if (child.exitValue() == 0) {
			lastResult = "OK";
			ir = new InputStreamReader(child.getInputStream());

		} else {
			lastResult = "ERROR";
			ir = new InputStreamReader(child.getErrorStream());

		}

		LineNumberReader input = new LineNumberReader(ir);
		String line = null;
		String isState = null;
		StringBuffer buf = new StringBuffer();
		try {
			while ((line = input.readLine()) != null) {
				isState = line;
				buf.append(line).append("\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MonkeyOut.info(getClass(), buf.toString());

		return buf.toString();
	}

	@Override
	public void processResult(int rc, String path, Object ctx, Stat stat) {
		// TODO Auto-generated method stub
		// System.out.println("processResult:" + );
		ZooKeeper zk = ZookeeperFactory.getZookeeper();
		boolean exists;
		switch (rc) {
		case Code.Ok:// 一切正常
			MonkeyOut.debug(getClass(), "processResult DialRequestThreads: "
					+ path);
			exists = true;
			break;
		case Code.NoNode:// 查询路径不存在
			// exists = false;
			zk.exists(this.resultNode, true, this, null);
			return;
		case Code.SessionExpired:// 服务器意外问题
		case Code.NoAuth:// 未认证的
			return;
		default:// 继续检测
			// Retry errors
			zk.exists(this.resultNode, true, this, null);
			return;
		}

		if (exists) {
			SetData(resultNode, result);
		}
	}

	private void SetData(String path, String Value) {
		String valueOld = ZookeeperFactory.getData(path);
		if (valueOld == null) {
			MonkeyOut.debug(getClass(), "First  data");
		} else {
			MonkeyOut.debug(getClass(), "Already exists data :" + valueOld);
		}

		valueOld += Value;

		synchronized (path) {
			ZookeeperFactory.setData(path, valueOld);
		}
	}
}
