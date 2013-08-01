package com.ccindex.tool;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ccindex.constant.Constant;

/**
 * 
 * @ClassName: parseCmd
 * @Description: TODO(这里用一句话描述这个类的作用)解析传递过来的cmd指令,组装linux系统识别的指令
 * @author tianyu.yang
 * @date 2013-3-8 下午3:48:52
 * 
 */
public class CmdParse {
	// 格式monkey --all -c"at 10:56 echo "hello world" > a.out"
	private static Pattern pattAll = Pattern
			.compile("^monkey(?:\\s+--all\\s+|\\s+)-c\"(.*)\"$");
	public static Pattern pattSpecial = Pattern
			.compile("^monkey\\s+--only\\s+(\\S+)\\s+-c\"(.*)\"");

	// 对at进行格式化判断
	private static Pattern patAt = Pattern
			.compile("^(\\S*)\\s+((at|crontab)\\s+[^-]*)(?:--(\\S+)|\\s*)");

	public static ArrayList<String> parseCmd(String cmd) {
		String cmdParse = cmd.trim();
		String cmdReturn = null;
		ArrayList<String> arrlist = new ArrayList<String>();
		// 匹配全量运行
		Matcher matAll = pattAll.matcher(cmdParse);
		if (matAll.find()) {
			// 匹配到命令列表
			cmdReturn = matAll.group(1);
		}

		String hostName = Constant.getHostname();
		// 匹配特殊运行,目前识别设备型号
		Matcher matSpecial = pattSpecial.matcher(cmdParse);
		if (matSpecial.find()) {
			String hostList = matSpecial.group(1);
			if (hostList.contains(hostName)) {
				cmdReturn = matSpecial.group(2);
			}
		}

		if (cmdReturn == null) {
			return null;
		} else {
			String cmds[] = cmdReturn.split("\\|\\|");
			for (String cm : cmds) {
				arrlist.add(cm.trim());
			}
		}

		return arrlist;

	}

	public static ArrayList<String> getCmd(String cmd) {
		String cmdParse = cmd.trim();
		String cmdReturn = null;
		ArrayList<String> arrlist = new ArrayList<String>();
		// 匹配全量运行
		Matcher matAll = pattAll.matcher(cmdParse);
		if (matAll.find()) {
			// 匹配到命令列表
			cmdReturn = matAll.group(1);
		}

		// 匹配特殊运行,目前识别设备型号
		Matcher matSpecial = pattSpecial.matcher(cmdParse);
		if (matSpecial.find()) {
			cmdReturn = matSpecial.group(2);
		}

		if (cmdReturn == null) {
			return null;
		} else {
			String cmds[] = cmdReturn.split("\\|\\|");
			for (String cm : cmds) {
				arrlist.add(cm.trim());
			}
		}

		return arrlist;

	}
	public static void main(String[] args) {

		Constant.setHostname("liyingli");
		String cmd = "monkey --only liyingli -c\"root at 10:09 do something --del || aaa \"";
		// // parseCmd(cmd);
		// // cmd = "monkey --only liyingli -c\"root at 10:09 do something\"";
		// // parseCmd(cmd);
		//
		// cmd = "monkey -c\"root at 10:09 do something\"";
		// parseCmd(cmd);

		//cmd = "monkey --only liyingli -c\"tree\"";
		parseCmd(cmd);

	}
}
