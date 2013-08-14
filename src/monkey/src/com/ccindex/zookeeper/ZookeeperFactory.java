package com.ccindex.zookeeper;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

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

	/**
	 * 
	 * @Title: getData
	 * @Description: TODO(这里用一句话描述这个方法的作用)获取指定路径的数据,不进行观察
	 * @param path
	 * @return String
	 * @throws
	 */
	public static String getData(String path) {

		ZooKeeper zk = getZookeeper();
		if (zk == null) {
			return null;
		}
		byte[] bytes = null;
		try {
			bytes = zk.getData(path, false, null);
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MonkeyOut.debug(ZookeeperFactory.class, "Get Data OK: "
				+ new String(bytes));

		return new String(bytes);
	}

	/**
	 * 
	 * @Title: setData
	 * @Description: TODO(这里用一句话描述这个方法的作用)设置路径对应的值
	 * @param path
	 * @param value
	 *            void
	 * @throws
	 */
	public static void setData(String path, String value) {
		ZooKeeper zk = getZookeeper();
		if (zk == null) {
			MonkeyOut.debug(ZookeeperFactory.class, "Set Data Error: " + value);
			return;
		} else {
			try {
				zk.setData(path, value.getBytes(), -1);
			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		MonkeyOut.debug(ZookeeperFactory.class, "Set Data OK: " + value);
	}

	public static boolean exists(String path) {
		while (true) {
			ZooKeeper zk = getZookeeper();
			States s = zk.getState();
			if (!s.isConnected()) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				MonkeyOut.debug(ZookeeperFactory.class, "Error connect zk : "
						+ s);
				continue;
			}

			Stat stat = null;
			try {
				stat = zk.exists(path, false);
			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (stat != null) {
				break;
			} else {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				MonkeyOut.debug(ZookeeperFactory.class, "Retry to check path :"
						+ path);
			}
		}

		return true;
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

	public static String create(String path, byte[] data, List<ACL> acl,
			CreateMode createMode) {
		ZooKeeper zk = getZookeeper();
		if (zk == null) {
			MonkeyOut.debug(ZookeeperFactory.class, "Create Path: " + path);
			return null;
		} else {
			try {
				return zk.create(path, data, acl, createMode);
			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
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
