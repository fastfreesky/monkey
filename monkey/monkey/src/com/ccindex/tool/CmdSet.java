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
import com.ccindex.record.RetryFailedEvent;
import com.ccindex.warn.SendMail;

public class CmdSet {

	public static boolean flagExistSet = false;

	private static final String SET = "set";
	private static final String GET = "get";
	private static final String LS = "ls";
	public static final String QUIT = "quit";
	// 新建cmd时候的默认cmd
	public static final String CMDNEW = "/cmd/cmd_";
	// create [-e] path data
	private static final String CREATE = "create";
	// private final String CLOSE = "close";
	private static final String DELETE = "delete";
	private static final String HELP = "help";
	// {(monkey\\s+.*)}
	private static Pattern cmdModelSet = Pattern
			.compile("^set\\s+\\{(monkey\\s+.*)\\}");

	private static Pattern cmdModelget = Pattern.compile("^get\\s+(/\\S*)\\s*");

	private static Pattern cmdModelLs = Pattern.compile("ls\\s+(/\\S*)\\s*");

	// private Pattern cmdModelQuit = Pattern.compile("quit");
	private static Pattern cmdModelCreate = Pattern
			.compile("create\\s+(/\\S*)\\s+\\{(.*)\\}");
	// 创建临时路径
	private static Pattern cmdModelCreateE = Pattern
			.compile("create\\s+-e\\s+(/\\S*)\\s+\\{(.*)\\}");

	// private Pattern cmdModeClose = Pattern.compile("close");
	private static Pattern cmdModeDelete = Pattern
			.compile("delete\\s+(/\\S*)\\s*");

	// 所有命令后追加结束命令
	public static final String cmdEnd = "monkey  -c\"End\"";

	public static String getFinalCmd(String src) {
		return src;
		// return src + ":: " + cmdEnd;
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
		} else if (line.startsWith(GET)) {
			setFlagExistSet(false);
			String ret = dialGet(line, zk);
			if (ret == null) {
				return false;
			} else {
				callback.add(ret);
			}
		} else if (line.startsWith(LS)) {
			setFlagExistSet(false);
			return dialLs(line, zk, callback);
		} else if (line.startsWith(CREATE)) {
			setFlagExistSet(false);
			return dialCreate(line, zk);
		} else if (line.startsWith(DELETE)) {
			setFlagExistSet(false);
			return dialDelete(line, zk);
		} else if (line.startsWith(HELP)) {
			setFlagExistSet(false);
			dialHelp();
		} else {
			setFlagExistSet(false);
			System.out.println("Error Cmd, Please input help");
		}

