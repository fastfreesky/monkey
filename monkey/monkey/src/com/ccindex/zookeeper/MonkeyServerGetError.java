package com.ccindex.zookeeper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.ccindex.constant.Constant;
import com.ccindex.constant.Debug;
import com.ccindex.listener.MonkeyListenerForRunServerCmd;
import com.ccindex.listener.MonkeyListenerForGatherClientResult;
import com.ccindex.operator.ChildrenChange;
import com.ccindex.record.RegisterErrorRecordToServer;
import com.ccindex.tool.CmdSet;

/**
 * 
 * @ClassName: MonkeyServerGetError
 * @Description: TODO(这里用一句话描述这个类的作用)获取一天连接失败次数
 * @author tianyu.yang
 * @date 2013-5-16 下午5:20:44
 * 
 */
public class MonkeyServerGetError implements Watcher, Runnable {

	private ZooKeeper zk = null;
	// 获取的数据的路径
	private String path = null;
	// 生成文件的根路径
	private String basePath = null;
	// 传入日期
	private String date = null;
	// zk是否建立成功标志
	public volatile boolean flagSucceedConnect = false;

	/**
	 * 
	 * @Title: MonkeyServerGetError.java
	 * @Description: 设置服务器IP及端口
	 * @param hostPort
	 *            服务器的IP和端口
	 * @param znode
	 *            zookeeper的节点
	 * @throws KeeperException
	 * @throws IOException
	 */

	/**
	 * 
	 * @Title: MonkeyServerGetError.java
	 * @Description:
	 * @param hostPort
	 *            服务器的IP和端口
	 * @param date
	 *            待获取的日期
	 * @throws KeeperException
	 * @throws IOException
	 */
	public MonkeyServerGetError(String hostPort, String date, String basePath)
			throws KeeperException, IOException {
		zk = new ZooKeeper(hostPort, 20000, this);
		this.path = "/error/" + date;
		this.date = date;
		this.basePath = basePath;
	}

	/***************************************************************************
	 * We do process any events ourselves, we just need to forward them on.
	 * 
	 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.proto.WatcherEvent)
	 */
	@Override
	public void process(WatchedEvent event) {

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
				return;
			}
		}
	}

	@Override
	public void run() {
		Debug.info(getClass(), "Waiting for conning......");
		// 等待建立成功连接
		while (!flagSucceedConnect) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		StringBuilder buf = new StringBuilder();
		try {
			ArrayList<String> list = (ArrayList<String>) zk.getChildren(path,
					false);
			if (list.size() == 0) {
				System.out.println("No Error HostName");
			} else {
				// 获取每个设备中的值
				for (String l : list) {
					buf.append(l);
					buf.append("\n");
					buf.append(new String(zk.getData(path + "/" + l, false,
							null)));
					buf.append("\n");
				}

				ObjectToFile(basePath + "/" + date + ".error", buf.toString()
						.getBytes());
			}

			zk.close();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void ObjectToFile(String fileName, byte[] values) {

		File file = new File(fileName);
		FileOutputStream outWriter = null;
		try {
			outWriter = new FileOutputStream(file, true);
			outWriter.write(values);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (outWriter != null)
					outWriter.close();

				System.out
						.println("OutFile Name is :" + file.getAbsolutePath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
