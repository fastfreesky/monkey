package com.ccindex.record;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @ClassName: RecordCmd
 * @Description: TODO(这里用一句话描述这个类的作用)命令记录类别,用于记录每条指令的日志记录
 * @author tianyu.yang
 * @date 2013-4-12 上午10:33:28
 * 
 */
public class RecordToFile {

	private static int seqNum = 0;

	private static DateFormat fm = new SimpleDateFormat("yyyyMMddHHmmss");
	private static DecimalFormat df = new DecimalFormat("0000");

	// 生成命令唯一编码,用年月日时分秒,仿造hadoop格式job_年月日时分秒_顺序增加序列号如(job_201304121611_0001)
	private static String jobId;
	// 文件存放路径
	private static String jobPath;
	// 文件句柄
	private static File fileHandle;
	// 文件写入句柄
	private static FileWriter fileWriter;

	public static void init(String path) throws IOException {
		jobPath = path;
		makeJobId();

		String fileName = jobPath + "/" + jobId;
		fileHandle = new File(fileName);

		if (!fileHandle.exists()) {
			fileHandle.createNewFile();
		}

		fileWriter = new FileWriter(fileHandle, true);

	}

	public static void close() {

		try {
			System.out.println("If you want to Look log, Please Look JobId: "
					+ jobPath + "/" + jobId);
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void makeJobId() {
		Date start = new Date(System.currentTimeMillis());
		String date = fm.format(start);
		// System.out.
		seqNum++;

		jobId = "job_" + date + "_" + df.format(seqNum);
	}

	// 创建以jobId为名字的文件进行写入操作,记录该命令的所有操作
	public static void record(String values) {
		if (fileWriter != null) {
			try {
				fileWriter.write(values + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		try {
			init("D:\\Application\\monkey\\state");
			record("Test value");
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
