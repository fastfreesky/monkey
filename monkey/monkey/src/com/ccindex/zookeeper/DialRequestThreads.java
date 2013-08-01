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
import com.ccindex.constant.Debug;
import com.ccindex.listener.MonkeyListenerForRunServerCmd;
import com.ccindex.listener.MonkeyListenerForGetServerCmd;
import com.ccindex.operator.DataChange;
import com.ccindex.tool.CmdParse;
import com.ccindex.watcher.MonkeyClientWatcher;

/**
 * 
 * @ClassName: DialRequestThreads
 * @Description: TODO(这里用一句话描述这个类的作用)多线程处理应用请求
 * @author tianyu.yang
 * @date 2013-3-12 下午1:46:48 Runnable
 */
public class DialRequestThreads implements StatCallback, Runnable {

	// 判定是否为第一次获取启动设备
	private int flagFirstTime = 0;

	// private ZooKeeper zk = null;

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
	private String dateNode;

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
	public DialRequestThreads(String node, String resultNode, String perl,
			Watcher watcher) {

		dateNode = node;
		this.resultNode = resultNode;

		// this.zk = zk;
		this.perl = perl;
		listenGetCmd = new MonkeyListenerForGetServerCmd();

		getCmdData = new DataChange(Zoo.getZookeeper(), dateNode, watcher,
				listenGetCmd);

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
			String cmd = listenGetCmd.getValue();

			Debug.info(getClass(), "Ready Cmd: " + cmd);

			// //测试结束标志
			// setCompleteReturn(lastResult, "Test");
			// return;
			//
			if (cmd != null) {
				// 待执行的命令

				// // 获取将执行的指令,进行翻译
				ArrayList<String> cmdList = CmdParse.parseCmd(cmd);
				if (cmdList == null) {
					// setReturnData("Error cmd " + cmd);
					Debug.info(getClass(), "Error cmd " + cmd);
					return;
				}

				// 返回执行命令的结果信息
				String resultMsg = "";
				for (String cm : cmdList) {
					try {
						// 结束标志,不在进行返回结果
						setReturnData("Create return Node\n");

						// String cmdPackage = cm;
						String cmdPackage = perl + " \""
								+ cm.replaceAll("\"", "\\\\\"") + "\"";
						Debug.info(getClass(), "Running job: " + cmdPackage);

						Process child = Runtime.getRuntime().exec(
								new String[] { "/bin/sh", "-c", cmdPackage },
								null, null);
						child.waitFor();

						resultMsg += DebugResult(child);
						// 将执行结果返回
						result = Constant.getHostname() + " Running cmd [" + cm
								+ "] " + lastResult + " \n";
						setReturnData(result);

						if (lastResult != null && lastResult.equals("ERROR")) {
							setCompleteReturn(lastResult, resultMsg);
							MonkeyListenerForRunServerCmd.runningProcess
									.remove(dateNode);
							Debug.debug(getClass(), "Task Over : " + dateNode);
							return;
						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				// 全部指令结束,告诉服务器,数据完成,设置cmd值
				if (lastResult != null && lastResult.equals("ERROR")) {

				} else {
					setCompleteReturn(lastResult, resultMsg);
					MonkeyListenerForRunServerCmd.runningProcess
							.remove(dateNode);
					Debug.debug(getClass(), "Task Over : " + dateNode);
					return;
				}
			} else {
				return;

			}

		}

	}

	private void setCompleteReturn(String key, String result) {

		try {
			/**
			 * 第一次进入,需要创建路径,存储数据
			 */
			ZooKeeper zk = null;
			// 延时几秒,确定服务器段建立父节点OK,避免去监测,逻辑复杂
			while (true) {
				zk = Zoo.getZookeeper();
				States s = zk.getState();
				if (!s.isConnected()) {
					Thread.sleep(3000);
					Debug.debug(getClass(), "Error connect zk : " + s);
					continue;
				}
				Stat stat = zk.exists(resultNode, false);
				if (stat != null) {
					break;
				} else {
					countKill++;
					Thread.sleep(3000);
					if (countKill > 100) {
						return;
					}
				}
			}

			String tmpPath = resultNode + "/" + Constant.getHostname() + "-["
					+ key + "]_";
			this.resultNodeLast = zk.create(tmpPath, result.getBytes(),
					Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void setReturnData(String result) {
		try {
			isFirstTime++;
			/**
			 * 第一次进入,需要创建路径,存储数据
			 */
			ZooKeeper zk = Zoo.getZookeeper();

			if (isFirstTime == 1) {
				try {
					while (true) {
						Stat stat;

						stat = zk.exists(resultNode, false);
						if (stat != null) {
							break;
						} else {
							Thread.sleep(3000);
						}
					}
					String tmpPath = resultNode + "/" + Constant.getHostname();
					this.resultNodeProcess = zk.create(tmpPath,
							result.getBytes(), Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT);
					Debug.debug(getClass(), "Create Return Node :"
							+ this.resultNodeProcess);
				} catch (KeeperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				byte[] old = zk.getData(resultNodeProcess, false, null);
				String valueOld = "";
				if (old != null) {
					valueOld = new String(old);
				}
				valueOld += result;

				Debug.debug(getClass(), "Set Return Value :" + valueOld);
				zk.setData(resultNodeProcess, valueOld.getBytes(), -1);
			}

		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String DebugResult(Process child) {

		InputStreamReader ir = new InputStreamReader(child.getInputStream());
		LineNumberReader input = new LineNumberReader(ir);
		String line = null;
		String isState = null;
		StringBuffer buf = new StringBuffer();
		try {
			while ((line = input.readLine()) != null) {
				Debug.info(getClass(), line);
				isState = line;
				buf.append(line).append("\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 最后一行,取运行结果标志
		if (isState != null && isState.contains("[0]")) {
			lastResult = "OK";
		} else {
			lastResult = "ERROR";
		}

		return buf.toString();
	}

	@Override
	public void processResult(int rc, String path, Object ctx, Stat stat) {
		// TODO Auto-generated method stub
		// System.out.println("processResult:" + );
		ZooKeeper zk = Zoo.getZookeeper();
		boolean exists;
		switch (rc) {
		case Code.Ok:// 一切正常
			flagFirstTime++;
			System.out.println("processResult DialRequestThreads:["
					+ flagFirstTime + "]" + path);
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
			try {
				String valueOld = "";
				if (zk.getData(resultNode, false, null) != null) {

					valueOld = new String(zk.getData(resultNode, false, null));

					System.out.println("Already exists data " + valueOld);
				} else {
					System.out.println("First  data");
				}
				valueOld += result;

				synchronized (resultNode) {
					zk.setData(resultNode, valueOld.getBytes(), -1);
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

	public static void main(String[] args) {

		ArrayList<String> cmdList = CmdParse
				.parseCmd("monkey --only CHN-DG-9-5A3 CHN-DG-9-5A4 CHN-DG-9-5A5 CHN-DG-9-5A6 CHN-DG-9-5A7 CHN-DG-9-5A8 CHN-DG-9-5A9 CHN-DG-9-5AA CHN-DG-9-5AB CHN-DG-9-5AC CHN-DG-9-5AD CHN-DG-9-5AE CHN-DG-9-5AF CHN-DG-9-5AG CHN-DG-9-5AH CHN-DG-9-5AJ CHN-DG-9-5AK CHN-DG-9-5AL CHN-DG-9-5AM CHN-DG-9-5AN CHN-DG-9-5AO CHN-DG-9-5AP CHN-DG-9-5AQ CHN-DG-9-5AR CHN-DG-9-5AS CHN-DG-9-5AT CHN-DG-9-5AU CHN-DG-9-5AV  -c\"rm -rf /Application/etl/state1/\"");

		String line = "[Thread-1]- 2013-03-19 16:12:04,511 - com.ccindex.zookeeper.DialRequestThreads -  - drwxr-xr-x  27 root root  4096 Sep 11  2012 var";
		if (line.contains("[0]")) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR");
		}
	}

}
