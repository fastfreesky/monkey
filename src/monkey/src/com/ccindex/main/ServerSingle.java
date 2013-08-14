package com.ccindex.main;

import java.io.IOException;

import com.ccindex.interfaceI.MonkeyMainI;
import com.ccindex.record.RecordToFile;
import com.ccindex.tool.ParseArgs;
import com.ccindex.tool.ParseCmd;
import com.ccindex.warn.MonkeyOut;
import com.ccindex.warn.SendMail;
import com.ccindex.warn.SendMsg;
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

	private String ipPort;
	private String[] cmdValuesList;
	private String cmdValues;
	private int retryTimes;
	private ParseCmd parseCmd;
	private int timeout;
	private ParseArgs args;
	private String msg;

	@Override
	public void init(ParseArgs args) {
		// TODO Auto-generated method stub
		this.args = args;
		ipPort = args.getIpPort();
		retryTimes = args.getRetryTimesDefault();
		timeout = args.getTimeout();
		cmdValuesList = args.getServerCmdList();
		cmdValues = args.getServerCmd();
		msg = args.getMsg();
	}

	@Override
	public void run() throws Exception {
		// TODO Auto-generated method stub

		parseCmd = new ParseCmd();

		SendMsg.setMsg(msg);
		SendMail.packageTitleAll("Task :", cmdValues);
		RecordToFile.init(args.getRecordDir());

		for (String val : cmdValuesList) {

			// 任务初始化不成功,直接退出,不用进行任务初始化
			parseCmd.initInputCmdServer(val);
			parseCmd.setRetryTimesInline(args.getRetrytimesInline());
			parseCmd.setTimeoutInline(args.getTimeoutInline());

			SendMail.setTitle(val);
			SendMail.packageMail(SendMail.getTitle(), "Begin");

			// 重试次数
			for (int i = 0; i < retryTimes; ++i) {
				try {
					new MonkeyServerSingle(ipPort, parseCmd).run();

				} catch (Exception e) {
					
					e.printStackTrace();
					
					if (i == retryTimes - 1) {
						SendMail.packageMail(SendMail.getTitle(), e.toString());
						SendMail.packageMail(SendMail.getTitle(), "End");
						break;
					} else {
						SendMail.packageMail(SendMail.getTitle(), e.toString());
						retryAgain(i);
						continue;
					}
					
				}

				// 如果执行成功,则跳出循环
				if (parseCmd.isSucceed()) {
					break;
				} else {
					// 重试时候,进行任务重新组装
					retryAgain(i);
				}

			}
			// 尝试次数到了,仍然没有成功
			if (!parseCmd.isSucceed()) {
				break;
			}

		}

		SendMsg.setStatus(parseCmd.isSucceed());
		SendMsg.sendMsg();

		MonkeyOut.info(getClass(), "End...");
		SendMail.packageMail(SendMail.getTitle(), "End");

		SendMail.sendMail();
		RecordToFile.close();
	}

	private void retryAgain(int nowCount) {
		// 重试时候,进行任务重新组装
		String valT = parseCmd.packageRetryTask(parseCmd.getKernelCmd());
		if (valT != null) {
			try {
				parseCmd.initInputCmdServer(valT);

				MonkeyOut.info(getClass(), "Retry Times [" + (nowCount + 1)
						+ "]" + "Retry msg: " + valT);
				SendMail.packageMail(SendMail.getTitle(), "Retry Times ["
						+ (nowCount + 1) + "]" + "Retry msg: " + valT);

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		try {
			// 10min
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
