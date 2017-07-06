package com.navinfo.dataservice.engine.fcc.tips;

import net.sf.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /*
    "采用6位字符表示,每个字符赋值为0/1分别表示无/有,从右到左含义如下：
    赋值如:000011表示斜坡和阶梯;000101表示斜坡和扶梯
    从右边数第1位:斜坡
    从右边数第2位:阶梯
    从右边数第3位:扶梯
    从右边数第4位:直梯
    从右边数第5位:默认为0
    从右边数第6位:其他
    如果所有位均为0,表示未调查"
    */
    public static JSONArray getCrossStreetAccess(String access) {
        char[] accessArray = access.toCharArray();
        JSONArray accessList = new JSONArray();
        boolean isAllZero = true;
        for(int i = 0;i < accessArray.length; i++) {
            if(i == 0 && accessArray[i] == '1') {//第6位
                accessList.add("其他");
                isAllZero = false;
            }else if(i == 1 && accessArray[i] == '1') {//第5位
                accessList.add("不转");
                isAllZero = false;
            }else if(i == 2 && accessArray[i] == '1') {//第4位
                accessList.add("直梯");
                isAllZero = false;
            }else if(i == 3 && accessArray[i] == '1') {//第3位
                accessList.add("扶梯");
                isAllZero = false;
            }else if(i == 4 && accessArray[i] == '1') {//第2位
                accessList.add("阶梯");
                isAllZero = false;
            }else if(i == 5 && accessArray[i] == '1') {//第1位
                accessList.add("斜坡");
                isAllZero = false;
            }
        }
        if(isAllZero) {
            accessList.add("未调查");
        }
        return accessList;
    }

    public static Set<String> getMeshesByGrids(List<Integer> grids) {
        Set<String> meshes = new HashSet<>();
        for(Integer grid : grids) {
            String mesh = String.valueOf(grid).substring(0,6);
            if(!meshes.contains(mesh)) {
                meshes.add(mesh);
            }
        }
        return meshes;
    }

    public static void main(String[] args) {
        List<String> accessList = TipsSelectorUtils.getCrossStreetAccess("000101");
        for(String s : accessList) {
            System.out.println(s);
        }
    }
}
