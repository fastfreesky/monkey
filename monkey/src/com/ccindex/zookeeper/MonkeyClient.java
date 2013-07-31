package com.ccindex.zookeeper;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.ccindex.constant.Constant;
import com.ccindex.listener.MonkeyListenerForRunServerCmd;
import com.ccindex.listener.MonkeyListenerForGetServerCmd;
import com.ccindex.operator.ChildrenChange;
import com.ccindex.operator.DataChange;
import com.ccindex.record.RegisterErrorRecordToServer;
import com.ccindex.tool.ParseCmd;
import com.ccindex.warn.MonkeyOut;
import com.ccindex.watcher.MonkeyClientWatcher;

/**
 * 
 * @ClassName: Executor
 * @Description: TODO(这里用一句话描述这个类的作用) zookeeper线程运行类
 * @author tianyu.yang
 * @date 2013-2-26 下午5:25:55
 *       备注:该类是一个逻辑复杂类,继承接口Watcher(观察接口,检测zookeeper服务器是否有变化), Runnable(线程)
 */
public class MonkeyClient implements Runnable {

	private ZooKeeper zk = null;

	// 监测命令的变化
	private ChildrenChange getChild;
	private MonkeyListenerForRunServerCmd listenGetChild;

	// 观察者的类
	private MonkeyClientWatcher clientWathcer;

	// 连接IP
	private String hostPort;
	// Perl
	private String perPath;
	private int retryTimes;
	private int timeout;

	/**
	 * 
	 * @Title: Executor.java
	 * @Description: 设置服务器IP及端口及对应的监控znode
	 * @param hostPort
	 *            服务器的IP和端口
	 * @param znode
	 *            zookeeper的节点
	 * @throws KeeperException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public MonkeyClient(String hostPort, String perPath, int retryTimes,
			int timeout) throws KeeperException, IOException {

		this.hostPort = hostPort;
		this.perPath = perPath;
		this.retryTimes = retryTimes;
		this.timeout = timeout;

		// 监听端口保持一份
		clientWathcer = new MonkeyClientWatcher();
		listenGetChild = new MonkeyListenerForRunServerCmd();

		initZookeeper();
	}

	private void initZookeeper() throws IOException {
		zk = new ZooKeeper(hostPort, 20000, clientWathcer);
		clientWathcer.setZk(zk);

		// 未成功连接,等待成功,成功之后,再进行其他操作
		while (!clientWathcer.flagSucceedConnect) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// 设置新的zk
		listenGetChild.setZKAndPerlPathAdnWatcher(zk, perPath, clientWathcer);

		getChild = new ChildrenChange(zk, "/cmd", null, listenGetChild, false);
		clientWathcer.setGetChild(getChild);
	}

	@Override
	public void run() {
		// synchronized (this) {
		int times = 0;
		while (true) {
			while (!clientWathcer.flagEnd) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// 程序宕机,恢复初始化
			clientWathcer.flagEnd = false;

			MonkeyOut.info(getClass(), "Child Process killed");

			times++;

			if (times > retryTimes) {
				MonkeyOut.info(getClass(), "End...Crash Down more than "
						+ retryTimes + "times");
				break;
			}
			MonkeyOut.info(getClass(), "Client Start again...[" + times
					+ " ] times");

			RegisterErrorRecordToServer.setError("Client Start again...["
					+ times + " ] times");
			// 宕机之后,10分钟后恢复
			try {
				Thread.sleep(timeout);

				initZookeeper();

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// if (args.length < 4) {
		// System.err
		// .println("USAGE: Executor hostPort znode filename program [args ...]");
		// System.exit(2);
		// }
		// String hostPort = args[0];
		// String znode = args[1];
		// String filename = args[2];
		// String exec[] = new String[args.length - 3];
		// System.arraycopy(args, 3, exec, 0, exec.length);
		// try {
		// new Executor(hostPort, znode, filename, exec).run();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}
}
