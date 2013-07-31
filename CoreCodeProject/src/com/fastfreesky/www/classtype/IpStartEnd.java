package com.fastfreesky.www.classtype;

public class IpStartEnd<T> {

	private T ipStart;
	private T ipEnd;

	public T getIpStart() {
		return ipStart;
	}

	public void setIpStart(T ipStart) {
		this.ipStart = ipStart;
	}

	public T getIpEnd() {
		return ipEnd;
	}

	public void setIpEnd(T ipEnd) {
		this.ipEnd = ipEnd;
	}

	@Override
	public String toString() {
		return "IpStartEnd [ipStart=" + ipStart + ", ipEnd=" + ipEnd + "]";
	}

	
}
