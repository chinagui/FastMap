package com.navinfo.dataservice.engine.editplus.utils;

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
	
	/*
	 * 将KIND转成汉字
	 */
	public static String kindToChi(int kind){
		String kindChi="";
		
		switch(kind){
		case 0:kindChi="作业中";break;
		case 1:kindChi="高速道路";break;
		case 2:kindChi="城市高速";break;
		case 3:kindChi="国道";break;
		case 4:kindChi="省道";break;
		case 5:kindChi="预留";break;
		case 6:kindChi="县道";break;
		case 7:kindChi="乡镇村道路";break;
		
		case 8:kindChi="其它道路";break;
		case 9:kindChi="非引导道路";break;
		case 10:kindChi="步行道路";break;
		case 11:kindChi="人渡";break;
		case 13:kindChi="轮渡";break;
		case 15:kindChi="10级路(障碍物)";break;
		}
		
		return kindChi;
	}

}
