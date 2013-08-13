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

	// 判定是否为第一次获取启动设备
	private int flagFirstTime = 0;

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

		getCmdData = new DataChange(ZookeeperFactory.getZookeeper(), dateNode,
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
						setReturnData("Create return Node\n");

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
						setReturnData(result);

						if (lastResult != null && lastResult.equals("ERROR")) {
							setCompleteReturn(lastResult, resultMsg);
							MonkeyListenerForRunServerCmd.runningProcess
									.remove(dateNode);
							MonkeyOut.debug(getClass(), "Task Over : "
									+ dateNode);
							return;
						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						lastResult = "ERROR";
						setCompleteReturn(lastResult, e.toString());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						lastResult = "ERROR";
						setCompleteReturn(lastResult, e.toString());
					}

				}

				// 全部指令结束,告诉服务器,数据完成,设置cmd值
				if (lastResult != null && lastResult.equals("ERROR")) {

				} else {
					setCompleteReturn(lastResult, resultMsg);
					// 永远不进行remove,remove之后,造成,如果任务重启,会再次运行的悲剧,目前暂时如此解决
					// MonkeyListenerForRunServerCmd.runningProcess
					// .remove(dateNode);
					MonkeyOut.debug(getClass(), "Task Over : " + dateNode);
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
				zk = ZookeeperFactory.getZookeeper();
				States s = zk.getState();
				if (!s.isConnected()) {
					Thread.sleep(3000);
					MonkeyOut.debug(getClass(), "Error connect zk : " + s);
					continue;
				}
				Stat stat = zk.exists(resultNode, false);
				if (stat != null) {
					break;
				} else {
					countKill++;
					Thread.sleep(10000);
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
			if (isFirstTime == 1) {
				try {
					ZooKeeper zk = ZookeeperFactory.getZookeeper();
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
					MonkeyOut.debug(getClass(), "Create Return Node :"
							+ this.resultNodeProcess);
				} catch (KeeperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				ZooKeeper zk = ZookeeperFactory.getZookeeper();

				byte[] old = zk.getData(resultNodeProcess, false, null);
				String valueOld = "";
				if (old != null) {
					valueOld = new String(old);
				}
				valueOld += result;

				MonkeyOut.debug(getClass(), "Set Return Value :" + result);
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
			flagFirstTime++;
			MonkeyOut.debug(getClass(), "processResult DialRequestThreads:["
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
					MonkeyOut.debug(getClass(), "Already exists data "
							+ valueOld);
				} else {
					MonkeyOut.debug(getClass(), "First  data");
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

}
