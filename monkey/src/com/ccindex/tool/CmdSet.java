package com.ccindex.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

import com.ccindex.record.RecordToFile;
import com.ccindex.warn.SendMail;

public class CmdSet {

	public static boolean flagExistSet = false;

	private static final String SET = "set";
	// 新建cmd时候的默认cmd
	public static final String CMDNEW = "/cmd/cmd_";

	// {(monkey\\s+.*)}
	private static Pattern cmdModelSet = Pattern
			.compile("^set\\s+\\{(monkey\\s+.*)\\}");

	public static String getFinalCmd(String src) {
		return src;
	}

	public static boolean dialDelete(ZooKeeper zk, String path) {
		try {
			if (path == null)
				return true;
			List<String> list = zk.getChildren(path, false);
			for (String li : list) {
				String deletePath = path + "/" + li;
				zk.delete(deletePath, -1);
			}
			zk.delete(path, -1);
			zk.close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public static boolean parseLine(String line, ZooKeeper zk, String cmd,
			String result, ArrayList<String> callback) throws KeeperException,
			InterruptedException {
		if (line.startsWith(SET)) {
			return dialSet(line, zk, cmd, result);
		} else {
			setFlagExistSet(false);
			System.out.println("Error Cmd, Please input help");
		}

		return true;
	}

	private static synchronized boolean setCmdPackage(ZooKeeper zk, String cmd,
			String values) {

		try {
			zk.setData(cmd, values.getBytes(Charset.forName("UTF8")), -1);
			System.out
					.println("Set node [" + cmd + " ] cmd [" + values + "]OK");

			RecordToFile.record("Set node [" + cmd + " ] cmd [" + values
					+ "]OK");

			Thread.sleep(3000);
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setFlagExistSet(true);

		return true;
	}

	private static boolean dialSet(String line, ZooKeeper zk, String cmd,
			String result) throws KeeperException, InterruptedException {
		Matcher match = cmdModelSet.matcher(line);
		if (match.find()) {
			String data = match.group(1).trim();
			// 对设置结果进行判断分析,摘取执行的命令
			return setCmdPackage(zk, cmd, data);

		} else {
			setFlagExistSet(false);
			System.out.println("Error set cmd: [eg]-set {data}");
			return false;
		}
	}

	public static synchronized void setFlagExistSet(boolean flagExistSet) {
		CmdSet.flagExistSet = flagExistSet;
	}
}