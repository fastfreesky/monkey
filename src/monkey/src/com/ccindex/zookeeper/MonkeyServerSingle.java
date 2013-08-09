package com.ccindex.zookeeper;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import com.ccindex.constant.Constant;
import com.ccindex.listener.MonkeyListenerForGatherClientResult;
import com.ccindex.operator.ChildrenChange;
import com.ccindex.tool.CmdSet;
import com.ccindex.tool.ParseCmd;
import com.ccindex.warn.MonkeyOut;
import com.ccindex.watcher.MonkeyServerWatcher;

/**
 * 
 * @ClassName: Executor
 * @Description: TODO(这里用一句话描述这个类的作用) zookeeper线程运行类
 * @author tianyu.yang
 * @date 2013-2-26 下午5:25:55
 *       备注:该类是一个逻辑复杂类,继承接口Watcher(观察接口,检测zookeeper服务器是否有变化),
 *       Runnable(线程),DataMonitor.DataMonitorListener(监听类)
 */
public class MonkeyServerSingle implements Runnable {

	private ZooKeeper zk = null;

	// 创建的命令,及返回的结果集
	private String cmdRun = null, resultRun = null;

	private MonkeyServerWatcher wathcer;
	// 监测命令的变化
	private ChildrenChange getChild;
	private MonkeyListenerForGatherClientResult listenGetChild;

	private String inputCmd = null;

	private ParseCmd cmd;

	/**
	 * 
	 * @Title: Executor.java
	 * @Description: 设置服务器IP及端口
	 * @param hostPort
	 *            服务器的IP和端口
	 * @param znode
	 *            zookeeper的节点
	 * @throws KeeperException
	 * @throws IOException
	 */
	public MonkeyServerSingle(String hostPort, ParseCmd cmd)
			throws KeeperException, IOException {

		wathcer = new MonkeyServerWatcher();
		this.cmd = cmd;

		ZookeeperFactory.init(hostPort, 20000, wathcer);
		zk = ZookeeperFactory.getZookeeper();

		try {
			// 创建时候,就完成创建命令,直接等待输入,避免客户端监测不到路径
			if (!cmd.isRunningCmd(zk)) {
				ZookeeperFactory.close();
				throw new IOException("Now is not useful to Running...: "
						+ cmd.getErrorReason());
			}

			cmdRun = zk.create(CmdSet.CMDNEW, null, Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);

			MonkeyOut.info(getClass(), "Create node [" + cmdRun + "] OK");

			// 获取返回结果值,打印到屏幕
			String new_client = cmdRun.replaceFirst("/cmd", "/result");
			// String value="";
			resultRun = zk.create(new_client, null, Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
			MonkeyOut.info(getClass(), "Create node [" + resultRun + "] OK");

			Thread.sleep(3000);

			Constant.setDeletePath(resultRun);
			// byte [] valueBytes = zk.getData(result2, true, null);
			// ResultData resultData = new ResultData(zk, resultRun);
			// 注册返回信息,及时获取返回信息
			listenGetChild = new MonkeyListenerForGatherClientResult(zk, null,
					cmdRun, resultRun, cmd);
			getChild = new ChildrenChange(zk, resultRun, listenGetChild, true);
			wathcer.setGetChild(getChild);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		MonkeyOut.info(getClass(), "Waiting for conning......");

		while (wathcer.isConnect()) {

			try {
				inputCmd = cmd.getFinalCmd();

				String cmdFinal = "set {" + inputCmd + "}";
				boolean result = CmdSet.dialSet(zk, cmdRun, cmdFinal);

				if (result) {
					listenGetChild.waitForEnd();
				}

				CmdSet.dialDelete(zk, Constant.getDeletePath());
				System.out.println("Close!");
				break;

			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		ZookeeperFactory.close();
	}

}
