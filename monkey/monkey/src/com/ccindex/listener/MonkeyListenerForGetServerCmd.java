package com.ccindex.listener;

import com.ccindex.constant.Debug;

public class MonkeyListenerForGetServerCmd implements
MonkeyListener<byte[]> {

	public String value;
	public volatile boolean flagEnd = false;
	
	@Override
	public boolean exists(byte[] t) {
		// TODO Auto-generated method stub
		Debug.info(getClass(), "get Cmd OK  "+new String(t));
		
		setValue(new String(t));

		flagEnd = true;
		return false;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
