package com.ccindex.tool;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @ClassName: ParseSetValue
 * @Description: TODO(这里用一句话描述这个类的作用)对命令进行拆分的类,确定是否需要进行特殊的配置等
 * @author tianyu.yang
 * @date 2013-3-20 上午10:27:03
 * 
 */
public class CmdPackage {
	private String cmd;
	private int total=1;
	private int less=1;
	private int timeout=0;

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
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

	// 从set值中摘取--conf配置
	private static Pattern cmdModelSetConf = Pattern
			.compile("(.*)--conf\\s+(.*)");

	public CmdPackage(String value) {
		Matcher match = cmdModelSetConf.matcher(value);
		if (match.find()) {
			String cmd = match.group(1);
			setCmd(cmd);

			String conf = match.group(2);
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

		} else {
			setCmd(value);
			setTotal(1);
			setLess(1);
		}
	}

	public static void main(String[] args) {
		String cmd = "monkey -c\"ls -al\" --conf total=10;less=10;timeout=10";
		
		CmdPackage cm=new CmdPackage(cmd);
	}
}
