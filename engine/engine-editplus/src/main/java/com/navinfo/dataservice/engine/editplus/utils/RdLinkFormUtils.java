package com.navinfo.dataservice.engine.editplus.utils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RdLinkFormUtils {

	public RdLinkFormUtils() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * 将List<RdLinkForm>转换成Set<Integer>，即返回form_of_ways的set集合
	 */
	public static Set<Integer> formToList(List<IRow> rdLinkFormList){
		Set<Integer> formSet=new HashSet<Integer>();
		for(int i=0;i<rdLinkFormList.size();i++){
			formSet.add(((RdLinkForm)rdLinkFormList.get(i)).getFormOfWay());
		}
		return formSet;
	}
	
	/*
	 * 将form_of_ways转成汉字
	 */
	public static String formToChi(int form){
		String formChi="";
		switch(form){
		case 0:formChi="未调查";break;
		case 1:formChi="无属性";break;
		case 2:formChi="其他";break;
		case 10:formChi="IC";break;
		case 11:formChi="JCT";break;
		case 12:formChi="SA";break;
		case 13:formChi="PA";break;
		case 14:formChi="全封闭道路";break;
		case 15:formChi="匝道";break;
		case 16:formChi="跨线天桥(Overpass)";break;
		case 17:formChi="跨线地道(Underpass)";break;
		case 18:formChi="私道";break;
		case 20:formChi="步行街";break;
		case 21:formChi="过街天桥";break;
		case 22:formChi="公交专用道";break;
		case 23:formChi="自行车道";break;
		case 24:formChi="跨线立交桥";break;
		case 30:formChi="桥";break;		
		case 31:formChi="隧道";break;
		case 32:formChi="立交桥";break;
		case 33:formChi="环岛";break;
		case 34:formChi="辅路";break;
		case 35:formChi="调头口(U-Turn)";break;
		case 36:formChi="POI连接路";break;
		case 37:formChi="提右";break;
		case 38:formChi="提左";break;
		case 39:formChi="主辅路出入口";break;
		case 43:formChi="窄道路";break;
		case 48:formChi="主路";break;
		case 49:formChi="侧道";break;
		case 50:formChi="交叉点内道路";break;
		case 51:formChi="未定义交通区域(UTA)";break;
		case 52:formChi="区域内道路";break;
		case 53:formChi="停车场出入口连接路";break;
		case 54:formChi="停车场出入口虚拟连接路";break;
		case 57:formChi="Highway对象外JCT";break;
		case 60:formChi="风景路线";break;
		case 80:formChi="停车位引导道路(Parking Lane)";break;
		case 81:formChi="虚拟调头口";break;
		case 82:formChi="虚拟提左提右";break;
		}
		return formChi;
	}

}
