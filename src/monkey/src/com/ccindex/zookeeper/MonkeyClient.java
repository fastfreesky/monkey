package com.ccindex.zookeeper;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.ccindex.listener.MonkeyListenerForRunServerCmd;
import com.ccindex.operator.ChildrenChange;
import com.ccindex.record.RegisterErrorRecordToServer;
import com.ccindex.tool.CmdSet;
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

		ZookeeperFactory.init(hostPort, 20000, clientWathcer);

		initZookeeper();
	}

	private void initZookeeper() throws IOException {
		zk = ZookeeperFactory.getZookeeper();

		// 设置新的zk
		listenGetChild.setZKAndPerlPathAdnWatcher(perPath, clientWathcer);

		getChild = new ChildrenChange(zk, CmdSet.BASECMD, listenGetChild, false);
		clientWathcer.setGetChild(getChild);
	}

	@Override
	public void run() {
		// synchronized (this) {
		int times = 0;
		while (true) {

			clientWathcer.waitForEnd();

			ZookeeperFactory.close();

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
				// 删除历史任务,重新内部启动
				clientWathcer.removeChild(getChild);
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
}
