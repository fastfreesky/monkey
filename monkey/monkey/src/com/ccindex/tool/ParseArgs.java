package com.ccindex.tool;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import com.ccindex.main.ObjectFactory;
import com.ccindex.warn.MonkeyOut;

/**
 * 
 * @ClassName: ParseArgs
 * @Description: TODO(这里用一句话描述这个类的作用)解析参数类
 * @author tianyu.yang
 * @date 2013-5-28 下午2:26:36
 * 
 */
public class ParseArgs {

	// yaml配置文件
	private ConfigUtil configUtil;

	// 启动类型
	private enum startTypeE {
		server, client;

		public static boolean isContains(String key) {
			if (key != null
					&& (key.equals(server.toString()) || key.equals(client))) {
				return true;
			}
			return false;
		}
	}

	private String startType = null;
	// 连接服务端的IP及端口
	private String ipPort = null;
	// 启动脚本配置
	private String config = null;
	// 服务端设置的程序命令
	private String serverCmd = null;
	// 服务端命令分发重试次数
	private int serverRetryTimes = 0;

	public ParseArgs(String args[]) throws FileNotFoundException {
		if (args.length < 2) {
			throw new IndexOutOfBoundsException(
					"\nError Params: \n\t[0]--start type(server|client)\n\t[1]--ip:port(more than one, use \",\" to split)");
		}

		if (!startTypeE.isContains(args[0])) {
			throw new IndexOutOfBoundsException(
					"\nError StartType: \n\t[0]--start type(server|client) ");
		}

		startType = args[0];
		ipPort = args[1];

		if (startType.equals(startTypeE.server)) {
			if (args.length < 3) {
				throw new IndexOutOfBoundsException(
						"\nError Params: \n\t[0]--start type(server|client)\n\t[1]--ip:port(more than one, use \",\" to split)\n\t[2]--cmd\n\t[3]--retryTimes(not must need,if has,must bu int type)");
			}

			serverCmd = args[2];
			if (args.length == 4) {
				serverRetryTimes = Integer.parseInt(args[3]);
			}
		}

		if (config == null) {
			config = "./conf/monkey.yaml";
		}

		configUtil = new ConfigUtil(config);

	}

	// 获取启动类型
	public String getStartType() {
		return startType;
	}

	// 获取IP及端口
	public String getIpPort() {
		return ipPort;
	}

	// 获取yaml的配置文件
	public ConfigUtil getConfigUtil() {
		return configUtil;
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

	// 获取需要实例化的入口类列表
	public List<Map> getMainClass() {
		return (List<Map>) configUtil.get("monkey", "input.class.main");
	}

	// 服务端设置的命令字符串
	public String getServerCmd() {
		return serverCmd;
	}

	// 服务端命令按照规则解析到一个数组中,用::区分多条命令
	public String[] getServerCmdList() {
		return getServerCmd().split("::");
	}

	public int getServerRetryTimes() {
		if (serverRetryTimes <= 0) {
			serverRetryTimes = getRetryTimesDefault();
		}

		return serverRetryTimes;
	}

	// 默认的重试次数
	public int getRetryTimesDefault() {
		List<Map> map = getMainClass();
		for (Map<String, String> mp : map) {
			if (mp.get("name").toString().equals(getStartType())) {
				return Integer.parseInt(mp.get("retry").toString());
			}
		}
		return 1;
	}

	// 获取客户端及服务端设定的重试次数
	public List<Map> getRetryTimes() {
		return (List<Map>) configUtil.get("monkey", "input.class.retry");
	}

}
