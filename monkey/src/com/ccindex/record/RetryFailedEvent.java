package com.ccindex.record;

import java.util.ArrayList;
import java.util.List;

import com.ccindex.tool.CmdPackage;
import com.ccindex.tool.CmdParse;

/**
 * 
 * @ClassName: RetryFailedEvent
 * @Description: TODO(这里用一句话描述这个类的作用)重新尝试时,重新组装命令,去除之前成功完成的任务,只进行尚未完成的任务
 * @author tianyu.yang
 * @date 2013-4-24 上午9:57:35
 * 
 */
public class RetryFailedEvent {

	// 记录应当运行的全部机器
	public static List<String> hostNameTaskAll = new ArrayList<String>();
	// 已经完成的机器列表
	public static List<String> hostNameTaskSucceed = new ArrayList<String>();
	// 需要重新运行的机器列表
	public static List<String> hostNameTaskNeedRetry = new ArrayList<String>();

	public static void init() {
		hostNameTaskAll.clear();
		hostNameTaskSucceed.clear();
		hostNameTaskNeedRetry.clear();
	}

	public static List<String> getHostNameTaskAll() {
		return hostNameTaskAll;
	}

	public static void setHostNameTaskAll(List<String> hostNameTaskAll) {
		RetryFailedEvent.hostNameTaskAll.addAll(hostNameTaskAll);
	}

	public static List<String> getHostNameTaskSucceed() {
		return hostNameTaskSucceed;
	}

	public static void setHostNameTaskSucceed(List<String> hostNameTaskSucceed) {
		RetryFailedEvent.hostNameTaskSucceed.addAll(hostNameTaskSucceed);
	}

	public static void setHostNameTaskSucceed(String hostNameTaskSucceed) {
		RetryFailedEvent.hostNameTaskSucceed.add(hostNameTaskSucceed);
	}

	/**
	 * 
	 * @Title: getHostNameTaskNeedRetry
	 * @Description: TODO(这里用一句话描述这个方法的作用)内部通过全部任务列表,完成任务列表,确定哪些尚未完成
	 * @return List<String>
	 * @throws
	 */
	public static List<String> getHostNameTaskNeedRetry() {

		hostNameTaskNeedRetry.clear();

		for (String tl : hostNameTaskAll) {
			if (!hostNameTaskSucceed.contains(tl)) {
				hostNameTaskNeedRetry.add(tl);
			}
		}

		return hostNameTaskNeedRetry;
	}

	/**
	 * 
	 * @Title: packageNewTask
	 * @Description: TODO(这里用一句话描述这个方法的作用)组装新的任务,修改地方--only || --all || --total
	 *               --less --timeout等
	 * @param task
	 * @return String
	 * @throws
	 */
	public static String packageNewTask(String task) {

		if (hostNameTaskAll.size() == 0 || hostNameTaskSucceed.size() == 0) {
			return null;
		}
		StringBuffer buf = new StringBuffer();
		// 处理命令
		CmdPackage cmdPackage = new CmdPackage(task);

		int total = cmdPackage.getTotal() - hostNameTaskSucceed.size();
		int less = cmdPackage.getLess() - hostNameTaskSucceed.size();
		int timeout = cmdPackage.getTimeout();

		buf.append("monkey --only ");
		List<String> arr = getHostNameTaskNeedRetry();
		int num = arr.size();
		for (String ar : arr) {
			if (num == 1)
				buf.append(ar).append(" ");
			else
				buf.append(ar).append(";");

			num--;
		}

		buf.append(" -c\"");

		ArrayList<String> cmd = CmdParse.getCmd(cmdPackage.getCmd());
		num = cmd.size();
		for (String ar : cmd) {
			if (num == 1)
				buf.append(ar).append("\" --conf ");
			else
				buf.append(ar).append("||");

			num--;
		}

		buf.append("total=").append(total).append(" less=").append(less)
				.append(" timeout=").append(timeout);

		return buf.toString();
	}

	public static void main(String[] args) {

		init();
		List<String> all = new ArrayList<String>();
		all.add("aa-bb-23-dd");
		all.add("aa-bb-23-ee");

		List<String> succ = new ArrayList<String>();
		succ.add("aa-bb-23-dd");

		setHostNameTaskAll(all);
		setHostNameTaskSucceed(succ);

		String aa = packageNewTask("monkey -c\"ls -al /home\" --conf total=2 less=2 timeout=30");
	}
}
