package com.fastfreesky.www.tool.ip;

import com.fastfreesky.www.classtype.IpValue;
import com.fastfreesky.www.generics.FindValueInArea;
import com.fastfreesky.www.tool.find.SearchAlgorithm;

public class ParseIp extends FindValueInArea<Long, IpValue<String>> {

	@Override
	protected void initKSize(int length) {
		// TODO Auto-generated method stub
		keyArrayStart = new Long[length];
		keyArrayEnd = new Long[length];
	}

	@Override
	public IpValue<String> get(Long key) {
		// TODO Auto-generated method stub
		int stautus = SearchAlgorithm.findIpInArea(keyArrayStart,
				keyArrayEnd, key);
		if (stautus == -1) {
			return null;
		} else {
			return hashMapIp.get(keyArrayStart[stautus]);
		}
	}

	public IpValue<String> getIp(Long key) {
		return get(key);
	}

	public IpValue<String> getIp(String ip) {
		int stautus = SearchAlgorithm.findIpInArea(keyArrayStart,
				keyArrayEnd, Ip.ipToLong(ip));
		if (stautus == -1) {
			return null;
		} else {
			return hashMapIp.get(keyArrayStart[stautus]);
		}
	}

	public static void main(String[] args) {
		ParseIp ip = new ParseIp();

		IpValue<String> ipva = new IpValue<String>();
		ipva.setProvince("dddd");
		ip.addData(123l, 145l, ipva);

		IpValue<String> ipvb = new IpValue<String>();
		ipvb.setProvince("eeee");
		ip.addData(1234l, 1236l, ipvb);

		IpValue<String> ipvc = new IpValue<String>();
		ipvc.setProvince("ffff");
		ip.addData(12345l, 123567l, ipvc);

		ip.readey();

		System.out.println(ip.getIp(100l));
		System.out.println(ip.getIp(123l).getProvince());
		System.out.println(ip.getIp(134l).getProvince());
		System.out.println(ip.getIp(145l).getProvince());
		System.out.println(ip.getIp(148l));

		System.out.println(ip.getIp(1100l));
		System.out.println(ip.getIp(1234l).getProvince());
		System.out.println(ip.getIp(1235l).getProvince());
		System.out.println(ip.getIp(1236l).getProvince());
		System.out.println(ip.getIp(1239l));

		System.out.println(ip.getIp(12342l));
		System.out.println(ip.getIp(12345l).getProvince());
		System.out.println(ip.getIp(123487l).getProvince());
		System.out.println(ip.getIp(123567l).getProvince());
		System.out.println(ip.getIp(1235670l));
	}

}
