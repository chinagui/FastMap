package com.navinfo.dataservice.engine.fcc.tips;

public class TipsSelectorUtils {
	
	public static String convertElecEyeKind(int kind){
		
		switch(kind){
		
		case 1:return "限速摄像头";
		case 10:return "交通信号灯摄像头";
		case 12:return "单行线摄像头";
		case 13:return "非机动车道摄像头";
		case 14:return "出入口摄像头";
		case 15:return "公交车道摄像头";
		case 16:return "禁止左/右转摄像头";
		case 18:return "应急车道摄像头";
		case 19:return "交通标线摄像头";
		case 20:return "区间测速开始";
		case 21:return "区间测速结束";
		case 22:return "违章停车摄像头";
		case 23 :return "限行限号摄像头";
		case 98:return "其他";
		}
		
		return null;
	}
	
	

	public static String convertElecEyeLocation(int loc){
		switch(loc){
		case 0:return "未调查";
		case 1:return "左";
		case 2:return "右";
		case 4:return "上";
		}
		
		return null;
	}
	
	public static String convertUsageFeeType(int tp){
		switch(tp){
		case 2:return "桥";
		case 3:return "隧道";
		}
		return null;
	}
	
	public static String convertUsageFeeVehicleType(int vt){
		switch(vt){
		case 1:return "客车";
		case 2:return "配送卡车";
		case 3:return "运输卡车";
		case 5:return "出租车";
		case 6:return "公交车";
		}
		
		return null;
	}
}
