package com.navinfo.dataservice.control.row.save;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoiPart;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiParentSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxSamepoiPartSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxSamepoiSelector;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.control.row.batch.BatchProcess;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;
import com.navinfo.navicommons.database.sql.DBUtils;

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
        JSONObject result = null;
        try {

            JSONObject json = JSONObject.fromObject(parameter);

            OperType operType = Enum.valueOf(OperType.class,
                    json.getString("command"));

            ObjType objType = Enum.valueOf(ObjType.class,
                    json.getString("type"));

            int dbId = json.getInt("dbId");

            conn = DBConnector.getInstance().getConnectionById(dbId);

            JSONObject poiData = json.getJSONObject("data");

            if (poiData.size() == 0 && operType == OperType.UPDATE && objType != ObjType.IXSAMEPOI) {
                upatePoiStatus(json.getString("objId"), conn, false);
                return result;
            }

            EditApiImpl editApiImpl = new EditApiImpl(conn);

            editApiImpl.setToken(userId);

            result = editApiImpl.runPoi(json);

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
            StringBuffer sb = new StringBuffer();
            int pid = 0;
            // POI同一关系
            if (ObjType.IXSAMEPOI.equals(objType)) {
                if (OperType.CREATE.equals(operType)) {
                    String poiPids = JsonUtils.getStringValueFromJSONArray(json.getJSONArray("poiPids"));
                    sb.append(poiPids);
                } else if (OperType.UPDATE.equals(operType)) {
                    JSONObject data = json.getJSONObject("data");
                    Integer samePid = data.getInt("pid");
                    this.generatePoiPid(sb, samePid, conn);
                } else if (OperType.DELETE.equals(operType)) {
                    Integer samePid = json.getInt("objId");
                    this.generatePoiPid(sb, samePid, conn);
                }
            // POI父子关系
            } else if (ObjType.IXPOIPARENT.equals(objType)) {
                Integer childPoiPid = json.getInt("objId");
                Integer parentPoiPid = 0;
                if (OperType.CREATE.equals(operType) || OperType.UPDATE.equals(operType)) {
                    parentPoiPid = json.getInt("parentPid");
                } else if (OperType.DELETE.equals(operType)) {
                    IxPoiParentSelector selector = new IxPoiParentSelector(conn);
                    List<IRow> parents = selector.loadParentRowsByChildrenId(childPoiPid, true);
                    for (IRow row : parents) {
                        IxPoiParent parent = (IxPoiParent) row;
                        parentPoiPid = parent.getParentPoiPid();
                        break;
                    }
                }
                sb.append(childPoiPid).append(",").append(parentPoiPid);
            // 其他
            } else {
                if (!OperType.CREATE.equals(operType)) {
                    pid = json.getInt("objId");
                    sb.append(String.valueOf(pid));
                } else {
                    pid = result.getInt("pid");
                    sb.append(String.valueOf(pid));
                }
            }

            if (operType == OperType.UPDATE) {
                json.put("objId", pid);
                BatchProcess batchProcess = new BatchProcess();
                batchProcess.execute(json, conn, editApiImpl);
            }

            upatePoiStatus(sb.toString(), conn, true);

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
     * poi操作修改poi状态为已作业，鲜度信息为0 zhaokk sourceFlag 0 web 1 Android
     *
     * @param pids
     * @throws Exception
     */
    public void upatePoiStatus(String pids, Connection conn, boolean flag) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (flag) {
            sb.append(" MERGE INTO poi_edit_status T1 ");
            sb.append(" USING (SELECT row_id as a , 2 AS b,0 AS C FROM ix_poi where pid in ("
                    + pids + ")) T2 ");
            sb.append(" ON ( T1.row_id=T2.a) ");
            sb.append(" WHEN MATCHED THEN ");
            sb.append(" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c ");
            sb.append(" WHEN NOT MATCHED THEN ");
            sb.append(" INSERT (T1.row_id,T1.status,T1.fresh_verified) VALUES(T2.a,T2.b,T2.c)");
        } else {
            sb.append(" UPDATE poi_edit_status T1 SET T1.status = 2 where T1.row_id = ");
            sb.append(" (SELECT row_id as a FROM ix_poi where pid = " + pids + ")");
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

}
