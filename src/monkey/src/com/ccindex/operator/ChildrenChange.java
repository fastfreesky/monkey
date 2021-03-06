package com.ccindex.operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException.Code;

import com.ccindex.interfaceI.MonkeyListenerI;
import com.ccindex.warn.MonkeyOut;
import com.ccindex.watcher.WatcherImpl;

public class ChildrenChange extends WatcherImpl implements ChildrenCallback {

	// 判定是否为第一次获取启动设备
	private static int flagFirstTime = 0;
	private ZooKeeper zk;
	private String znode;
	private MonkeyListenerI<List<String>> listener;

	private boolean isIgnoreFirstTime = false;

	public void setIgnoreFirstTime(boolean isIgnoreFirstTime) {
		this.isIgnoreFirstTime = isIgnoreFirstTime;
	}

	// 此次任务监控的路径
	private String path;

	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * 
	 * @Title: isPath
	 * @Description: TODO(这里用一句话描述这个方法的作用)判断此次变化的路径是否为监控的路径
	 * @param path
	 * @return boolean
	 * @throws
	 */
	public boolean isPath(String path) {
		if (this.path != null && path != null && this.path.equals(path)) {
			return true;
		}

		return false;
	}

	ArrayList<String> childOld = new ArrayList<String>();

	public ChildrenChange(ZooKeeper zk, String znode,
			MonkeyListenerI<List<String>> listener, boolean ignore) {

		this.zk = zk;
		this.znode = znode;
		this.listener = listener;
		setPath(znode);
		setIgnoreFirstTime(ignore);
		zk.getChildren(znode, true, this, null);

	}

	public String getPath() {
		return path;
	}

	@Override
	public void processResult(int rc, String path, Object ctx,
			List<String> children) {
		// TODO Auto-generated method stub
		Collections.sort(children);
		MonkeyOut.debug(getClass(), "processResult:[" + flagFirstTime + "]"
				+ children);
		// System.out.println("processResult:" + );
		boolean exists;
		switch (rc) {
		case Code.Ok:// 一切正常
			flagFirstTime++;
			exists = true;
			break;
		case Code.NoNode:// 查询路径不存在
			exists = false;
			break;
		case Code.SessionExpired:// 服务器意外问题
		case Code.NoAuth:// 未认证的
			return;
		default:// 继续检测
			// Retry errors
			zk.getChildren(znode, true, this, null);
			return;
		}
		// 第一次运行,若有在运行指令,则重启之后,需要手动执行,暂不支持自动执行
		if (exists && flagFirstTime == 1 && children.size() != 0) {
			childOld.addAll(children);
		}
		// 忽略第一次屏蔽作用
		if (isIgnoreFirstTime) {
			if (exists && children.size() != 0) {
				// 对列表进行判断,若无变化,则不进行处理,如果有新增,则调用处理方法,将新增的cmd子程序传参给处理函数
				// 处理列表
				// 对比列表,寻找新增节点
				ArrayList<String> childRun = new ArrayList<String>();
				for (String cmd : children) {
					if (childOld.contains(cmd) && flagFirstTime > 1) {
						continue;
					} else {
						// 第一次全部运行
						childRun.add(cmd);
					}
				}
				if (childRun.size() > 0)
					listener.exists(childRun);

				childOld.clear();
				childOld.addAll(children);
			}
		} else {

			if (exists && children.size() != 0 && flagFirstTime > 1) {
				// 对列表进行判断,若无变化,则不进行处理,如果有新增,则调用处理方法,将新增的cmd子程序传参给处理函数
				// 处理列表
				// 对比列表,寻找新增节点
				ArrayList<String> childRun = new ArrayList<String>();
				for (String cmd : children) {
					if (childOld.contains(cmd)) {
						continue;
					} else {
						childRun.add(cmd);
					}
				}
				if (childRun.size() > 0)
					listener.exists(childRun);

				childOld.clear();
				childOld.addAll(children);
			}
		}

	}

	@Override
	public boolean dialOtherSigProcess(WatchedEvent event) {
		// TODO Auto-generated method stub
		String path = event.getPath();
		if (path != null && path.equals(znode)) {
			// Something has changed on the node, let's find out
			zk.getChildren(znode, true, this, null);
		}
		return true;
	}
}
