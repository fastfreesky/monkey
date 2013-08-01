package com.ccindex.zookeeper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.ccindex.constant.Constant;
import com.ccindex.constant.Debug;
import com.ccindex.listener.MonkeyListenerForRunServerCmd;
import com.ccindex.listener.MonkeyListenerForGatherClientResult;
import com.ccindex.operator.ChildrenChange;
import com.ccindex.tool.CmdSet;

/**
 * 
 * @ClassName: Executor
 * @Description: TODO(这里用一句话描述这个类的作用) zookeeper线程运行类
 * @author tianyu.yang
 * @date 2013-2-26 下午5:25:55
 *       备注:该类是一个逻辑复杂类,继承接口Watcher(观察接口,检测zookeeper服务器是否有变化),
 *       Runnable(线程),DataMonitor.DataMonitorListener(监听类)
 */
public class MonkeyServer implements Watcher, Runnable {

	private ZooKeeper zk = null;
	// 防止被优化
	private volatile boolean flag_close = false;
	// 创建的命令,及返回的结果集
	private String cmdRun = null, resultRun = null;

	// 监测命令的变化
	private ChildrenChange getChild;
	private MonkeyListenerForGatherClientResult listenGetChild;

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
	public MonkeyServer(String hostPort) throws KeeperException, IOException {
		zk = new ZooKeeper(hostPort, 20000, this);
		// 创建时候,就完成创建命令,直接等待输入,避免客户端监测不到路径
		try {
			cmdRun = zk.create(CmdSet.CMDNEW, null, Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);

			Debug.info(getClass(), "Create node [" + cmdRun + "] OK");

			// 获取返回结果值,打印到屏幕
			String new_client = cmdRun.replaceFirst("/cmd", "/result");
			// String value="";
			resultRun = zk.create(new_client, null, Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
			Debug.info(getClass(), "Create node [" + resultRun + "] OK");

			Constant.setDeletePath(resultRun);
			// byte [] valueBytes = zk.getData(result2, true, null);
			// ResultData resultData = new ResultData(zk, resultRun);
			// 注册返回信息,及时获取返回信息
			listenGetChild = new MonkeyListenerForGatherClientResult(zk,
					null, cmdRun, resultRun);
			getChild = new ChildrenChange(zk, resultRun, null,
					listenGetChild, true);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/***************************************************************************
	 * We do process any events ourselves, we just need to forward them on.
	 * 
	 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.proto.WatcherEvent)
	 */
	@Override
	public void process(WatchedEvent event) {
		Debug.debug(getClass(), "Executor process:" + event);
		if (getChild != null && getChild.isPath(event.getPath())) {
			getChild.process(event);
		}
		flag_close = true;
	}

	@Override
	public void run() {
		Debug.info(getClass(), "Waiting for conning......");
		while (!flag_close) {
			// System.out.println("Conn...");
		}
		ArrayList<String> callback = new ArrayList<String>();
		while (flag_close) {
			callback.clear();
			// 正在执行设置命令
			if (CmdSet.flagExistSet) {
				while (!listenGetChild.isSucceed) {
					// Scanner inTmp = new Scanner(System.in);
					// String readTmp = inTmp.nextLine(); //
					// 读取键盘输入的一行（以回车换行为结束输入）
					// if (readTmp.equals("")) {
					// break;
					// }
				}
				listenGetChild.init();
			}
			String readLine = null;
			System.out.print("Zookeeper Cmd:");
			Scanner in = new Scanner(System.in);
			readLine = in.nextLine(); // 读取键盘输入的一行（以回车换行为结束输入）

			if (readLine.equals(CmdSet.QUIT)) {

				CmdSet.dialDelete(zk, Constant.getDeletePath());
				flag_close = false;
				System.out.println("Close!");
				break;
			} else if (readLine.equals("")) {
				continue;
			} else {
				// System.out.println(readLine);

				try {
					CmdSet.parseLine(readLine, zk, cmdRun, resultRun, callback);
					if (callback.size() > 0) {
						System.out.println(callback);
					}

				} catch (KeeperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			// wait();
		}
		// try {
		// wait();
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

}
