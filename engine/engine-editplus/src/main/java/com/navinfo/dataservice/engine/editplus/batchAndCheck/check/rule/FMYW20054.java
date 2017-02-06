package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.plus.model.basic.ChangeLog;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.CheckTest;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
/**
 * FM-YW-20-054
 * 检查条件：
 *     Lifecycle！=1（删除）
 * 检查原则：
 * 名称（name）中仅包含以下符号一个或多个时（不区分全半角），
 * （）［＼］｀｛｝＂”》、“《。°ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫ＝报log：请确认名称正确性。
 * @author zhangxiaoyi
 */
public class FMYW20054 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String nameStr = nameObj.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			String tmpStr=CheckUtil.strQ2B(nameStr);
			//Pattern p = Pattern.compile(".*[()\\[\\\\]'{}\"《》、.°ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫ\\=]+.*");
			Pattern p = Pattern.compile("^[()\\[\\]'\\\\{}\"《》、.°ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫ=]+$");
			Matcher m = p.matcher(tmpStr);
			if(m.matches()){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {}
	
	public static void main(String[] args) throws Exception{
		String tmpStr="\\";
		Pattern p = Pattern.compile("");
		Matcher m = p.matcher(tmpStr);
		System.out.println(m.matches());
	}

}