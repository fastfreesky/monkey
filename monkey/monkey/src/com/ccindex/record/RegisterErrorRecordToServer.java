package com.ccindex.record;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

import com.ccindex.constant.Constant;

/**
 * 
 * @ClassName: ErrorRecord
 * @Description: TODO(这里用一句话描述这个类的作用)记录客户端错误信息
 * @author tianyu.yang
 * @date 2013-4-13 上午9:39:59
 * 
 */
public class RegisterErrorRecordToServer {

	private static DateFormat fm = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private static DateFormat fmDate = new SimpleDateFormat("yyyy-MM-dd");

	private static String error;

	public static void setError(String erro) {
		Date start = new Date(System.currentTimeMillis());
		String day = fm.format(start);
		error = "[" + day + "] " + erro;
	}

	public static String getError() {
		return error;
	}

	public static void setErrorRecord(ZooKeeper zk) {
		try {
			if (error == null) {
				return;
			}
			// 取出日期,判断是否为当天错误日志
			List<String> listError = zk.getChildren("/error", false);

			String nowTime = fm.format(new Date(System.currentTimeMillis()));
			String today = fmDate.format(new Date(System.currentTimeMillis()));

			String todayPath = "/error/" + today;

			String errorPath = todayPath + "/" + Constant.getHostname();

			// 当天日期已经存在
			if (listError != null && listError.contains(today)) {
				List<String> listErrorDetails = zk
						.getChildren(todayPath, false);

				if (listErrorDetails.contains(Constant.getHostname())) {
					byte[] bytes = zk.getData(errorPath, false, null);
					String newValue = new String(bytes) + "\n" + error;
					zk.setData(errorPath, newValue.getBytes(), -1);

				} else {
					zk.create(errorPath, error.getBytes(), Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT);
				}

			} else {
				zk.create(todayPath, nowTime.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
				zk.create(errorPath, error.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}

		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		error = null;
	}
}
