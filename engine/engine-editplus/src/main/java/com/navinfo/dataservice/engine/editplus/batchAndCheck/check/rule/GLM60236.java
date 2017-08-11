package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.vividsolutions.jts.geom.Geometry;
/**
 * GLM60236
 * 检查条件：非删除POI且存在父子关系
 * 重复检查：
 * 一子多父
 * 循环检查：
 * 在一组父子关系中，其中一个POI在这组关系中既充当其它POI的父亲，又充当了其它POI的子，
 * 则认为这组POI存在循环建立父子关系，报LOG：POI存在循环建立父子关系！
 * 例如：一组父子关系为A->B->A，则认为 A->B，与B->A是循环建立了
 * @author zhangxiaoyi
 */
public class GLM60236 extends BasicCheckRule {
	Map<Long, Long> parentMap = new HashMap<Long, Long>();
	//检查结果去重用
	Set<Long> dupPid=new HashSet<Long>();
	
	public void run()throws Exception{
		Map<Long, BasicObj> rows=getRowList();
		loadReferDatas(rows.values());
		Set<Long> pidList=new HashSet<Long>();
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			if(!obj.getMainrow().getOpType().equals(OperationType.PRE_DELETED)){
				pidList.add(obj.objPid());
				try{
					runCheck(obj);
				}catch(Exception e){
					log.warn(e.getMessage(),e);
				}
			}
		}
		if(pidList==null||pidList.size()==0){return;}
		String pids=pidList.toString().replace("[", "").replace("]", "");
		Connection conn = this.getCheckRuleCommand().getConn();
		List<Clob> values=new ArrayList<Clob>();
		String pidString="";
		if(pidList.size()>1000){
			Clob clob=ConnectionUtil.createClob(conn);
			clob.setString(1, pids);
			pidString=" IN (select to_number(column_value) from table(clob_to_table(?)))";
			values.add(clob);
		}else{
			pidString=" IN ("+pids+")";
		}
		String sqlStr="SELECT C1.GROUP_ID       G1,"
				+ "       C1.CHILD_POI_PID  C,"
				+ "       P1.PARENT_POI_PID P1,"
				+ "       P2.GROUP_ID       G2,"
				+ "       P2.PARENT_POI_PID P2"
				+ "  FROM IX_POI_CHILDREN C1,"
				+ "       IX_POI_PARENT   P1,"
				+ "       IX_POI_CHILDREN C2,"
				+ "       IX_POI_PARENT   P2,"
				+ "       IX_POI          P"
				+ " WHERE C1.GROUP_ID = P1.GROUP_ID"
				+ "   AND C2.GROUP_ID = P2.GROUP_ID"
				+ "   AND P.PID = C1.CHILD_POI_PID"
				+ "   AND P1.PARENT_POI_PID > P2.PARENT_POI_PID"
				+ "   AND C1.CHILD_POI_PID = C2.CHILD_POI_PID"
				+ "   AND P.U_RECORD != 2"
				+ "   AND C1.U_RECORD != 2"
				+ "   AND C2.U_RECORD != 2"
				+ "   AND P1.U_RECORD != 2"
				+ "   AND P2.U_RECORD != 2"
				+ "   AND C1.CHILD_POI_PID "+pidString
				+ " UNION"
				+ " SELECT C1.GROUP_ID       G1,"
				+ "       C1.CHILD_POI_PID  C,"
				+ "       P1.PARENT_POI_PID P1,"
				+ "       P2.GROUP_ID       G2,"
				+ "       P2.PARENT_POI_PID P2"
				+ "  FROM IX_POI_CHILDREN C1,"
				+ "       IX_POI_PARENT   P1,"
				+ "       IX_POI_CHILDREN C2,"
				+ "       IX_POI_PARENT   P2,"
				+ "       IX_POI          P"
				+ " WHERE C1.GROUP_ID = P1.GROUP_ID"
				+ "   AND C2.GROUP_ID = P2.GROUP_ID"
				+ "   AND P.PID = C1.CHILD_POI_PID"
				+ "   AND P1.PARENT_POI_PID != P2.PARENT_POI_PID"
				+ "   AND C1.CHILD_POI_PID = C2.CHILD_POI_PID"
				+ "   AND P.U_RECORD != 2"
				+ "   AND C1.U_RECORD != 2"
				+ "   AND C2.U_RECORD != 2"
				+ "   AND P1.U_RECORD != 2"
				+ "   AND P2.U_RECORD != 2"
				+ "   AND P1.PARENT_POI_PID "+pidString;
		PreparedStatement pstmt=conn.prepareStatement(sqlStr);;
		if(values!=null&&values.size()>0){
			for(int i=0;i<values.size();i++){
				pstmt.setClob(i+1,values.get(i));
			}
		}			
		ResultSet rs = pstmt.executeQuery();
		//去重用，若targets重复（不判断顺序，只要pid相同即可），则不重复报。否则报出
		Set<Long> dupPid=new HashSet<Long>();
		while (rs.next()) {
			Long pidTmp1=rs.getLong("C");
			Long pidTmp2=rs.getLong("P1");
			Long pidTmp3=rs.getLong("P2");
			if(!dupPid.contains(pidTmp1)||!dupPid.contains(pidTmp2)||!dupPid.contains(pidTmp3)){
				String targets="[IX_POI,"+pidTmp1+"];[IX_POI,"+pidTmp2+"];[IX_POI,"+pidTmp3+"]";
				setCheckResult("", targets, 0,"POI存在一子多父");
				dupPid.add(pidTmp1);dupPid.add(pidTmp2);dupPid.add(pidTmp3);
			}
		}
	}
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//循环检查
			Long pid=poi.getPid();
			int whileNum=0;
			Set<Long> parentPids=new HashSet<Long>();
			Long pPid=pid;
			//因为一子多父为错误数据，此处不单独考虑一子多父，默认均为一子一父
			while(parentMap.containsKey(pPid)){
				//一般父子关系层次会在6层以内，增加判断，防止代码错误导致的死循环
				if(whileNum>6){return;}
				else{whileNum++;}
				pPid=parentMap.get(pPid);
				parentPids.add(pPid);
				//递归向上查找父，并将找到的父poi集合放到parentPids中，若某次查到的pid在集合parentPids中，说明出现循环；否则将pid加入parentPids
				if(parentPids.contains(pid)){
					//判断是否已经报过error了
					boolean isError=false;
					for(Long tmp:parentPids){
						if(!dupPid.contains(tmp)){isError=true;dupPid.add(tmp);}
					}
					//去重用，若targets重复（不判断顺序，只要pid相同即可），则不重复报。否则报出
					if(isError){
						String targets=parentPids.toString().replace("[", "(").replace(",", "];[IX_POI,").replace("(", "[IX_POI,").replace(" ", "");
						log.info(targets);
						setCheckResult(poi.getGeometry(), targets,poi.getMeshId(),"POI存在循环建立父子关系");
						return;
					}
				}
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		log.info("start loadRefer");
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			IxPoiObj poiObj=(IxPoiObj) obj;
			List<IxPoiChildren> childs = poiObj.getIxPoiChildrens();
			if(childs==null||childs.size()==0){continue;}
			for(IxPoiChildren c:childs){pidList.add(c.getChildPoiPid());}
		}
		parentMap = IxPoiSelector.getAllParentChildByPids(getCheckRuleCommand().getConn(), pidList);
		log.info("end loadRefer");
	}

}
