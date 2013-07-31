package com.fastfreesky.www.tool.ip;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.fastfreesky.www.classtype.IpStartEnd;
import com.fastfreesky.www.tool.bytes.ByteUtils;

public class Ip {
	/**
	 * 
	 * @Title: ipToLong
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 *               将127.0.0.1形式的IP地址转换成十进制整数，这里没有进行任何错误处理
	 * @param strIp
	 * @return long
	 * @throws
	 */
	public static long ipToLong(String strIp) {
		// 将每个.之间的字符串转换成整型
		long[] ip = new long[4];

		try {
			// 先找到IP地址字符串中.的位置
			int position1 = strIp.indexOf(".");
			int position2 = strIp.indexOf(".", position1 + 1);
			int position3 = strIp.indexOf(".", position2 + 1);

			ip[0] = Long.parseLong(strIp.substring(0, position1).trim());
			ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2)
					.trim());
			ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3)
					.trim());
			ip[3] = Long.parseLong(strIp.substring(position3 + 1).trim());

		} catch (NumberFormatException e) {
			e.printStackTrace();
			return -1;
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return -1;
		}
		return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
	}

	/**
	 * 
	 * @Title: longToIP
	 * @Description: TODO(这里用一句话描述这个方法的作用)将十进制整数形式转换成127.0.0.1形式的ip地址
	 * @param longIp
	 * @return String
	 * @throws
	 */
	public static String longToIP(long longIp) {
		StringBuffer sb = new StringBuffer("");
		// 直接右移24位
		sb.append(String.valueOf((longIp >>> 24)));
		sb.append(".");
		// 将高8位置0，然后右移16位
		sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
		sb.append(".");
		// 将高16位置0，然后右移8位
		sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
		sb.append(".");
		// 将高24位置0
		sb.append(String.valueOf((longIp & 0x000000FF)));
		return sb.toString();
	}

	/**
	 * 
	 * @Title: ipChangeToClassL
	 * @Description: TODO(这里用一句话描述这个方法的作用)将掩码IP<127.0.0.1/24>转换为IP段的类
	 * @param ip
	 * @return IpStartEnd<Long>
	 * @throws
	 */
	public static IpStartEnd<Long> ipChangeToClassL(String ip) {

		String[] ips = ip.trim().split("/");
		if (ips.length != 2) {
			System.out.println("Error Ip Format <127.0.0.1/24>");
			return null;
		}

		IpStartEnd<Long> ipStartendL = new IpStartEnd<Long>();
		int ysize = Integer.parseInt(ips[1].trim());
		int size = (32 - ysize);

		ipStartendL.setIpStart((ipToLong(ips[0]) >> size) << size);
		ipStartendL.setIpEnd(ipToLong(ips[0]) | (ByteUtils.getAllBitOne(size)));

		return ipStartendL;
	}

	/**
	 * 
	 * @Title: ipChangeToClassL
	 * @Description: TODO(这里用一句话描述这个方法的作用)将起始IP段终止IP段转化为Long型
	 * @param startIp
	 * @param endIp
	 * @return IpStartEnd<Long>
	 * @throws
	 */
	public static IpStartEnd<Long> ipChangeToClassL(String startIp, String endIp) {

		IpStartEnd<Long> ipStartendL = new IpStartEnd<Long>();
		ipStartendL.setIpStart(ipToLong(startIp.trim()));
		ipStartendL.setIpEnd(ipToLong(endIp.trim()));

		return ipStartendL;
	}

	/**
	 * 
	 * @Title: ipStartEndChangeToIp
	 * @Description: TODO(这里用一句话描述这个方法的作用)将点位法的IP段,转化为掩码方式(<1.195.192.0/18>)
	 * @param ipStart
	 * @param ipEnd
	 * @return String
	 * @throws
	 */
	public static String ipStartEndChangeToIp(String ipStart, String ipEnd) {
		long sip = ipToLong(ipStart.trim());
		long eip = ipToLong(ipEnd.trim());

		int min_one_size = ByteUtils.getMinOneSize(sip);
		int min_zero_size = ByteUtils.getMinZeroSize(eip);

		int size = Math.min(min_one_size, min_zero_size) - 1;
		long ack = 32 - size;
		return longToIP(sip) + "/" + ack;
	}

	/**
	 * 
	 * @Title: ipMaskToIpLongFromFile
	 * @Description: TODO(这里用一句话描述这个方法的作用)将IP掩码格式转化为<起始IPLong, 终止IPLong, 省,
	 *               城市,地区,运营商...类型字段,分割符为\t>
	 * @param fileIn 输入文件
	 * @param fileOut	输出文件
	 * @param isAdd	是否为追加写
	 * @param args 其他新增字段
	 * @return boolean
	 * @throws
	 */
	public static boolean ipMaskToIpLongFromFile(String fileIn, String fileOut,
			boolean isAdd, String... args) {

		String addMsg = "";
		if (args.length != 0) {
			for (int i = 0; i < args.length; ++i) {
				if (i == args.length - 1) {
					addMsg += args[i];
				} else {
					addMsg += args[i] + "\t";
				}
			}
		}

		File filein = new File(fileIn);
		if (!(filein.exists() && filein.isFile())) {
			System.out.println("Input file is not exists or not file");
			return false;
		}
		File fileout = new File(fileOut);
		if (!fileout.exists()) {
			new File(fileout.getParent()).mkdirs();
		}
		BufferedReader read = null;
		OutputStreamWriter write = null;
		try {

			InputStreamReader reader = new InputStreamReader(
					new FileInputStream(filein), "utf-8");
			read = new BufferedReader(reader);

			write = new OutputStreamWriter(
					new FileOutputStream(fileout, isAdd), "utf-8");

			String line = null;
			IpStartEnd<Long> ip;
			StringBuffer buf = new StringBuffer();
			while ((line = read.readLine()) != null) {

				ip = ipChangeToClassL(line.trim());
				if (ip == null) {
					continue;
				}
				buf.setLength(0);

				buf.append(ip.getIpStart()).append("\t").append(ip.getIpEnd());
				if (args.length != 0) {
					buf.append("\t").append(addMsg);
				}
				buf.append("\n");

				write.append(buf.toString());
			}

			System.out.println("Ok to write :" + fileOut);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			try {
				if (read != null) {
					read.close();
				}

				if (write != null) {
					write.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return true;
	}

	public static void main(String[] args) {
		// IpStartEnd<Long> ipStartEnd = ipChangeToClassL("1.197.0.0 /20");
		// System.out.println(ipStartEnd.toString());
		// System.out.println(longToIP(29605888));
		// System.out.println(longToIP(29622271));
		// System.out.println(ipStartEndChangeToIp("1.195.192.0  ",
		// "1.195.255.255  "));
		String fileinall = "D:\\Application\\henan.txt";
		String fileoutall = "D:\\Application\\out\\henan.20130730.out";
		ipMaskToIpLongFromFile(fileinall, fileoutall, false, "河南", "-", "-",
				"电信");

		String fileout = "D:\\Application\\out\\henan.details.20130730.out";

		String filein = "D:\\Application\\anyang.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "安阳", "-", "电信");

		filein = "D:\\Application\\hebi.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "鹤壁", "-", "电信");

		filein = "D:\\Application\\jiyuan.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "济源", "-", "电信");

		filein = "D:\\Application\\jiaozuo.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "焦作", "-", "电信");

		filein = "D:\\Application\\kaifeng.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "开封", "-", "电信");

		filein = "D:\\Application\\luoyang.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "洛阳", "-", "电信");

		filein = "D:\\Application\\luohe.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "漯河", "-", "电信");

		filein = "D:\\Application\\nanyang.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "南阳", "-", "电信");

		filein = "D:\\Application\\pingdingshan.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "平顶山", "-", "电信");

		filein = "D:\\Application\\puyang.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "濮阳", "-", "电信");

		filein = "D:\\Application\\sanmenxia.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "三门峡", "-", "电信");

		filein = "D:\\Application\\shangqiu.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "商丘", "-", "电信");

		filein = "D:\\Application\\xinxiang.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "新乡", "-", "电信");

		filein = "D:\\Application\\xinyang.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "信阳", "-", "电信");

		filein = "D:\\Application\\xuchang.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "许昌", "-", "电信");

		filein = "D:\\Application\\zhengzhou.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "郑州", "-", "电信");

		filein = "D:\\Application\\zhoukou.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "周口", "-", "电信");

		filein = "D:\\Application\\zhumadian.txt";
		ipMaskToIpLongFromFile(filein, fileout, true, "河南", "驻马店", "-", "电信");

	}
}
