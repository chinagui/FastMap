package com.navinfo.dataservice.dao.fcc;

import java.util.ArrayList;
import java.util.List;

/** 
 * @ClassName: SolrQueryUtils.java
 * @author y
 * @date 2017-1-18 下午5:29:50
 * @Description: solr查询公共类-一些公共的东西放在这
 *  
 */
public class SolrQueryUtils {
	
    private static List<String> notDisplayTipTpye=new ArrayList<String>();
	
	public static String NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL="";
	
	static{
		
		notDisplayTipTpye.add("1108");//减速带
		notDisplayTipTpye.add("1110");// 卡车限制
		notDisplayTipTpye.add("1113");//车道限速
		notDisplayTipTpye.add("1114");//卡车限速
		notDisplayTipTpye.add("1115");//车道变化点
		notDisplayTipTpye.add("1204");//可逆车道
		notDisplayTipTpye.add("1303");//卡车交限
		notDisplayTipTpye.add("1306");//路口语音引导
		notDisplayTipTpye.add("1307");//自然语音引导
		notDisplayTipTpye.add("1308");//禁止卡车驶入
		notDisplayTipTpye.add("1310");//公交车道
		notDisplayTipTpye.add("1311");//可变导向车道
		notDisplayTipTpye.add("1401");//方向看板
		notDisplayTipTpye.add("1402");//Real Sign
		notDisplayTipTpye.add("1406");//实景图
		notDisplayTipTpye.add("1409");//普通路口模式图
		notDisplayTipTpye.add("1513");//窄道
		notDisplayTipTpye.add("1707");//里程桩
		
		if(notDisplayTipTpye.size()!=0){
			
			StringBuilder builder= new StringBuilder();
			builder.append("-s_sourceType:(");
			
			if (notDisplayTipTpye.size() > 0) {

				for (int i = 0; i < notDisplayTipTpye.size(); i++) {
					String type = notDisplayTipTpye.get(i);
					if (i > 0) {
						builder.append(" ");
					}
					builder.append(type);
				}
				builder.append(")");
			}
			
			NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL=builder.toString();
			
		}
	}
	

}
