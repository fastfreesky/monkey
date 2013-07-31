package com.fastfreesky.www.classtype;

/**
 * 
 * @ClassName: IpValue
 * @Description: TODO(这里用一句话描述这个类的作用)通过IP解析出的值
 * @author tianyu.yang
 * @date 2013-7-30 上午11:45:23
 * 
 */
public class IpValue<T> {

	private T province;
	private T city;
	private T area;
	private T isp;

	public T getProvince() {
		return province;
	}

	public void setProvince(T province) {
		this.province = province;
	}

	public T getCity() {
		return city;
	}

	public void setCity(T city) {
		this.city = city;
	}

	public T getArea() {
		return area;
	}

	public void setArea(T area) {
		this.area = area;
	}

	public T getIsp() {
		return isp;
	}

	public void setIsp(T isp) {
		this.isp = isp;
	}

}
