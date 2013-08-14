package com.ccindex.watcher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

import com.ccindex.constant.Constant;
import com.ccindex.operator.ChildrenChange;
import com.ccindex.operator.DataChange;
import com.ccindex.record.RegisterErrorRecordToServer;
import com.ccindex.tool.CmdSet;
import com.ccindex.warn.MonkeyOut;
import com.ccindex.zookeeper.ZookeeperFactory;

/**
 * 
 * @ClassName: MonkeyClientWatcher
 * @Description: TODO(这里用一句话描述这个类的作用)监听集结处
 * @author tianyu.yang
 * @date 2013-4-13 上午9:21:39
 * 
 */
public class MonkeyClientWatcher extends WatcherImpl {

	// 监测命令的变化
	// private ChildrenChange getChild;
	private ArrayList<ChildrenChange> getChild = new ArrayList<ChildrenChange>();
	// 获取指定的cmd中的值
	// private DataChange getCmdData;
	// 监听cmd内部值变化,用数组,存储多个执行命令,否则,只执行最后一个命令
	private ArrayList<DataChange> getCmdData = new ArrayList<DataChange>();

	@Override
	public void dialConnectOkEvent() {
		// TODO Auto-generated method stub
		RegisterErrorRecordToServer.setErrorRecord(ZookeeperFactory
				.getZookeeper());
	}

	@Override
	public boolean dialProcess(WatchedEvent event) {
		// TODO Auto-generated method stub
		// 成功连接后,再进行处理
		if (isConnect()) {
			// 注册在线设备
			registHost();

			if (getChild.size() != 0) {
				for (ChildrenChange data : getChild) {
					if (data.isPath(event.getPath())) {
						data.process(event);
					}
				}
			}
			// 判断此次变化是否为数据变化
			if (getCmdData.size() != 0) {
				for (DataChange data : getCmdData) {
					if (data.isPath(event.getPath())) {
						data.process(event);
					}
				}
			}

		}

		return true;
	}

	private void registHost() {
		try {
			DateFormat fm = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date start = new Date(System.currentTimeMillis());
			String day = fm.format(start);
			String path = CmdSet.packagePath(CmdSet.BASEHOSTNAME,
					Constant.getHostname());

			ZooKeeper zk = ZookeeperFactory.getZookeeper();
			List<String> hostList = zk.getChildren(CmdSet.BASEHOSTNAME, false);
			if (!hostList.contains(Constant.getHostname())) {
				MonkeyOut.debug(getClass(), "Again register HostName "
						+ Constant.getHostname());
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
		this.getCmdData.add(getCmdData);
		// this.getCmdData = getCmdData;
		MonkeyOut.debug(getClass(), "add cmd data " + getCmdData.getPath());
	}

	public void removeCmdData(DataChange getCmdData) {
		this.getCmdData.remove(getCmdData);
		MonkeyOut.debug(getClass(), "remove " + getCmdData.getPath());
	}

	public void setGetChild(ChildrenChange getChild) {
		this.getChild.add(getChild);
		MonkeyOut.debug(getClass(), "add child " + getChild.getPath());
	}

	public void removeChild(ChildrenChange getChild) {
		this.getChild.remove(getChild);
		MonkeyOut.debug(getClass(), "remove " + getChild.getPath());
	}

}
