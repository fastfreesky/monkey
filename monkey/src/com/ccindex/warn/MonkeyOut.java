package com.ccindex.warn;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * 
 * @ClassName: MonkeyOut
 * @Description: TODO(这里用一句话描述这个类的作用)调度输出控制处
 * @author tianyu.yang
 * @date 2013-5-28 下午2:51:29
 * 
 */
public class MonkeyOut {

	// 不同类对应的log4j
	private static HashMap<String, Logger> arr = new HashMap<String, Logger>();

	public static void initConf(String confLog4j) {
		if (confLog4j != null) {
			File file = new File(confLog4j);
			if (file.exists()) {
				PropertyConfigurator.configure(confLog4j);
				debug(MonkeyOut.class, "Read Log4j Conf: " + confLog4j);
			}
		}
	}

	public static void debug(Class c, Object message) {
		Logger logger = getLogger(c);
		logger.debug(message);
	}

	public static void info(Class c, Object message) {
		Logger logger = getLogger(c);
		logger.info(message);
	}

	public static void error(Class c, Object message) {
		Logger logger = getLogger(c);
		logger.error(message);
	}

	private static Logger getLogger(Class key) {
		Logger logger = null;
		if (arr.containsKey(key.toString())) {
			logger = arr.get(key.toString());
		} else {
			logger = Logger.getLogger(key);
			arr.put(key.toString(), logger);
		}
		return logger;
	}

	public static void main(String[] args) {
		debug(MonkeyOut.class, "Test 1 debug");
		info(MonkeyOut.class, "Test 1 info");

	}
}
