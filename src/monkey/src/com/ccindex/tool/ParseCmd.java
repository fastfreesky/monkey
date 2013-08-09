package com.ccindex.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.ccindex.warn.MonkeyOut;
import com.ccindex.warn.SendMail;

/**
 * 
 * @ClassName: ParseCmd
 * @Description: TODO(这里用一句话描述这个类的作用)解析输入的命令行字段
 * @author tianyu.yang
 * @date 2013-7-8 下午1:41:41
 * 
 *       任务1:判断是否为合法的命令格式
 *       任务2:解析命令行中需要的字段,主要包括--conf之后的total,less,timeout字段,还有单个命令中的设备字段,运行的命令等
 */
public class ParseCmd {

	//命令执行是否成功
	private volatile boolean isSucceed = false;
	private String inputCmd;
	private int total;
	private int less;
	private int timeout;
	// 内部重试次数,目前每5s重新查询一次可否执行的设备,一共5min
	private int retryTimesInline = 1;
	// 内部重试超时时间
	private int timeoutInline = 0;

	public int getTimeoutInline() {
		return timeoutInline;
	}

	public void setTimeoutInline(int timeoutInline) {
		this.timeoutInline = timeoutInline;
	}

	public boolean isSucceed() {
		return isSucceed;
	}

	public void setSucceed(boolean isSucceed) {
		this.isSucceed = isSucceed;
	}

	// 核心命令
	private String kernelCmd;
	// 待运行的设备字符串
	private String hostNameString;

	// 记录应当运行的全部机器
	public List<String> hostNameTaskAll = new ArrayList<String>();
	// 记录正在运行的全部机器
	public List<String> hostNameTaskRunning = new ArrayList<String>();
	// 已经完成的机器列表
	public List<String> hostNameTaskSucceed = new ArrayList<String>();
	// 运行失败的机器
	public List<String> hostNameTaskFailed = new ArrayList<String>();
	// 需要重新运行的机器列表
	public List<String> hostNameTaskNeedRetry = new ArrayList<String>();

	public List<String> getHostNameTaskFailed() {
		return hostNameTaskFailed;
	}

	public void addHostNameTaskFailed(String hostName) {
		if (!hostNameTaskFailed.contains(hostName)) {
			hostNameTaskFailed.add(hostName);
		}
	}

	public List<String> getHostNameTaskAll() {
		return hostNameTaskAll;
	}

	public List<String> getHostNameTaskRunning() {
		return hostNameTaskRunning;
	}

	public void addHostNameTaskRunning(String hostName) {
		if (!hostNameTaskRunning.contains(hostName)) {
			hostNameTaskRunning.add(hostName);
		}
	}

	public void addHostNameTaskSucceed(String hostName) {
		if (!hostNameTaskSucceed.contains(hostName)) {
			hostNameTaskSucceed.add(hostName);
		}
	}

	public List<String> getHostNameTaskSucceed() {
		return hostNameTaskSucceed;
	}

	public String getKernelCmd() {
		return kernelCmd;
	}

	/**
	 * 
	 * @Title: getHostNameTaskNeedRetry
	 * @Description: TODO(这里用一句话描述这个方法的作用)内部通过全部任务列表,完成任务列表,确定哪些尚未完成
	 * @return List<String>
	 * @throws
	 */
	public List<String> getHostNameTaskNeedRetry() {

		hostNameTaskNeedRetry.clear();

		for (String tl : hostNameTaskAll) {
			if (!hostNameTaskSucceed.contains(tl)) {
				hostNameTaskNeedRetry.add(tl);
			}
		}

		return hostNameTaskNeedRetry;
	}

	public String packageRetryTask(String kernelCmd) {

		if (hostNameTaskAll.size() == 0 || hostNameTaskSucceed.size() == 0) {
			return null;
		}
		StringBuffer buf = new StringBuffer();

		int total = getTotal() - hostNameTaskSucceed.size();
		int less = getLess() - hostNameTaskSucceed.size();
		int timeout = getTimeout();

		List<String> arr = getHostNameTaskNeedRetry();
		buf.append("monkey --only ");
		for (String n : arr) {
			buf.append(n).append(",");
		}

		buf.deleteCharAt(buf.length() - 1);
		buf.append(" -c\"");
		buf.append(kernelCmd);
		buf.append("\" --conf ");

		buf.append("total=").append(total).append(" less=").append(less)
				.append(" timeout=").append(timeout);

		return buf.toString();
	}

