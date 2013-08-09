package com.ccindex.interfaceI;

import java.util.List;

/**
 * 
 * @ClassName: MonkeyListener
 * @Description: TODO(这里用一句话描述这个类的作用)监听器的接口
 * @author tianyu.yang
 * @date 2013-3-14 下午2:13:43
 * 
 */
public interface MonkeyListenerI<T> {
	/**
	 * 
	 * @Title: exists 
	 * @Description: TODO(这里用一句话描述这个方法的作用)处理监听到的数据 
	 * @param t    待处理的类型
	 * boolean 返回为false,则外部继续监视该操作,为true则不进行监视,停止操作
	 * @throws
	 */
	boolean exists(T t);
}
