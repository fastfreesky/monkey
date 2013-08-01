package com.ccindex.constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * 
 * @ClassName: Debug
 * @Description: TODO(这里用一句话描述这个类的作用)简单封装logger,不用每个类进行getLogger初始化
 * @author tianyu.yang
 * @date 2013-3-18 下午2:03:58
 * 
 */
public class Debug {
	// protected static Logger logger = Logger.getLogger(Client.class);

	private static HashMap<String, Logger> arr = new HashMap<String, Logger>();

	public static void debug(Class c, Object message) {
		Logger logger = getLogger(c);
		logger.debug(message);
	}

	public static void info(Class c, Object message) {
		Logger logger = getLogger(c);
		logger.info(message);
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
		File flie = new File("D:\\nowRunNode1.txt");
		BufferedReader br;

		try {
			br = new BufferedReader(new FileReader(flie));
			String line = null;
			while ((line = br.readLine()) != null) {
				// 判定only指定的机器,是否也在运行中,不在,则退出BGP-BJ-9-3m1
				System.out.println("Src :" + line);

				if (line.trim().replace("\r", "").equals("123")) {
					System.out.println(line);
				} else {
					System.out.println("---" + line);
				}
			}

			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