	public String getInputCmd() {
		return inputCmd;
	}

	public boolean isHostName(String hostname) {
		if (hostNameTaskAll.contains(hostname)) {
			return true;
		} else {
			return false;
		}
	}

	public void init() {
		this.inputCmd = null;
		this.total = 0;
		this.less = 0;
		this.timeout = 0;
		this.retryTimesInline = 1;
		this.timeoutInline = 0;
		this.kernelCmd = null;
		this.hostNameString = null;
		this.hostNameTaskAll.clear();
		this.hostNameTaskSucceed.clear();
		this.hostNameTaskNeedRetry.clear();
		this.hostNameTaskFailed.clear();
		this.hostNameTaskRunning.clear();
		this.errorReason = null;
		this.finalCmd = null;
		setSucceed(false);
	}

	public void initInputCmdServer(String inputCmd) throws IOException {

		init();

		String confString = null;
		this.inputCmd = inputCmd.trim();
		// 特殊的运行设备
		Matcher matchSpecial = pattSpecialServer.matcher(getInputCmd());
		Matcher matchSpecialDefault = pattSpecialServerDefault
				.matcher(getInputCmd());

		if (matchSpecial.find()) {
			hostNameString = matchSpecial.group(1);
			kernelCmd = matchSpecial.group(2);
			confString = matchSpecial.group(3);

		} else if (matchSpecialDefault.find()) {
			hostNameString = matchSpecialDefault.group(1);
			kernelCmd = matchSpecialDefault.group(2);

		} else {
			throw new IOException("Error monkey cmd format");
		}

		// 获取将要运行的设备名称
		if (!parseHostName(hostNameString)) {
			throw new IOException("Exists Repeat HostName");
		}

		// 解析配置文件
		if (confString != null) {
			parseConf(confString);
		} else {
			// 默认6个小时超时时间
			setTotal(hostNameTaskAll.size());
			setLess(hostNameTaskAll.size());
			setTimeout(21600);
		}

		// 设置新组建的命令
		setFinalCmd(kernelCmd);

		MonkeyOut.info(getClass(), inputCmd);
		debugParseCmd();
	}

	public void initClientCmd(String inputCmd) throws IOException {

		init();

		String confString = null;
		this.inputCmd = inputCmd;

		// 特殊的运行设备
		Matcher matchSpecial = pattSpecialClient.matcher(getInputCmd());

		if (matchSpecial.find()) {
			hostNameString = matchSpecial.group(1);
			kernelCmd = matchSpecial.group(2);
		} else {
			throw new IOException("Error monkey cmd format");
		}

		// 获取将要运行的设备名称
		if (!parseHostName(hostNameString)) {
			throw new IOException("Exists Repeat HostName");
		}
		// 获取客户端执行程序的集合
		setClientCmdList(kernelCmd);

		MonkeyOut.info(getClass(), inputCmd);
	}

	private ArrayList<String> clientCmdList = new ArrayList<String>();

	public ArrayList<String> getClientCmdList() {
		return clientCmdList;
	}

	public void setClientCmdList(String kernelCmd) {
		String s[] = kernelCmd.split("\\|\\|");

		for (String n : s) {
			clientCmdList.add(n);
		}
	}

