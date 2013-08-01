package com.ccindex.watcher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.ZooDefs.Ids;

import com.ccindex.constant.Constant;
import com.ccindex.constant.Debug;
import com.ccindex.main.Client;
import com.ccindex.operator.ChildrenChange;
import com.ccindex.operator.DataChange;
import com.ccindex.record.RegisterErrorRecordToServer;
import com.ccindex.zookeeper.MonkeyClient;

/**
 * 
 * @ClassName: MonkeyClientWatcher
 * @Description: TODO(这里用一句话描述这个类的作用)监听集结处
 * @author tianyu.yang
 * @date 2013-4-13 上午9:21:39
 * 
 */
public class MonkeyClientWatcher implements Watcher {

	// 程序是否完成
	public volatile boolean flagEnd = false;

	// zk是否建立成功标志
	public volatile boolean flagSucceedConnect = false;

	private ZooKeeper zk = null;
	// 监测命令的变化
	private ChildrenChange getChild;
	// 获取指定的cmd中的值
	private DataChange getCmdData;

	@Override
	public void process(WatchedEvent event) {
		// TODO Auto-generated method stub
		Debug.debug(getClass(), "Input event " + event);

		// wathcer检测的信号类型,无路径处理
		if (event.getType() == Event.EventType.None) {
			// We are are being told that the state of the
			// connection has changed
			switch (event.getState()) {
			case SyncConnected:
				// In this particular example we don't need to do anything
				// here - watches are automatically re-registered with
				// server and any watches triggered while the client was
				// disconnected will be delivered (in order of course)
				Debug.info(getClass(), "Connect...Ok");
				RegisterErrorRecordToServer.setErrorRecord(zk);
				flagSucceedConnect = true;
				break;
			case Disconnected:
			case Expired:
				Debug.info(getClass(), "Crashed...");
				// It's all over
				try {
					zk.close();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				flagSucceedConnect = false;
				flagEnd = true;
				return;
			}
		}

		// 成功连接后,再进行处理
		if (flagSucceedConnect) {
			// 注册在线设备
			registHost();

			// 判断此次变化是否为子节点变化
			if (getChild != null && getChild.isPath(event.getPath()))
				getChild.process(event);

			// 判断此次变化是否为数据变化
			if (getCmdData != null && getCmdData.isPath(event.getPath()))
				getCmdData.process(event);
		} else {

			// 宕机处理
			flagEnd = true;
		}

	}

	private void registHost() {
		try {
			DateFormat fm = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date start = new Date(System.currentTimeMillis());
			String day = fm.format(start);
			String path = "/hostname/" + Constant.getHostname();

			List<String> hostList = zk.getChildren("/hostname", false);
			if (!hostList.contains(Constant.getHostname())) {
				Debug.debug(getClass(),
						"Again register HostName " + Constant.getHostname());
				zk.create(path, day.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL);
			}
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setGetCmdData(DataChange getCmdData) {
		this.getCmdData = getCmdData;
	}

	public void setZk(ZooKeeper zk) {
		this.zk = zk;
	}

	public void setGetChild(ChildrenChange getChild) {
		this.getChild = getChild;
	}

}
