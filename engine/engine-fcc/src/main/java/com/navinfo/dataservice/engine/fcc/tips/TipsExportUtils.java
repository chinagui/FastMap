package com.navinfo.dataservice.engine.fcc.tips;

import java.util.HashMap;
import java.util.Map;

/** 
 * @ClassName: TipsExportUtils.java
 * @author y
 * @date 2017-1-14 上午10:43:57
 * @Description: TODO
 *  
 */
public class TipsExportUtils {
	
	
	static Map<String,Integer> downloadOriginalPhotoTips=new HashMap<String,Integer>(); //到处时需要导出原图的tips类型配置
	
	static {
		
		downloadOriginalPhotoTips.put("1302", 1);
		downloadOriginalPhotoTips.put("1303", 1);
		downloadOriginalPhotoTips.put("1405", 1);
		downloadOriginalPhotoTips.put("1516", 1);
		downloadOriginalPhotoTips.put("1406", 1);
		downloadOriginalPhotoTips.put("1401", 1);
		downloadOriginalPhotoTips.put("1110", 1);
		downloadOriginalPhotoTips.put("1402", 1);
		downloadOriginalPhotoTips.put("1406", 1);
		downloadOriginalPhotoTips.put("2102", 1);

	}

}
