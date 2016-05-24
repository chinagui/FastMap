package com.navinfo.dataservice.engine.check.model.utils;

public class RdLinkUtils {

	public RdLinkUtils() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * 将IMI_CODE转成汉字
	 */
	public static String imiCodeToChi(int imiCode){
		String imiCodeChi="";
		
		switch(imiCode){
		case 1:imiCodeChi="IMI(交叉点内部道路-II)";break;
		case 2:imiCodeChi="IMI(转弯道-M)";break;
		case 3:imiCodeChi="IMI(无法描述的-I)";break;
		}
		
		return imiCodeChi;
	}

}
