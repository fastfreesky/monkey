package com.ccindex.main;

import java.io.IOException;

import com.ccindex.constant.Constant;
import com.ccindex.constant.Debug;
import com.ccindex.constant.SucceedFlag;
import com.ccindex.record.RecordToFile;
import com.ccindex.record.RetryFailedEvent;
import com.ccindex.tool.ParseArgs;
import com.ccindex.warn.SendMail;
import com.ccindex.zookeeper.MonkeyServerSingle;

/**
 * 
 * @ClassName: Server
 * @Description: TODO(这里用一句话描述这个类的作用)服务端入口 ,单独的set指令,一次执行
 * @author tianyu.yang
 * @date 2013-3-5 下午5:18:05
 * 
 */
public class ServerSingle implements MonkeyMainI {
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 3) {
			System.out.println("Input Params: [num]" + args.length);
			System.out
					.println("Params:\n\t[1]--hostport:IP:Port(Eg:127.0.0.1:2181);\n\t[2]--value(Eg:\"monkey -c\\\"ls -al /home\\\"\");\n\t[3]-retry times\n\t");
			System.exit(2);
		}

		try {
			RecordToFile.init("/Application/monkey/state");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// 主机及端口, 监控的路径,心跳时间(暂不对外提供)
		String hostPort = args[0];
		String value = args[1];
		int retryTimes = Integer.parseInt(args[2]);

		// 拆解任务,每个线程处理一个任务
		String[] values = value.split("::");

		SendMail.packageTitleAll("Task ", value);

		for (String val : values) {

			Constant.setSucceed(false);
			Debug.info(ServerSingle.class, "Begin..." + args[0]);

			SendMail.setTitle(val);
			SendMail.packageMail(val, "Begin");

			// 初始化任务列表
			RetryFailedEvent.init();

			int i = 0;
			for (i = 0; i < retryTimes; ++i) {
				try {
					new MonkeyServerSingle(hostPort, val.trim()).run();
				} catch (Exception e) {
					if (i == retryTimes - 1) {
						SendMail.packageMail(val, e.toString());
						Debug.info(ServerSingle.class, "End...");
						SendMail.packageMail(val, "End");
						break;
					} else {
						SendMail.packageMail(val, e.toString());
						// 重试时候,进行任务重新组装
						String valT = RetryFailedEvent.packageNewTask(val
								.trim());
						if (valT != null) {
							val = valT;
						}

						SendMail.packageMail(val, "Retry Times [" + (i + 1)
								+ "]" + "Retry msg: " + val);
						try {
							// 10min
							Thread.sleep(600000);
							// Thread.sleep(30000);
						} catch (InterruptedException ee) {
							// TODO Auto-generated catch block
							ee.printStackTrace();
						}
						continue;
					}
				}

				// 如果执行成功,则跳出循环
				if (Constant.isSucceed()) {
					break;
				} else {
					// 重试时候,进行任务重新组装
					String valT = RetryFailedEvent.packageNewTask(val.trim());
					if (valT != null) {
						val = valT;
					}

					SendMail.packageMail(val, "Retry Times [" + (i + 1) + "]"
							+ "Retry msg: " + val);
					try {
						// 10min
						Thread.sleep(600000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (i == retryTimes && !Constant.isSucceed()) {
					break;
				}

				Debug.info(ServerSingle.class, "End...");
				SendMail.packageMail(val, "End");
			}

		}

		SendMail.sendMail();
		RecordToFile.close();

	}

	private String ipPort;
	private String[] cmdValues;
	private int retryTimes;

	@Override
	public void init(ParseArgs args) {
		// TODO Auto-generated method stub
		ipPort = args.getIpPort();
		retryTimes = args.getRetryTimesDefault();
		cmdValues = args.getServerCmdList();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		for (String val : cmdValues) {

			SucceedFlag.initServer();
			// 重试次数
			for (int i = 0; i < retryTimes; ++i) {

			}
		}

	}
}
