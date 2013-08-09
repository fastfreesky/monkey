package com.ccindex.tool;


public class ObjectFactory {
	
	
	public static Object newInstance(String objectClass){
		try {
			Class pclass = Class.forName(objectClass);
			return pclass.newInstance(); 
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null ;
	}
}
