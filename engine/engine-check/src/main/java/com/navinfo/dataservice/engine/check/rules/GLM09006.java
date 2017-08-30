package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.rdlinkwarning.RdLinkWarning;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: GLM09006
 * @author songdongyan
 * @date 2016年12月29日
 * @Description: 相同点号和线号登记的警示信息不能超过6个，否则报错
 * @alter fhx <同一关联link，同一作用方向，且geometry相同的警示信息，不能超过6个，否则报错>
 * 新增警示信息，服务端前检查
 */
public class GLM09006 extends baseRule{

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		
		for(IRow obj:checkCommand.getGlmList()){
			//新增警示信息
			if(obj instanceof RdLinkWarning){
				RdLinkWarning rdWarninginfo=(RdLinkWarning) obj;
				
				checkRdWarningInfo(rdWarninginfo);
			}
		}
		
	}

	/**
	 * @param rdWarninginfo
	 * @throws Exception 
	 */
	private void checkRdWarningInfo(RdLinkWarning linkwarning) throws Exception {
		Geometry geo = GeoTranslator.transform(linkwarning.getGeometry(), GeoTranslator.dPrecisionMap, 5);
		
		Coordinate coor = geo.getCoordinate();
		
		//新增警示信息
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT 1 FROM RD_LINK_WARNING RW");
		sb.append(" WHERE RW.LINK_PID = " + linkwarning.getLinkPid());
		sb.append(" AND RW.GEOMETRY.SDO_POINT.X = " + coor.x);
		sb.append(" AND RW.GEOMETRY.SDO_POINT.Y = " + coor.y);
		sb.append(" AND RW.DIRECT = " + linkwarning.getDirect());
		sb.append(" AND RW.U_RECORD <> 2");
		sb.append(" HAVING COUNT(1) >= 6");

		String sql = sb.toString();
		log.info("RdWarninginfo前检查GLM09006:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			this.setCheckResult("", "", 0);
		}
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
