package com.ccindex.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.ccindex.interfaceI.MonkeyListenerI;
import com.ccindex.operator.DataChange;
import com.ccindex.tool.CmdSet;
import com.ccindex.warn.MonkeyOut;
import com.ccindex.zookeeper.DialRequestThreads;
import com.ccindex.zookeeper.ZookeeperFactory;

/**
 * 
 * @ClassName: MonkeyListenerForGetChildOfGetData
 * @Description: TODO(这里用一句话描述这个类的作用)用于监测获取命令后,处理有变化的命令路径
 * @author tianyu.yang
 * @date 2013-3-14 下午5:11:59
 * 
 */
public class MonkeyListenerForRunServerCmd implements
		MonkeyListenerI<List<String>> {

	// 运行脚本的指定路径
	private static String perlPath = null;
	//
	private Watcher watcher;

	public void setZKAndPerlPathAdnWatcher(String perlPath, Watcher watcher) {
		// TODO Auto-generated constructor stub
		this.perlPath = perlPath;
		this.watcher = watcher;
	}

	/**
	 * 
	 * (非 Javadoc)
	 * 
	 * @Title: exists
	 * @Description:
	 * @param t传入的数据
	 *            ,一定和上一次不同
	 * @return
	 * @see com.ccindex.interfaceI.MonkeyListenerI#exists(java.lang.Object)
	 */
	@Override
	public boolean exists(List<String> t) {

		// TODO Auto-generated method stub
		// 数据一定不为空

		MonkeyOut.info(getClass(), "Get children change " + t);
		// .debug("处理存在的数据....获取到子节点的变化 " + t);
		// 获取节点中包含的的数据指令
		for (String cmd : t) {
			String cmdNew = CmdSet.packagePath(CmdSet.BASECMD, cmd);
			String resultNew = CmdSet.packagePath(CmdSet.BASERESULT, cmd);

			if (runningProcess.containsKey(cmdNew)) {
				MonkeyOut.debug(getClass(), "Already Running Process :"
						+ cmdNew);
				continue;
			} else {
				runningProcess.put(cmdNew, cmdNew);
				MonkeyOut.debug(getClass(), "Add Running Process :" + cmdNew);
			}

			Thread thread = new Thread(new DialRequestThreads(cmdNew,
					resultNew, perlPath, watcher));
			thread.start();
		}
		return false;
	}

	// 记录线程运行状态
	public static HashMap<String, String> runningProcess = new HashMap<String, String>();
}