		return true;
	}

	// 存储序列数据集合
	private static ArrayList<CmdPackage> arrCmd = new ArrayList<CmdPackage>();
	private static int sequenceNum = 0;

	public static List<String> hostNameTask = new ArrayList<String>();
	public static int total = 1;
	public static int less = 1;
	public static int timeout = 0;

	public static CmdPackage getCmdPackage() {

		synchronized (arrCmd) {
			int size = arrCmd.size();

			return (size > sequenceNum) ? arrCmd.get(sequenceNum++) : null;
		}
	}

	/**
	 * 
	 * @Title: isCmd
	 * @Description: TODO(这里用一句话描述这个方法的作用)判定是否为合法的命令,主要判断可执行的设备是否在运行
	 * @param cmd
	 * @return String
	 * @throws
	 */
	public static boolean isCmd(ZooKeeper zk) {
		CmdPackage cmdPackage = getCmdPackage();
		if (cmdPackage == null) {
			System.out.println("No cmd to set");
			return false;
		}

		int total = cmdPackage.getTotal();
		int less = cmdPackage.getLess();
		int timeout = cmdPackage.getTimeout();

		List<String> hostName = null;
		// 获取当前在线的hostname
		try {
			hostName = zk.getChildren("/hostname", false);
		} catch (KeeperException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// 在线机器不足设置参数
		if (hostName.size() < total) {
			System.out.println("Error hostName nums set [" + total
					+ "] Now num is [" + hostName.size() + "]");
			SendMail.packageMail(SendMail.getTitle(),
					"Error hostName nums set [" + total + "] Now num is ["
							+ hostName.size() + "]");
			return false;
		}

		// 获取指令,检查是否有--only,如果包含,后面跟的为文件类型,则进行解析
		String data = cmdPackage.getCmd();

		Matcher match = CmdParse.pattSpecial.matcher(data);
		if (match.find()) {
			String hostList = match.group(1);
			File flie = new File(hostList);
			if (flie.exists() && flie.isFile()) {
				try {
					BufferedReader br = new BufferedReader(new FileReader(flie));
					String line;
					StringBuffer buf = new StringBuffer();
					while ((line = br.readLine()) != null) {
						buf.append(line.trim() + ";");
						// 判定only指定的机器,是否也在运行中,不在,则退出
						if (hostName.contains(line.trim())) {
							continue;
						} else {
							System.out.println("HostName [" + line
									+ "] is not work now");

							SendMail.packageMail(SendMail.getTitle(),
									"HostName [" + line + "] is not work now");
							return false;
						}
					}

					return true;
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				String[] host = hostList.split(";");
				for (String hn : host) {
					if (hostName.contains(hn.trim())) {
						continue;
					} else {
						System.out.println("HostName [" + hn
								+ "] is not work now");

						SendMail.packageMail(SendMail.getTitle(), "HostName ["
								+ hn + "] is not work now");
						return false;
					}
				}

				return true;

			}
		}

		return true;
	}

	// static Timer timer = null;

	public static synchronized boolean setCmdPackage(ZooKeeper zk, String cmd) {
		CmdPackage cmdPackage = getCmdPackage();
		if (cmdPackage == null) {
			System.out.println("No cmd to set");
			setFlagExistSet(true);
			return false;
		}

		total = cmdPackage.getTotal();
		less = cmdPackage.getLess();
		timeout = cmdPackage.getTimeout();

		List<String> hostName = null;
		// 获取当前在线的hostname
		try {
			hostName = zk.getChildren("/hostname", false);
			hostNameTask.clear();
			hostNameTask.addAll(hostName);
		} catch (KeeperException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// 在线机器不足设置参数
		if (hostName.size() < total) {
			System.out.println("Error hostName nums set [" + total
					+ "] Now num is [" + hostName.size() + "]");
			SendMail.packageMail(SendMail.getTitle(),
					"Error hostName nums set [" + total + "] Now num is ["
							+ hostName.size() + "]");
			return false;
		}

		// 获取指令,检查是否有--only,如果包含,后面跟的为文件类型,则进行解析
		String data = cmdPackage.getCmd();

		Matcher match = CmdParse.pattSpecial.matcher(data);
		if (match.find()) {
			String hostList = match.group(1);
			File flie = new File(hostList);
			hostNameTask.clear();
			if (flie.exists() && flie.isFile()) {
				try {
					hostNameTask.clear();
					BufferedReader br = new BufferedReader(new FileReader(flie));
					String line;
					StringBuffer buf = new StringBuffer();
					while ((line = br.readLine()) != null) {
						buf.append(line.trim() + ";");

						hostNameTask.add(line);
						// 判定only指定的机器,是否也在运行中,不在,则退出
						if (hostName.contains(line.trim())) {
							continue;
						} else {
							System.out.println("HostName [" + line
									+ "] is not work now");

							SendMail.packageMail(SendMail.getTitle(),
									"HostName [" + line + "] is not work now");
							return false;
						}
					}
					String value = buf.toString();
					data = data.replaceAll(
							hostList.replaceAll("\\\\", "\\\\\\\\"), value);

					br.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				String[] host = hostList.split(";");
				for (String hn : host) {
					hostNameTask.add(hn);
					if (hostName.contains(hn.trim())) {
						continue;
					} else {
						System.out.println("HostName [" + hn
								+ "] is not work now");

						SendMail.packageMail(SendMail.getTitle(), "HostName ["
								+ hn + "] is not work now");
						return false;
					}
				}

			}
		}

		RetryFailedEvent.setHostNameTaskAll(hostNameTask);

		try {
			zk.setData(cmd, data.getBytes(Charset.forName("UTF8")), -1);
			System.out.println("Set node [" + cmd + " ] cmd [" + data + "]OK");

			RecordToFile.record("Set node [" + cmd + " ] cmd [" + data + "]OK");

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

	/**
	 * 
	 * @Title: isUsefulCmd
	 * @Description: TODO(这里用一句话描述这个方法的作用)判断是否为可用的命令
	 * @param line
	 *            -传入的命令,格式为set\\s+\\{(monkey\\s+.*)\\}
	 * @param zk
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 *             boolean
	 * @throws
	 */
	public static boolean isUsefulCmd(String line, ZooKeeper zk)
			throws KeeperException, InterruptedException {
		Matcher match = cmdModelSet.matcher(line);
		if (match.find()) {
			String data = match.group(1).trim();
			// 对设置结果进行判断分析,摘取执行的命令
			arrCmd.clear();
			sequenceNum = 0;// 初始化,重新初始执行顺序
			for (String da : data.split("::")) {
				CmdPackage cmdPackage = new CmdPackage(da);
				arrCmd.add(cmdPackage);
			}
			return isCmd(zk);

		} else {
			System.out.println("Error set cmd: [eg]-set {data}");
			return false;
		}
	}

	private static boolean dialSet(String line, ZooKeeper zk, String cmd,
			String result) throws KeeperException, InterruptedException {
		Matcher match = cmdModelSet.matcher(line);
		if (match.find()) {
			String data = match.group(1).trim();
			// 对设置结果进行判断分析,摘取执行的命令
			arrCmd.clear();
			sequenceNum = 0;// 初始化,重新初始执行顺序
			for (String da : data.split("::")) {
				CmdPackage cmdPackage = new CmdPackage(da);
				arrCmd.add(cmdPackage);
			}
			return setCmdPackage(zk, cmd);

		} else {
			setFlagExistSet(false);
			System.out.println("Error set cmd: [eg]-set {data}");
			return false;
		}
	}

	public static synchronized void setFlagExistSet(boolean flagExistSet) {

		CmdSet.flagExistSet = flagExistSet;
	}

	private static String dialGet(String line, ZooKeeper zk)
			throws KeeperException, InterruptedException {

		Matcher match = cmdModelget.matcher(line);
		if (match.find()) {
			String path = match.group(1);
			if (null == zk.exists(path, false)) {
				System.out.println("Error get cmd: not exists path:" + path);
				return null;
			} else {
				byte[] bytes = zk.getData(path, false, null);
				return new String(bytes);
			}

		} else {
			System.out.println("Error get cmd: [eg]-get path");
		}
		return null;
	}

	private static boolean dialLs(String line, ZooKeeper zk,
			ArrayList<String> callback) throws KeeperException,
			InterruptedException {
		Matcher match = cmdModelLs.matcher(line);
		if (match.find()) {
			String path = match.group(1);
			if (null == zk.exists(path, false)) {
				System.out.println("Error ls cmd: not exists path:" + path);
				return false;

			} else {
				List<String> bytes = zk.getChildren(path, false);
				callback.addAll(bytes);
				return true;
			}

		} else {
			System.out.println("Error ls cmd: [eg]-ls path");
		}
		return false;
	}

	private static boolean dialCreate(String line, ZooKeeper zk)
			throws KeeperException, InterruptedException {
		Matcher match = cmdModelCreate.matcher(line);
		if (match.find()) {
			String path = match.group(1);
			String data = match.group(2).trim();
			if (data.length() <= 0) {
				System.out.println("Error create cmd: data must has values");
				return false;
			}
			if (null == zk.exists(path, false)) {
				zk.create(path, data.getBytes(Charset.forName("UTF8")),
						Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				return true;
			} else {
				System.out.println("Error create cmd: exists path:" + path);
				return false;
			}

		}

		Matcher matchE = cmdModelCreateE.matcher(line);
		if (matchE.find()) {
			String path = matchE.group(1);
			String data = matchE.group(2).trim();
			if (data.length() <= 0) {
				System.out.println("Error create cmd: data must has values");
				return false;
			}
			if (null == zk.exists(path, false)) {
				zk.create(path, data.getBytes(Charset.forName("UTF8")),
						Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
				return true;
			} else {
				System.out.println("Error create cmd: exists path:" + path);
				return false;
			}
		} else {
			System.out
					.println("Error create cmd: [eg]-create [-e] path {data} ");
		}
		return false;
	}

	private static boolean dialDelete(String line, ZooKeeper zk)
			throws KeeperException, InterruptedException {
		Matcher match = cmdModeDelete.matcher(line);
		if (match.find()) {
			String path = match.group(1);
			if (null == zk.exists(path, false)) {
				System.out.println("Error delete cmd: not exists path:" + path);
				return false;
			} else {
				zk.delete(path, -1);
				return true;
			}

		} else {
			System.out.println("Error delete cmd: [eg]-delete path");
		}
		return false;
	}

	private static void dialHelp() {

		File file = new File("monkeyReduce");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));

			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}