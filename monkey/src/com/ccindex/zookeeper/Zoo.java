package com.ccindex.zookeeper;

import java.io.IOException;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.ccindex.constant.Debug;

/**
 * 
 * @ClassName: zoo
 * @Description: TODO(这里用一句话描述这个类的作用)关于zookeeper的一些集合信息
 * @author tianyu.yang
 * @date 2013-4-15 下午12:00:49
 * 
 */
public class Zoo {

	// 连接的zk
	private static ZooKeeper zookeeper;

	public static ZooKeeper getZookeeper() {
		return zookeeper;
	}

	public synchronized static void setZookeeper(ZooKeeper zookeeper) {
		if (zookeeper == null) {
			System.out.println("Error zookeeper");
			Debug.debug(Zoo.class, "Error zookeeper");
		} else {
			Debug.debug(Zoo.class, "Again new zookeeper");
			System.out.println("Again new zookeeper");

			Zoo.zookeeper = zookeeper;
		}
	}

}
