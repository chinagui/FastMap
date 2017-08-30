package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: GLM09015
 * @author songdongyan
 * @date 2017年3月22日
 * @Description: 警示信息中的点形态不能是图廓点，否则报错。
 * NODE属性编辑后检查
 */
public class GLM09015 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow row:checkCommand.getGlmList()){
			//道路限制信息编辑
			if (row instanceof RdNodeForm){
				RdNodeForm RdNodeForm = (RdNodeForm) row;
				this.checkRdNodeForm(RdNodeForm);
			}
		}
		
	}

	/**
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private void checkRdNodeForm(RdNodeForm rdNodeForm) throws Exception {
		// TODO Auto-generated method stub
		int formOfWay = 0;
		if(rdNodeForm.status().equals(ObjStatus.INSERT)){
			formOfWay = rdNodeForm.getFormOfWay();
		}
		else if (rdNodeForm.status().equals(ObjStatus.UPDATE)){
			if(rdNodeForm.changedFields().containsKey("formOfWay")){
				formOfWay = Integer.parseInt(rdNodeForm.changedFields().get("formOfWay").toString());
			}
		}
		
		int nodepid = rdNodeForm.getNodePid();
		
		RdNodeSelector selector = new RdNodeSelector(this.getConn());
		
		RdNode node = (RdNode)selector.loadById(nodepid, true);
		
		Geometry geo = GeoTranslator.transform(node.getGeometry(), GeoTranslator.dPrecisionMap, 5);
		
		Coordinate coor = geo.getCoordinate();
		
		if(formOfWay == 2){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK_WARNING W ");
			sb.append(" WHERE W.U_RECORD <> 2");
			sb.append(" AND W.GEOMETRY.SDO_POINT.X = " + coor.x);
			sb.append(" AND W.GEOMETRY.SDO_POINT.Y = " + coor.y);
			
			String sql = sb.toString();
			log.info("RdLinkWarning GLM09015 sql:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
	        if(!resultList.isEmpty()){
				String target = "[RD_NODE," + rdNodeForm.getNodePid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

}
