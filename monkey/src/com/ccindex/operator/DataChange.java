package com.ccindex.operator;

import java.util.Arrays;

import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.ccindex.listener.MonkeyListener;
import com.ccindex.warn.MonkeyOut;

/**
 * 
 * @ClassName: GetDataOperator
 * @Description: TODO(这里用一句话描述这个类的作用)getData的处理类
 * @author tianyu.yang
 * @date 2013-3-14 下午2:17:46
 * 
 */
public class DataChange implements StatCallback, Watcher {
	// 判定是否为第一次获取启动设备
	private static int flagFirstTime = 0;
	private ZooKeeper zk;
	private String znode;
	private Watcher watcher;
	private MonkeyListener<byte[]> listener;
	public volatile boolean flagEnd = false;
	private byte[] prevData;

	/**
	 * 
	 * @Title: GetDataOperator.java
	 * @Description: 获取数据的方法
	 * @param zk
	 *            传入的zookeeper
	 * @param znode
	 *            待获取值的路径
	 * @param watcher
	 *            需要观察的观察者,(暂时不使用)
	 * @param listener
	 *            监听接口
	 */
	public DataChange(ZooKeeper zk, String znode, Watcher watcher,
			MonkeyListener<byte[]> listener) {
		this.zk = zk;
		this.znode = znode;
		this.watcher = watcher;
		this.listener = listener;
		this.zk.exists(znode, true, this, null);
		setPath(znode);
		// this.zk.getData(this.znode, true, this, null);
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

	@Override
	public void process(WatchedEvent event) {
		// TODO Auto-generated method stub
		// 待处理的事件类型,放到主逻辑处理,此处已经判定为数据变化event
		MonkeyOut.debug(getClass(), "process" + event);
		String path = event.getPath();
		// wathcer检测的信号类型
		if (event.getType() == Event.EventType.None) {
			// We are are being told that the state of the
			// connection has changed
			switch (event.getState()) {
			case SyncConnected:
				// In this particular example we don't need to do anything
				// here - watches are automatically re-registered with
				// server and any watches triggered while the client was
				// disconnected will be delivered (in order of course)
				break;
			case Expired:
				// It's all over
				flagEnd = true;
				break;
			}
		} else {
			if (path != null && path.equals(znode)) {
				// 监听的路径数据发生变化,可能删除,可能被创建,可能新增
				zk.exists(znode, true, this, null);
			}
		}
		// if (watcher != null) {
		// watcher.process(event);
		// }
	}

	@SuppressWarnings("deprecation")
	@Override
	public void processResult(int rc, String path, Object ctx, Stat stat) {
		// TODO Auto-generated method stub
		// 处理侦查到的数据变化情况
		MonkeyOut.debug(getClass(), path);
		boolean exists;
		switch (rc) {
		case Code.Ok:// 一切正常
			exists = true;
			flagFirstTime++;
			break;
		case Code.NoNode:// 查询路径不存在
			exists = false;
			break;
		case Code.SessionExpired:// 服务器意外问题
		case Code.NoAuth:// 未认证的
			flagEnd = true;
			return;
		default:// 继续检测
			// Retry errors
			this.zk.exists(znode, true, this, null);
			return;
		}

		byte b[] = null;
		// 返回正常
		if (exists) {
			try {
				// 获取数据成功,外部对数据进行处理
				b = zk.getData(znode, false, null);
			} catch (KeeperException e) {
				// We don't need to worry about recovering now. The watch
				// callbacks will kick off any exception handling
				e.printStackTrace();
			} catch (InterruptedException e) {
				return;
			}
		}
		// 修改,删除,创建,都会触发处理数据操作(b==null时候,为删除操作)
		// (b == null && b != prevData && flagFirstTime != 1)
		if ((b != null && !Arrays.equals(prevData, b)) && flagFirstTime != 1) {
			listener.exists(b);
			prevData = b;
		}
	}

}
