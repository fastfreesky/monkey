package com.ccindex.tool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Monkey {

	private static Pattern monkeySelf = Pattern
			.compile("monkey\\s+(start|stop|restart)\\s*$");

	public static boolean isMonkeySelfCmd(String cmd) {

		Matcher match = monkeySelf.matcher(cmd);
		if (match.find()) {
			return true;
		} else {
			return false;
		}
	}

	public static String packageMonkeyCmd(String cmd) {
		return "nohup " + cmd + " &>/dev/null &";
	}

	public static void main(String[] args) {
		String cmd = "monkey start";
		System.out.println(Monkey.isMonkeySelfCmd(cmd));
		System.out.println(Monkey.packageMonkeyCmd(cmd));

		cmd = "monkey stop";
		System.out.println(Monkey.isMonkeySelfCmd(cmd));
		System.out.println(Monkey.packageMonkeyCmd(cmd));

		cmd = "monkey restart";
		System.out.println(Monkey.isMonkeySelfCmd(cmd));
		System.out.println(Monkey.packageMonkeyCmd(cmd));


	}
}
