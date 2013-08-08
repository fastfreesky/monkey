package com.ccindex.zookeeper;

import java.io.IOException;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.ccindex.warn.MonkeyOut;
import com.ccindex.watcher.WatcherImpl;

/**
 * 
 * @ClassName: ZookeeperFactory
 * @Description: TODO(这里用一句话描述这个类的作用)关于zookeeper的一些集合信息
 * @author tianyu.yang
 * @date 2013-4-15 下午12:00:49
 * 
 */
public class ZookeeperFactory {

	// 连接的zk
	private static volatile ZooKeeper zookeeper;
	private static volatile String connectString;
	private static volatile int sessionTimeout;
	private static volatile WatcherImpl watcher;

	public static void init(String connectString, int sessionTimeout,
			WatcherImpl watcher) {
		ZookeeperFactory.connectString = connectString;
		ZookeeperFactory.sessionTimeout = sessionTimeout;
		ZookeeperFactory.watcher = watcher;
	}

	public static ZooKeeper getZookeeper() {

		if (zookeeper == null || !watcher.isConnect()) {

			MonkeyOut.debug(ZookeeperFactory.class, "Error zookeeper");
			try {
				zookeeper = new ZooKeeper(connectString, sessionTimeout,
						watcher);

				watcher.waitForConnect();

				MonkeyOut.info(ZookeeperFactory.class, "Again new zookeeper");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return zookeeper;
	}

	public static void close() {
		try {
			MonkeyOut.info(ZookeeperFactory.class, "Close zookeeper");
			zookeeper.close();
			zookeeper = null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
