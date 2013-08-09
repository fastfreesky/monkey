package com.ccindex.tool;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ccindex.warn.MonkeyOut;

public class Hive {

	private static Pattern hive = Pattern.compile("hive.*-e\\s*\".*\"");
	public static Pattern hiveRedirect = Pattern
			.compile("hive.*-e\\s*\".*\"\\s*>\\s*(\\S+)\\s*");

	public static Pattern redirect = Pattern.compile(".*>\\s*(\\S+)\\s*");

	public static boolean chmodFile(String file) {
		String command = "chmod -R 777 " + file;
		Runtime runtime = Runtime.getRuntime();
		try {
			Process proc = runtime.exec(command);
			proc.waitFor();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public static boolean isRedirectOut(String cmd) {
		Matcher match = redirect.matcher(cmd);
		if (match.find()) {
			String outF = match.group(1);
			File file = new File(outF.trim());

			File fileD = new File(file.getParent());
			if (!fileD.exists()) {
				MonkeyOut.info(Hive.class, "Mkdir " + fileD.getPath());
				boolean ret = fileD.mkdirs();
				if (ret) {
					return chmodFile(fileD.getPath());
				} else {
					return false;
				}

			}

			return true;
		} else {
			return false;
		}
	}

	public static String parseHiveArgs(String[] hiveArgs) {
		// hive语句组装判断
		if (hiveArgs.length != 4 && hiveArgs.length != 5
				&& hiveArgs.length != 8) {
			throw new IndexOutOfBoundsException(
					"Error Params: \n\t[0]--start type(server|client|hive)\n\t[1]--user\n\t[2]--hostName(;splite)\n\t[3]--hiveSql\n\t[4]--outFile\n\t[5]--destIp\n\t[6]--destPath\n\t[7]--timeout");
		}
		String user = hiveArgs[1];
		String hostName = hiveArgs[2];
		String hiveSql = hiveArgs[3];

		StringBuffer buf = new StringBuffer();

		buf.append("monkey --only ");
		// 运行的设备
		buf.append(hostName).append(" -c\"su - ").append(user)
				.append(" -c\"hive -e\\\"");

		// 执行hive,输出到屏幕 monkey --only host -c" su - user -c" hive -e "hiveSql"
		// " "
		// "  "
		if (hiveArgs.length == 4) {
			buf.append(hiveSql).append(" \\\"\"\"");

		} else if (hiveArgs.length == 5) {
			String outFile = hiveArgs[4].trim();

			buf.append(hiveSql).append(" \\\" > ").append(outFile)
					.append("\"\"");

		} else if (hiveArgs.length == 8) {
			String outFile = hiveArgs[4].trim();
			String destIp = hiveArgs[5].trim();
			String destPath = hiveArgs[6].trim();
			String timeout = hiveArgs[7].trim();

			// 重定向到文件并上传到同一个地方monkey --only host -c" su - user -c" hive -e
			// "hiveSql" > outfile
			// " || curl -F Filedata=@$outLog http://$destIp/put$destPath  "
			// buf.append(hiveSql).append(" \\\" > ").append(outFile)
			// .append("\" || curl -F Filedata=@").append(outFile)
			// .append(" http://").append(destIp).append("/put")
			// .append(destPath).append("\"");

			buf.append(hiveSql).append(" \\\" > ").append(outFile)
					.append("\" || ").append("su - ").append(user)
					.append(" -c\"").append("curl -F Filedata=@")
					.append(outFile).append(" http://").append(destIp)
					.append("/put").append(destPath).append("\"\"");
		}

		return buf.toString();
	}

	public static boolean isHiveCmd(String cmd) {

		Matcher matchHive = hive.matcher(cmd);
		Matcher matchHiveRedirect = hiveRedirect.matcher(cmd);

		if (matchHiveRedirect.find()) {
			String outF = matchHiveRedirect.group(1);
			File file = new File(outF.trim());

			File fileD = new File(file.getParent());
			if (!fileD.exists()) {
				MonkeyOut.info(Hive.class, "Mkdir " + fileD.getPath());
				return fileD.mkdirs();
			}

			return true;
		}

		if (matchHive.find()) {
			return true;
		}

		return false;
	}

	public static void main(String[] args) {
		// String cmd =
		// "su - tianyu.yang -c\"hive -e \"use rdb; select * from fc_rdb_seq where pt=20130528 limit 1\" > D:\\ddd\\test\\test1.txt\"";
		// System.out.println(isRedirectOut(cmd));

		String[] arg = { "hive", "CCN-BJ-G-3N9,CHN-HZ-2-3N9,CHN-CQ-I-5A1",
				"tianyu.yang", "add jar hdfs:///user/hive/udf/hive_udf.jar;",
				"/home/tianyu.yang/cbu_apple/2013.out", "210.14.132.235:8888",
				"/home/tianyu.yang/itemProject", "3600" };

		// String[] arg = { "hive", "CCN-BJ-G-3N9,CHN-HZ-2-3N9,CHN-CQ-I-5A1",
		// "tianyu.yang", "add jar hdfs:///user/hive/udf/hive_udf.jar;",
		// "/home/tianyu.yang/cbu_apple/2013.out" };

		// String[] arg = { "hive", "CCN-BJ-G-3N9,CHN-HZ-2-3N9,CHN-CQ-I-5A1",
		// "tianyu.yang", "add jar hdfs:///user/hive/udf/hive_udf.jar;"};
		System.out.println(parseHiveArgs(arg));

	}
}
