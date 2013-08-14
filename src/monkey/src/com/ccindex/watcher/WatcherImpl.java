package com.ccindex.watcher;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import com.ccindex.warn.MonkeyOut;

public class WatcherImpl implements Watcher {

	protected volatile boolean isConnect = false;

	public boolean isConnect() {
		return isConnect;
	}

	protected void setConnect(boolean isConnect) {
		synchronized (this) {
			if (isConnect == false) {
				MonkeyOut.info(getClass(), "Notify ...");
				notifyAll();
			}
			this.isConnect = isConnect;
		}

		// this.isConnect = isConnect;
	}

	public boolean waitForEnd() {
		while (isConnect()) {
			try {
				MonkeyOut.info(getClass(), "Waiting For End...");
				synchronized (this) {
					wait();
				}
				MonkeyOut.info(getClass(), "End Ok...");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// while (isConnect()) {
		// try {
		// Thread.sleep(10000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

		return true;
	}

	public boolean waitForConnect() {

		while (!isConnect()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * 
	 * @Title: dialOtherSigProcess
	 * @Description: TODO(这里用一句话描述这个方法的作用)处理除过建立连接及各种意外断开之后的其他信号类型
	 * @param event
	 * @return boolean
	 * @throws
	 */
	public boolean dialOtherSigProcess(WatchedEvent event) {
		return true;
	};

	/**
	 * 
	 * @Title: dialProcess
	 * @Description: TODO(这里用一句话描述这个方法的作用)处理所有的信号类型
	 * @param event
	 * @return boolean
	 * @throws
	 */
	public boolean dialProcess(WatchedEvent event) {
		return true;
	};

	/**
	 * 
	 * @Title: dialConnectOkEvent
	 * @Description: TODO(这里用一句话描述这个方法的作用)处理建立成功后的方法 void
	 * @throws
	 */
	public void dialConnectOkEvent() {
	};

	/**
	 * 
	 * @Title: dialConnectNotOkEvent
	 * @Description: TODO(这里用一句话描述这个方法的作用)处理中断连接或意外中断的方法 void
	 * @throws
	 */
	public void dialConnectNotOkEvent() {
	};

	@Override
	public void process(WatchedEvent event) {
		MonkeyOut.debug(getClass(), "process event " + event);

		// wathcer检测的信号类型,无路径处理
		if (event.getType() == Event.EventType.None) {
			// We are are being told that the state of the
			// connection has changed
			switch (event.getState()) {
			case SyncConnected:
				MonkeyOut.info(getClass(), "Connect...Ok");
				setConnect(true);
				dialConnectOkEvent();
				break;
			case Disconnected:
			case Expired:
				MonkeyOut.info(getClass(), "Crashed...");
				setConnect(false);
				dialConnectNotOkEvent();
				break;
			default:
				setConnect(false);
				break;
			}
		} else {
			dialOtherSigProcess(event);
		}

		dialProcess(event);
	}
}
