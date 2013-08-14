package com.ccindex.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.ccindex.warn.MonkeyOut;
import com.ccindex.warn.SendMsg;

/**
 * 
 * @ClassName: ParseArgs
 * @Description: TODO(这里用一句话描述这个类的作用)解析参数类
 * @author tianyu.yang
 * @date 2013-5-28 下午2:26:36
 * 
 */
public class ParseArgs {

	// 启动类型
	private enum startTypeE {
		client, server, hive, check;

		public static boolean isContains(String key) {
			if (key != null
					&& (key.equals(server.toString())
							|| key.equals(client.toString())
							|| key.equals(hive.toString()) || key.equals(check
							.toString()))) {

				return true;
			}
			return false;
		}
	}

	public static void main(String[] args) {

		// String[] arg = { "hive", "CCN-BJ-G-3N9,CHN-HZ-2-3N9,CHN-CQ-I-5A1",
		// "tianyu.yang", "add jar hdfs:///user/hive/udf/hive_udf.jar;",
		// "/home/tianyu.yang/cbu_apple/2013.out", "210.14.132.235:8888",
		// "/home/tianyu.yang/itemProject","3600" };

		// String[] arg = { "hive", "CCN-BJ-G-3N9,CHN-HZ-2-3N9,CHN-CQ-I-5A1",
		// "tianyu.yang", "add jar hdfs:///user/hive/udf/hive_udf.jar;",
		// "/home/tianyu.yang/cbu_apple/2013.out" };

		String[] arg = { "hive", "CCN-BJ-G-3N9,CHN-HZ-2-3N9,CHN-CQ-I-5A1",
				"tianyu.yang", "add jar hdfs:///user/hive/udf/hive_udf.jar;",
				"--msg=中国最牛", "--conf=a.txt" };

		try {
			ParseArgs parseArgs = new ParseArgs(arg);
			System.out.println(parseArgs.getIpPort());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 启动脚本配置
	private String config = null;
	// yaml配置文件
	private ConfigUtil configUtil;
	// 连接服务端的IP及端口
	private String ipPort = null;
	// 日志记录目录
	private String recordDir;
	// 分发重试次数
	private int retryTimes = 0;
	// 内部重试时间及重试次数
	private int retrytimesInline = 60;
	// 服务端设置的程序命令
	private String serverCmd = null;
	// 服务端IP及端口
	private String serverIpPort;
	// 执行shell脚本的脚本
	private String shell;
	private String startType = null;
	// 重试间隔时间
	private int timeout = 0;
	// 用户
	private String user;

	private int timeoutInline = 0;

	// log4j外部配置路径
	private String confLog4j;
	// 短信内容
	private String msg;
	// 待监测的命令结果
	private String checkCmd;

	public String getConfLog4j() {
		return confLog4j;
	}

	public String getMsg() {
		return msg;
	}

	public void setConfLog4j() {
		this.confLog4j = (String) configUtil.get("monkey", "log4j.conf.file");
	}

	/**
	 * 
	 * @Title: getRealArag
	 * @Description: TODO(这里用一句话描述这个方法的作用)从传参中获取配置文件相关信息,返回其他应用级的参数信息
	 * @param args
	 * @return String[]
	 * @throws
	 */
	private String[] getRealArag(String args[]) {

		// conf:monkey.yaml配置文件,--msg,如果发送短信的话,短息内容
		for (String s : args) {
			if (s.startsWith("--conf=")) {
				config = s.substring(7);
			} else if (s.startsWith("--msg=")) {
				msg = s.substring(6);
			}
		}

		if (config == null && msg == null) {
			return args;
		}

		String[] newArgs;
		if (config != null && msg != null) {
			newArgs = new String[args.length - 2];
		} else {
			newArgs = new String[args.length - 1];
		}

		int i = 0;
		for (String s : args) {
			if (s.startsWith("--conf=") || s.startsWith("--msg=")) {
				continue;
			} else {
				newArgs[i] = s;
				i++;
			}
		}

		return newArgs;
	}

	private String packageServerCmd(String args[]) {
		String user = args[1];
		String hostName = args[2];
		String hiveSql = args[3];

		StringBuffer buf = new StringBuffer();

		buf.append("monkey --only ").append(hostName).append(" -c\"");
		if (user.equalsIgnoreCase("root")) {
			// 运行的设备
			buf.append(hiveSql);

		} else {
			String[] hiveSqlA = hiveSql.split("\\|\\|");
			for (int i = 0; i < hiveSqlA.length; ++i) {
				if (i == hiveSqlA.length - 1) {
					buf.append("su - ").append(user).append(" -c\"")
							.append(hiveSqlA[i]).append(" \"");
				} else {
					buf.append("su - ").append(user).append(" -c\"")
							.append(hiveSqlA[i]).append(" \" || ");
				}
			}
		}

		buf.append("\"");

		return buf.toString();
	}

	public ParseArgs(String args[]) throws FileNotFoundException {
		String[] argsReal = getRealArag(args);

		if (argsReal.length < 1) {
			throw new IndexOutOfBoundsException(
					"\nError Params: \n\t[0]--start type(server|client|hive)\n\t");
		}

		startType = argsReal[0];

		if (!startTypeE.isContains(startType)) {
			throw new IndexOutOfBoundsException(
					"\nError StartType: \n\t[0]--start type(server|client|hive) ");
		}

		// 服务端命令参数解析
		if (startType.equals(startTypeE.server.toString())) {
			if (argsReal.length < 2) {
				throw new IndexOutOfBoundsException(
						"\nError Params: \n\t[0]--start type(server|client|hive)\n\t[1]--cmd\n");
			}
			// 另类命令格式:server user hostname cmd
			if (argsReal.length == 4) {
				// 组装执行命令
				setServerCmd(packageServerCmd(argsReal));
			} else {
				setServerCmd(argsReal[1]);
			}

		} else if (startType.equals(startTypeE.hive.toString())) {
			// 解析组装hive指令
			String finlCm = Hive.parseHiveArgs(argsReal);
			// System.out.println(finlCm);

			// 修改启动方式为server方式
			startType = startTypeE.server.toString();
			setServerCmd(finlCm);
		} else if (startType.equals(startTypeE.check.toString())) {
			// 监测某任务哪些任务尚未完成
			checkCmd = argsReal[1];
		}

		if (config == null) {
			configUtil = new ConfigUtil(this.getClass().getResourceAsStream(
					"/monkey.yaml"));
		} else {
			configUtil = new ConfigUtil(config);
		}

		getValueFromConfig();

		// 之后有调试信息,更改log4j的配置
		MonkeyOut.initConf(getConfLog4j());

		debugParseArgs();
	}

	public String getCheckCmd() {
		return checkCmd;
	}

	private void getValueFromConfig() {

		setServerIpPort();
		setRetryTimesAndTimeoutDefault();
		setRetrytimesInlineAndTimeout();
		setRecordDir();
		setShell();
		setConfLog4j();

		// 从服务器池中,随即获取两个
		ipPort = getServerIpPort();

	}

	public void debugParseArgs() {

		MonkeyOut.info(getClass(), toString());
	}

	public void setServerCmd(String serverCmd) {

		File file = new File(serverCmd);
		if (file.exists() && file.isFile()) {
			this.serverCmd = ReadFromFile.readFileByLines(serverCmd);
		} else {
			this.serverCmd = serverCmd;
		}
	}

	// 获取yaml的配置文件
	public ConfigUtil getConfigUtil() {
		return configUtil;
	}

	// 获取IP及端口
	public String getIpPort() {
		return ipPort;
	}

	// 获取需要实例化的入口类列表
	private List<Map> getMainClass() {
		return (List<Map>) configUtil.get("monkey", "input.class.main");
	}

	// 获取需要实例化的入口类
	public String getMainClass(String key) {
		List<Map> map = getMainClass();
		for (Map<String, String> mp : map) {
			if (mp.get("name").toString().equals(key)) {
				return mp.get("class").toString();
			}
		}

		return null;
	}

	// 获取需要实例化的入口类
	public Object getMainClassDefault() {
		List<Map> map = getMainClass();
		for (Map<String, String> mp : map) {
			if (mp.get("name").toString().equals(getStartType())) {
				return ObjectFactory.newInstance(mp.get("class").toString());
			}
		}

		return null;
	}

	public String getRecordDir() {
		return recordDir;
	}

	// 获取客户端及服务端设定的重试次数
	private List<Map> getRetryTimes() {
		return (List<Map>) configUtil.get("monkey", "input.class.retry");
	}

	// 默认的重试次数
	public int getRetryTimesDefault() {
		return retryTimes;
	}

	public int getRetrytimesInline() {
		return retrytimesInline;
	}

	// 服务端设置的命令字符串
	public String getServerCmd() {
		return serverCmd;
	}

	// 服务端命令按照规则解析到一个数组中,用::区分多条命令
	public String[] getServerCmdList() {
		if (getServerCmd() == null) {
			return null;
		}

		return getServerCmd().split("::");
	}

	private String getServerIpPort() {
		Random rand = new Random();

		String spSever[] = serverIpPort.split(",");
		int size = spSever.length;
		ArrayList<Integer> arr = new ArrayList<Integer>();
		while (true) {
			int n = rand.nextInt(size);
			if (arr.contains(n)) {
				continue;
			} else {
				arr.add(n);
			}

			if (arr.size() == 2) {
				break;
			}
		}

		String retValue = "";
		for (int c : arr) {
			retValue += spSever[c] + ",";
		}

		return retValue.substring(0, retValue.length() - 1);
	}

	public String getShell() {
		return shell;
	}

	// 获取启动类型
	public String getStartType() {
		return startType;
	}

	public int getTimeout() {
		return timeout;
	}

	public int getTimeoutInline() {
		return timeoutInline;
	}

	public void setRecordDir() {
		recordDir = (String) configUtil.get("monkey", "output.record.dir");
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public void setRetryTimesAndTimeoutDefault() {

		List<Map> map = getRetryTimes();
		for (Map<String, Object> mp : map) {
			if (mp.get("name").toString().equals(getStartType())) {

				if (retryTimes <= 0) {
					retryTimes = (Integer) mp.get("retry");
				}
				timeout = (Integer) mp.get("timeout");
			}
		}
	}

	public void setRetrytimesInlineAndTimeout() {

		List<Map> map = getRetryTimes();
		for (Map<String, Object> mp : map) {
			if (mp.get("name").toString().equals("heart")) {

				retrytimesInline = (Integer) mp.get("retry");
				timeoutInline = (Integer) mp.get("timeout");
			}
		}
	}

	private void setServerIpPort() {
		serverIpPort = (String) configUtil.get("monkey", "zookeeper.server");
	}

	public void setShell() {
		shell = (String) configUtil.get("monkey", "client.shell.path");
	}

	@Override
	public String toString() {
		return "ParseArgs [getConfLog4j()=" + getConfLog4j() + ", getMsg()="
				+ getMsg() + ", getIpPort()=" + getIpPort()
				+ ", getMainClass()=" + getMainClass()
				+ ", getMainClassDefault()=" + getMainClassDefault()
				+ ", getRecordDir()=" + getRecordDir() + ", getRetryTimes()="
				+ getRetryTimes() + ", getRetryTimesDefault()="
				+ getRetryTimesDefault() + ", getRetrytimesInline()="
				+ getRetrytimesInline() + ", getServerCmd()=" + getServerCmd()
				+ ", getServerCmdList()=" + Arrays.toString(getServerCmdList())
				+ ", getServerIpPort()=" + getServerIpPort() + ", getShell()="
				+ getShell() + ", getStartType()=" + getStartType()
				+ ", getTimeout()=" + getTimeout() + ", getTimeoutInline()="
				+ getTimeoutInline() + "]";
	}

}
