package com.ccindex.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * 
 * @ClassName: ConfigUtil
 * @Description: TODO(这里用一句话描述这个类的作用)读取配置文件类,目前主要解析yaml配置文件HashMap类别
 * @author tianyu.yang
 * @date 2013-5-28 下午2:01:06
 * 
 */
public class ConfigUtil {

	private HashMap<String, Object> config = new HashMap<String, Object>();

	/**
	 * 
	 * @Title: ConfigUtil.java
	 * @Description:
	 * @param config
	 *            传入配置的文件名,格式为HashMap解析方式
	 * @throws FileNotFoundException
	 */
	public ConfigUtil(String config) throws FileNotFoundException {
		InputStream in = new FileInputStream(new File(config));
		Yaml yaml = new Yaml(new SafeConstructor());
		this.config = (HashMap<String, Object>) yaml.load(in);
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Title: ConfigUtil.java
	 * @Description:
	 * @param config
	 *            传入配置的文件名,格式为HashMap解析方式
	 * @throws FileNotFoundException
	 */
	public ConfigUtil(InputStream in) throws FileNotFoundException {
		Yaml yaml = new Yaml(new SafeConstructor());
		this.config = (HashMap<String, Object>) yaml.load(in);
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Title: get
	 * @Description: TODO(获取yaml组中某key值对应的value)
	 * @param group
	 *            想要获取的组名
	 * @param key
	 *            对应组名下的key值
	 * @return Object 返回的对象
	 * @throws
	 */
	public Object get(String group, String key) {
		return ((Map) config.get(group)).get(key);
	}

	/**
	 * 
	 * @Title: getString
	 * @Description: TODO(获取yaml组中某key值对应的value)
	 * @param group
	 *            想要获取的组名
	 * @param key
	 *            对应组名下的key值
	 * @return String 返回的字符串
	 * @throws
	 */
	public String getString(String group, String key) {
		return ((Map) config.get(group)).get(key).toString();
	}

	/**
	 * 
	 * @Title: getGroup
	 * @Description: TODO(获取组信息)
	 * @param group
	 *            想要获取的组名
	 * @return Map<String,Object>
	 * @throws
	 */
	public Map<String, Object> getGroup(String group) {
		return ((Map) config.get(group));
	}
}
