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
 * @ClassName: SendMsg
 * @Description: TODO(这里用一句话描述这个类的作用)发送短信方式
 * @author tianyu.yang
 * @date 2013-7-24 上午10:15:45
 * 
 */
public class SendMsg {

	private static String msg;
	private static boolean status;

	public static String getMsg() {
		return msg;
	}

	public static void setMsg(String msg) {
		SendMsg.msg = msg;
	}

	public static boolean isStatus() {
		return status;
	}

	public static void setStatus(boolean status) {
		SendMsg.status = status;
	}

	public static void sendMsg() {
		if (getMsg() != null) {
			if (isStatus()) {
				MonkeyOut.error(SendMsg.class, getMsg() + " OK");
			} else {
				MonkeyOut.error(SendMsg.class, getMsg() + " ERROR");
			}
		}
	}

	public static void main(String[] args) {
		// setMsg("123456789012345678901234567890");
		setStatus(true);
		sendMsg();
	}
}