	private boolean parseHostName(String hostName) {
		File flie = new File(hostName);
		if (flie.exists() && flie.isFile()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(flie));
				String line;
				while ((line = br.readLine()) != null) {
					if (hostNameTaskAll.contains(line)) {
						MonkeyOut.info(ParseCmd.class,
								"Already exists hostname: " + line);
						return false;
					} else {
						hostNameTaskAll.add(line);
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			String[] host = hostName.split(",");
			for (String line : host) {
				if (hostNameTaskAll.contains(line)) {
					MonkeyOut.info(ParseCmd.class, "Already exists hostname: "
							+ line);
					return false;
				} else {
					hostNameTaskAll.add(line);
				}
			}

		}

		return true;
	}

	private void parseConf(String conf) {

		String[] confs = conf.split("\\s");
		for (String con : confs) {
			String c = con.trim();
			if (c.startsWith("total=")) {
				setTotal(Integer.parseInt(c.substring(6)));
			} else if (c.startsWith("less=")) {
				setLess(Integer.parseInt(c.substring(5)));
			} else if (c.startsWith("timeout=")) {
				setTimeout(Integer.parseInt(c.substring(8)));
			}
		}
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getLess() {
		return less;
	}

	public void setLess(int less) {
		this.less = less;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void debugParseCmd() {
		String value = "HostName List: [" + hostNameTaskAll + "], kernelCmd:  "
				+ kernelCmd + " , total:  " + total + " , less: " + less
				+ "  timeout: " + timeout + " ";

		MonkeyOut.info(getClass(), value);
	}

	public static List<String> getZkHostNameList(ZooKeeper zk) {

		List<String> hostnameList = null;
		try {
			hostnameList = zk.getChildren("/hostname", false);
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return hostnameList;
	}

	private String errorReason;

	public String getErrorReason() {
		return errorReason;
	}

	public void setErrorReason(String errorReason) {
		this.errorReason = errorReason;
	}

	/**
	 * 
	 * @Title: isRunningCmd
	 * @Description: TODO(这里用一句话描述这个方法的作用)判定当前环境是否可执行命令
	 * @param zk
	 * @param retrytimes
	 * @return boolean
	 * @throws
	 */
	public boolean isRunningCmd(ZooKeeper zk) {

		List<String> hostnameNotOk = new ArrayList<String>();

		for (int i = 0; i < retryTimesInline; ++i) {
			List<String> hostnameList = getZkHostNameList(zk);
			if (hostnameList == null
					|| hostnameList.size() < hostNameTaskAll.size()) {
				try {
					Thread.sleep(timeoutInline);
					setErrorReason("Retry times...[5s] : " + i
							+ "Work HostName is not enough");

					MonkeyOut.info(getClass(), getErrorReason());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}

			hostnameNotOk.clear();
			for (String s : hostNameTaskAll) {
				if (!hostnameList.contains(s)) {
					hostnameNotOk.add(s);
				}
			}

			if (hostnameNotOk.size() > 0) {
				try {
					Thread.sleep(timeoutInline);
					setErrorReason("Retry times...[5s] : " + i
							+ " Not Working: " + hostnameNotOk);
					MonkeyOut.info(getClass(), getErrorReason());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}

			return true;
		}
		return false;
	}

	private String finalCmd;

	public String getFinalCmd() {
		return finalCmd;
	}

	public void setFinalCmd(String finalCmd) {

		StringBuffer buf = new StringBuffer();
		buf.append("monkey --only ");
		for (String n : hostNameTaskAll) {
			buf.append(n).append(",");
		}

		buf.deleteCharAt(buf.length() - 1);
		buf.append(" -c\"");

		// File file = new File(finalCmd);
		// if (file.exists() && file.isFile()) {
		// buf.append(ReadFromFile.readFileByLines(finalCmd));
		// } else {
		// buf.append(finalCmd);
		// }
		// 暂不支持脚本形式的命令
		buf.append(finalCmd);
		buf.append("\"");

		this.finalCmd = buf.toString();

	}

	public void setRetryTimesInline(int retryTimesInline) {
		this.retryTimesInline = retryTimesInline;
	}

	public static Pattern pattSpecialServer = Pattern
			.compile("^monkey\\s+--only\\s+(\\S+)\\s+-c\"(.*)\"\\s+--conf\\s+(.*)");

	public static Pattern pattSpecialServerDefault = Pattern
			.compile("^monkey\\s+--only\\s+(\\S+)\\s+-c\"(.*)\"");

	public static Pattern pattSpecialClient = Pattern
			.compile("^monkey\\s+--only\\s+(\\S+)\\s+-c\"(.*)\"");

	public static void main(String[] args) {

		String cmd = "monkey  --only  CNC-CC-8-5AM  -c\"hive -e \"use rdb; select * from fc_rdb_seq where pt=20130528 limit 1\" >/home/tianyu.yang/aaa/a.out || cat  /home/tianyu.yang/aaa/a.out \"  ";

		ParseCmd parse = new ParseCmd();
		try {
			parse.initInputCmdServer(cmd);
			parse.debugParseCmd();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
