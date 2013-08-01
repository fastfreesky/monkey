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
import com.ccindex.warn.SendMail;

/**
 * 
 * @ClassName: MonkeyServerGetError
 * @Description: TODO(这里用一句话描述这个类的作用)获取一天连接失败次数
 * @author tianyu.yang
 * @date 2013-5-16 下午5:20:44
 * 
 */
public class MonkeyServerGetServerState implements Watcher, Runnable {

	private ZooKeeper zk = null;
	// 生成文件的根路径
	private String inputFile = null;

	private final String path = "/hostname";
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
	public MonkeyServerGetServerState(String hostPort, String inputFile)
			throws KeeperException, IOException {
		zk = new ZooKeeper(hostPort, 20000, this);
		this.inputFile = inputFile;
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
		int count = 0;
		try {
			ArrayList<String> hostName = (ArrayList<String>) zk.getChildren(
					path, false);

			System.out.println("HostName:");
			File flie = new File(inputFile);
			if (flie.exists() && flie.isFile()) {
				try {
					BufferedReader br = new BufferedReader(new FileReader(flie));
					String line = null;
					while ((line = br.readLine()) != null) {
						// 判定only指定的机器,是否也在运行中,不在,则退出BGP-BJ-9-3m1

						if (hostName.contains(line.trim())) {
							continue;
						} else {
							System.out.println("\t" + line);
							count++;
						}
					}
					br.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				String[] host = inputFile.split(";");
				for (String hn : host) {
					if (hostName.contains(hn.trim())) {
						continue;
					} else {
						System.out.println("\t" + hn);
						count++;
					}
				}
			}

			System.out.println("Now is not work count [" + count + "]");

			zk.close();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
