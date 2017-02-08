package com.navinfo.dataservice.control.row.save;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoiPart;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiParentSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxSamepoiSelector;
import com.navinfo.dataservice.engine.batch.BatchProcess;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiSave {
    private static final Logger logger = Logger.getLogger(PoiSave.class);

    /**
     * @param parameter
     * @param userId
     * @return
     * @throws Exception
     * @zhaokk POI行編保存
     */
    public JSONObject save(String parameter, long userId) throws Exception {

        Connection conn = null;
        JSONObject result = new JSONObject();
        try {

            JSONObject json = JSONObject.fromObject(parameter);

            OperType operType = Enum.valueOf(OperType.class,
                    json.getString("command"));

            ObjType objType = Enum.valueOf(ObjType.class,
                    json.getString("type"));

            int dbId = json.getInt("dbId");

            conn = DBConnector.getInstance().getConnectionById(dbId);

            JSONObject poiData = json.getJSONObject("data");

            if (poiData.size() == 0 && operType == OperType.UPDATE && objType != ObjType.IXSAMEPOI && objType != ObjType.IXPOIPARENT) {
                upatePoiStatus(json.getString("objId"), conn, false);
                JSONArray ret = new JSONArray();
                result.put("log", ret);
                result.put("check", ret);
                return result;
            }

//            StringBuffer buf = new StringBuffer();
//
//            int pid = 0;
//
//            if (operType != OperType.CREATE) {
//                if (objType == ObjType.IXSAMEPOI) {
//                    String poiPids = JsonUtils.getStringValueFromJSONArray(json
//                            .getJSONArray("poiPids"));
//                    buf.append(poiPids);
//                } else {
//                    pid = json.getInt("objId");
//
//                    buf.append(String.valueOf(pid));
//                }
//            } else {
//                pid = result.getInt("pid");
//                buf.append(String.valueOf(pid));
//            }
            EditApiImpl editApiImpl = new EditApiImpl(conn);
            editApiImpl.setToken(userId);
            StringBuffer sb = new StringBuffer();
            int pid = 0;
            // POI同一关系
            if (ObjType.IXSAMEPOI == objType) {
                if (OperType.CREATE == operType) {
                    String poiPids = JsonUtils.getStringValueFromJSONArray(json.getJSONArray("poiPids"));
                    sb.append(poiPids);
                } else if (OperType.UPDATE == operType) {
                    JSONObject data = json.getJSONObject("data");
                    Integer samePid = data.getInt("pid");
                    this.generatePoiPid(sb, samePid, conn);
                } else if (OperType.DELETE == operType) {
                    Integer samePid = json.getInt("objId");
                    this.generatePoiPid(sb, samePid, conn);
                }
                result = editApiImpl.runPoi(json);
                // POI父子关系
            } else if (ObjType.IXPOIPARENT == objType) {
                Integer childPoiPid = json.getInt("objId");
                Integer parentPoiPid = 0;
                if (OperType.CREATE == operType || OperType.UPDATE == operType) {
                    parentPoiPid = json.getInt("parentPid");
                    // 一个父子关系家族中，最多允许3级父子关系存在，大于3级以上，不可制作父子关系,
                    // 判断制作父子关系是否超过三级
	                boolean errorFlag = ParentChildReletion3level(conn, childPoiPid, parentPoiPid);
	                if (!errorFlag){
	                	throw new Exception("父子关系大于3级以上，不可制作父子关系！");
	                }
                } else if (OperType.DELETE == operType) {
                    IxPoiParentSelector selector = new IxPoiParentSelector(conn);
                    List<IRow> parents = selector.loadParentRowsByChildrenId(childPoiPid, true);
                    for (IRow row : parents) {
                        IxPoiParent parent = (IxPoiParent) row;
                        parentPoiPid = parent.getParentPoiPid();
                        break;
                    }
                }
                
                sb.append(childPoiPid).append(",").append(parentPoiPid);
                result = editApiImpl.runPoi(json);
                if (OperType.CREATE != operType) {
                    pid = json.getInt("objId");
                } else {
                    pid = result.getInt("pid");
                }
                // 其他
            } else {
                result = editApiImpl.runPoi(json);
                if (OperType.CREATE != operType) {
                    pid = json.getInt("objId");
                    sb.append(String.valueOf(pid));
                } else {
                    pid = result.getInt("pid");
                    sb.append(String.valueOf(pid));
                }
            }

            if (ObjType.IXSAMEPOI != objType) {
                json.put("objId", pid);
                BatchProcess batchProcess = new BatchProcess("row","save");
                List<String> batchList = batchProcess.getRowRules();
                batchProcess.execute(json, conn, editApiImpl, batchList);
            }
            upatePoiStatus(sb.toString(), conn, true);
            
            if(operType == OperType.UPDATE){
	            editApiImpl.updatePoifreshVerified(pid,"web");
            }

            return result;
        } catch (DataNotChangeException e) {
            DbUtils.rollback(conn);
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            DbUtils.rollback(conn);
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            DbUtils.commitAndClose(conn);
        }
    }

    private void generatePoiPid(StringBuffer sb, Integer samePid, Connection conn) throws Exception {
        IxSamepoiSelector selector = new IxSamepoiSelector(conn);
        IxSamepoi samepoi = (IxSamepoi) selector.loadById(samePid, true);
        int length = samepoi.getParts().size();
        for (int i = 0; i < length; i++) {
            IxSamepoiPart part = (IxSamepoiPart) samepoi.getParts().get(i);
            if (i < length - 1) {
                sb.append(part.getPoiPid()).append(",");
            } else {
                sb.append(part.getPoiPid());
            }
        }
    }

    /**
     * @Title: upatePoiStatus
     * @Description:poi操作修改poi状态为已作业，鲜度信息为0 zhaokk sourceFlag 0 web 1 Android
     * 		(修)(第七迭代) 变更:当新增 poi_edit_status 时,为 commit_his_status 字段赋默认值 0 
     * @param pids
     * @param conn
     * @param flag
     * @throws Exception  void
     * @throws 
     * @author zl zhangli5174@navinfo.com
     * @date 2016年12月8日 下午1:50:56 
     */
    public void upatePoiStatus(String pids, Connection conn, boolean flag) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (flag) {
            sb.append(" MERGE INTO poi_edit_status T1 ");
            sb.append(" USING (SELECT 2 AS b,0 AS C,pid as d FROM ix_poi where pid in ("
                    + pids + ")) T2 ");
            sb.append(" ON ( T1.pid=T2.d) ");
            sb.append(" WHEN MATCHED THEN ");
            sb.append(" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c ");
            sb.append(" WHEN NOT MATCHED THEN ");
            //zl 2016.12.08 新增时为 commit_his_status 字段赋默认值 0 
            sb.append(" INSERT (T1.status,T1.fresh_verified,T1.pid,commit_his_status) VALUES(T2.b,T2.c,T2.d,0)");
        } else {
            sb.append(" UPDATE poi_edit_status T1 SET T1.status = 2 where T1.pid in ("
                    + pids + ")");
        }


        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sb.toString());
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw e;

        } finally {
            DBUtils.closeStatement(pstmt);
        }

    }
    
    /**
     * 判断poiPid的父子关系是否超过三级
     * @param conn 大区库conn
     * @param poiPid 当前poi的pid
     * @param parentPid 当前poi的父pid，如果没有父，传0
     * @return 
     * @throws Exception 
     */
    public boolean ParentChildReletion3level(Connection conn, int poiPid, int parentPid) throws Exception{
    	IxPoiParentSelector poiParentSelector = new IxPoiParentSelector(conn);
    	List<Integer> childPids = poiParentSelector.getChildrenPids(poiPid);
    	List<String> error = new ArrayList<String>();
    	if (childPids.size() != 0){
    		// 遍历每个1级子有没有2级子
    		for (int oneChildPid: childPids){
    			List<Integer> twoChildPids = poiParentSelector.getChildrenPids(oneChildPid);
				// 有二级子
    			if (twoChildPids.size() != 0){
    				if (parentPid != 0){
    					// 有二级子，并且有父，即父子关系为4级，则报错
    					error.add("F");
    				} else{
    					// 遍历每个2级子有没有3级子
    					for (int twoChildPid: twoChildPids){
    						List<Integer> threeChildPids = poiParentSelector.getChildrenPids(twoChildPid);
    						// 有三级子，即父子关系为4级，则报错
    						if (threeChildPids.size() != 0){
    							error.add("F");
    						}
    					}
    				}
    			} else{
    				if (parentPid != 0){
    					int twoParentPid = poiParentSelector.getParentPid(parentPid);
    					if (twoParentPid != 0){
    						// 有一级子，有二级父，父子关系为4级，报错
    						error.add("F");
    					}
    				}
    				
    			}
    		}
    	} else{
    		// 当前poi没有子，但是有3级父，报错
    		if (parentPid != 0){
				int twoParentPid = poiParentSelector.getParentPid(parentPid);
				if (twoParentPid != 0){
					int threeParentPid = poiParentSelector.getParentPid(twoParentPid);
					if (threeParentPid != 0){
						error.add("F");
					}
				}
    		}
    	}
    	
    	if (error.contains("F")){
    		return false;
    	}
    	return true;
    }

}
