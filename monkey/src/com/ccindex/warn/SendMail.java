package com.ccindex.warn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

/**
 * 
 * @ClassName: SendMail
 * @Description: TODO(这里用一句话描述这个类的作用)monkey进程中执行报告
 * @author tianyu.yang
 * @date 2013-4-17 上午10:33:30
 * 
 */
public class SendMail {
	private static Logger logger = Logger.getLogger(SendMail.class);
	private static HashMap<String, StringBuffer> sendMailQ = new HashMap<String, StringBuffer>();
	private static String title;
	private static ArrayList<String> keyArray = new ArrayList<String>(100);

	// 项目总标志
	private static String titleAll;

	public static String getTitle() {
		return title;
	}

	public static void setTitle(String title) {
		SendMail.title = title;
	}

	/**
	 * 
	 * @Title: packageMail
	 * @Description: TODO(这里用一句话描述这个方法的作用)组装待发送的内容
	 * @param title
	 * @param values
	 *            void
	 * @throws
	 */
	public static void packageMail(String title, String values) {
		StringBuffer value;

		if (sendMailQ.containsKey(title)) {
			value = sendMailQ.get(title);
			value.append(values).append("\n");
		} else {
			value = new StringBuffer(values + "\n");
			sendMailQ.put(title, value);
			keyArray.add(title);
		}
	}

	/**
	 * 
	 * @Title: packageTitleAll
	 * @Description: TODO(这里用一句话描述这个方法的作用)设置总标题
	 * @param title
	 * @param values
	 *            void
	 * @throws
	 */
	public static void packageTitleAll(String title, String values) {
		titleAll = title + ": " + values + "\n";
	}

	/**
	 * 
	 * @Title: sendMail
	 * @Description: TODO(这里用一句话描述这个方法的作用)发送全部邮件列表 void
	 * @throws
	 */
	public static void sendMail() {

		Iterator iter = sendMailQ.entrySet().iterator();
		StringBuffer buf = new StringBuffer();
		int count = 1;

		buf.append(titleAll);

		for (String k : keyArray) {

			buf.append("Task [" + count + "] : ").append(k).append("\n")
					.append(sendMailQ.get(k).toString());

			count++;

		}

		logger.error(buf.toString());
	}

	public static void main(String[] args) {
		// logger.error("End Project");
		packageMail("ls -al", "vdddddddddddddddddddddddddddddddddddd");
		packageMail("ls -al",
				"vdddddddddddddddddddddddddddddddddddd44444444444444444444444444444");
		packageMail("ls",
				"vdddddddddddddddddddddddddddddddddddd44444444444444444444444444444");
		packageMail("ls",
				"vdddddddddddddddddddddddddddddddddddd44444444444444444444444444444");
		sendMail();
	}

}
