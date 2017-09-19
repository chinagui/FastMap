package com.navinfo.dataservice.scripts.tmp.Tips;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.upload.stat.UploadCrossRegionInfoDao;
import com.navinfo.dataservice.bizcommons.upload.stat.UploadRegionInfoOperator;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.*;
import java.util.*;

/**
 * @ClassName: TipsRegionTmpBatchScript.java
 * @date 2017-9-15 下午1:50:13
 * @Description：跨大区Tips批处理
 * 脚本背景：子任务上传Tips进行跨大区批处理统计，统计功能发布之前的数据需要进行批处理
 * 记录在UPLOAD_CROSS_REGION_INFO,同时跨大区的Tips数据任务信息都批成0，无任务
 */
public class TipsRegionTmpBatchScript {

	private static final Logger log = Logger.getLogger(TipsRegionTmpBatchScript.class);
    private String tableName = HBaseConstant.tipTab;
    private Map<Integer, TaskUserRegion> taskRegionMap = new HashMap<>();
	private Map<String, Set<String>> gridIdMap = new HashMap<String, Set<String>>();
    private Set<String> meshSet = new HashSet<>();

    private String getSubTaskString() throws Exception {
       StringBuffer sf = new StringBuffer();
        Connection tipsConn=null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            tipsConn = DBConnector.getInstance().getTipsIdxConnection();
            String sql = "SELECT S_QSUBTASKID SUBTASKID\n" +
                    "  FROM TIPS_INDEX T\n" +
                    " WHERE T.STAGE = 1\n" +
                    "   AND T.T_TIPSTATUS = 2\n" +
                    "   AND T.S_QSUBTASKID != 0\n" +
                    "UNION\n" +
                    "SELECT S_MSUBTASKID SUBTASKID\n" +
                    "  FROM TIPS_INDEX T\n" +
                    " WHERE T.STAGE = 1\n" +
                    "   AND T.T_TIPSTATUS = 2\n" +
                    "   AND T.S_MSUBTASKID != 0";
            pst = tipsConn.prepareStatement(sql);
            rs = pst.executeQuery();
            rs.setFetchSize(5000);
            int index = 0;
            while (rs.next()) {
                int subTaskId = rs.getInt("SUBTASKID");
                if(index == 0){
                    sf.append(subTaskId);
                }else{
                    sf.append("," + subTaskId);
                }
                index ++;
            }
            log.debug("getSubTaskSet" +  index);
        }catch (SQLException e) {
            log.error("query TipsRegionTmpBatchScript to batch erro..." + e.getMessage(), e);
            throw new SQLException("query TipsRegionTmpBatchScript to batch erro..."  + e.getMessage(),
                    e);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(pst);
            DbUtils.closeQuietly(tipsConn);
        }
        return sf.toString();
    }

    private void getSubTaskRegion() throws Exception{
        String subTaskIdString = this.getSubTaskString();
        Connection manConn=null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            manConn = DBConnector.getInstance().getManConnection();
            String sql = " SELECT SUB.SUBTASK_ID, SUB.EXE_USER_ID, T.REGION_ID\n" +
                    "   FROM SUBTASK SUB, TASK T\n" +
                    "  WHERE SUB.TASK_ID = T.TASK_ID\n" +
                    "    AND SUB.SUBTASK_ID IN\n" +
                    "        (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?)))\n" +
                    "  GROUP BY SUB.SUBTASK_ID,SUB.EXE_USER_ID, T.REGION_ID";

            pst = manConn.prepareStatement(sql);
            Clob clob=ConnectionUtil.createClob(manConn, subTaskIdString);
            pst.setClob(1, clob);

            rs = pst.executeQuery();
            rs.setFetchSize(5000);
            while (rs.next()) {
                int subTaskId = rs.getInt("SUBTASK_ID");
                int userId = rs.getInt("EXE_USER_ID");
                int regionId = rs.getInt("REGION_ID");
                TaskUserRegion tur = new TaskUserRegion();
                tur.setUserId(userId);
                tur.setRegionId(regionId);
                taskRegionMap.put(subTaskId, tur);
            }
            log.debug("getSubTaskRegion:  " + taskRegionMap.size() );
        }catch (SQLException e) {
            log.error("query getSubTaskRegion..." + e.getMessage(), e);
            throw new SQLException("getSubTaskRegion..."  + e.getMessage(),
                    e);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(pst);
            DbUtils.closeQuietly(manConn);
        }
    }

	/**
	 * @Description:查询子任务数据
	 * @return
	 * @throws SQLException
	 * @author: y
	 * @time:2017-9-13 上午11:51:13
	 */
	private void getSubTaskTips(String subTaskColName) throws Exception{
        Connection tipsConn=null;
        PreparedStatement pst = null;
        ResultSet rs = null;
	      try {
              tipsConn=DBConnector.getInstance().getTipsIdxConnection();
              String sql="SELECT ID, WKT, T." + subTaskColName +
                        "  FROM TIPS_INDEX T\n" +
                        " WHERE T.STAGE = 1\n" +
                        "   AND T.T_TIPSTATUS = 2\n" +
                        "   AND T." + subTaskColName + " != 0\n" +
                        " ORDER BY T." + subTaskColName;
              pst = tipsConn.prepareStatement(sql);
              rs = pst.executeQuery();
              rs.setFetchSize(5000);
              int subTaskId = 0;
//              StringBuffer meshSf = new StringBuffer();
              while (rs.next()) {
                  String rowkey = rs.getString("id");
                  STRUCT wkt = (STRUCT) rs.getObject("wkt");
                  int rsSubTaskId = rs.getInt(subTaskColName);
                  Geometry wktGeo = GeoTranslator.struct2Jts(wkt);

                  if(subTaskId !=0 && rsSubTaskId != subTaskId) {//另一个任务的记录开始
                      //统计subTaskId跨大区日志，将跨大区数据进行无任务批处理
                      doBatch(subTaskId);
                  }

                  Set<String> grids = CompGeometryUtil.geo2GridsWithoutBreak(wktGeo);
                  String gridId = grids.iterator().next();
                  String mesh = gridId.substring(0,6);
                  if(gridIdMap.containsKey(gridId)) {
                      Set<String> rowkeySet = gridIdMap.get(gridId);
                      rowkeySet.add(rowkey);
                  }else{
                      Set<String> rowkeySet = new HashSet<>();
                      rowkeySet.add(rowkey);
                      gridIdMap.put(gridId, rowkeySet);
                  }

                  meshSet.add(mesh);
                  subTaskId = rsSubTaskId;
              }

              doBatch(subTaskId);
          }catch (SQLException e) {
	            log.error("query TipsRegionTmpBatchScript to batch erro..." + e.getMessage(), e);
	            throw new SQLException("query TipsRegionTmpBatchScript to batch erro..."  + e.getMessage(),
	                    e);
	        } finally {
	        	DbUtils.closeQuietly(rs);
	            DbUtils.closeQuietly(pst);
	            DbUtils.closeQuietly(tipsConn);
	        }
	}

    private void doBatch(int subTaskId) throws Exception {
        //1.查询子任务所有图幅对应的regionId
        StringBuffer meshSf = new StringBuffer();
        for(String mesh : meshSet) {
            if(meshSf.length() == 0) {
                meshSf.append(mesh);
            }else{
                meshSf.append("," + mesh);
            }
        }
        Map<Integer, String> regionMeshMap = getSubTaskRegionMesh(meshSf.toString());
        if(regionMeshMap == null || regionMeshMap.size() == 0) {
            return;
        }

        Set<String> noTaskRowkeySet = new HashSet<>();
        List<UploadCrossRegionInfoDao> infos = new ArrayList<UploadCrossRegionInfoDao>();

        //2.获取任务当前region
        TaskUserRegion tur = taskRegionMap.get(subTaskId);
        if(tur == null) {
            return;
        }
        int curRegionId = tur.getRegionId();
        //写UPLOAD_CROSS_REGION_INFO记录,大区外数据更新成无任务
        for(String gridId : gridIdMap.keySet()) {
            Set<String> rowkeySet = gridIdMap.get(gridId);
            String mesh = gridId.substring(0,6);
            for(Integer regionId : regionMeshMap.keySet()) {
                String meshes = "," + regionMeshMap.get(regionId) + ",";
                //获取该gridId所在regionId
                if(meshes.contains("," + mesh + ",")) {
                    //只处理子任务大区外
                    if(regionId != curRegionId) {
                        noTaskRowkeySet.addAll(rowkeySet);
                        //写UPLOAD_CROSS_REGION_INFO记录
                        UploadCrossRegionInfoDao info = new UploadCrossRegionInfoDao();
                        info.setUserId(tur.getUserId());
                        info.setFromSubtaskId(subTaskId);
                        info.setUploadType(2);
                        info.setOutRegionId(regionId);
                        info.setOutGridId(Integer.valueOf(gridId));
                        info.setOutGridNumber(rowkeySet.size());
                        infos.add(info);
                    }
                    break;
                }
            }
        }
        //跨大区日志记录，跨大区数据无任务批处理
        if(infos.size() > 0) {
            doBatchTips(infos, noTaskRowkeySet);
        }

        meshSet.clear();
        gridIdMap.clear();
    }

    private void doBatchTips(List<UploadCrossRegionInfoDao> infos, Set<String> noTaskRowkeySet) throws Exception{
        //批量保存子任务写UPLOAD_CROSS_REGION_INFO记录
        Connection sysConn = null;
        try {
            sysConn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
            UploadRegionInfoOperator op = new UploadRegionInfoOperator(sysConn);
            op.save(infos);
        }catch (SQLException e) {
            log.error("query getSubTaskRegion..." + e.getMessage(), e);
            throw new SQLException("getSubTaskRegion..."  + e.getMessage(),
                    e);
        } finally {
            DbUtils.commitAndCloseQuietly(sysConn);
        }

        //批量更新任务外rowkey
        //1.更新hbase
        Table htab = null;
        Connection conn = null;
        PreparedStatement pst = null;
        try {
            conn = DBConnector.getInstance().getTipsIdxConnection();
            String updateSql = "update TIPS_INDEX set S_QSUBTASKID=0,S_MSUBTASKID=0,S_QTASKID=0,S_MTASKID=0 " +
                    "where id in (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?)))";

            htab = HBaseConnector.getInstance().getConnection()
                    .getTable(TableName.valueOf(tableName));
            List<Put> puts = new ArrayList<>();
            String[] queryColNames = { "source" };

            StringBuffer rowkeySF = new StringBuffer();
            for(String rowkey : noTaskRowkeySet) {
                JSONObject oldTip = HbaseTipsQuery.getHbaseTipsByRowkey(htab,
                        rowkey, queryColNames);
                JSONObject source = oldTip.getJSONObject("source");
                source.put("s_qTaskId", 0);
                source.put("s_qSubTaskId", 0);
                source.put("s_mTaskId", 0);
                source.put("s_mSubTaskId", 0);
                Put put = new Put(rowkey.getBytes());
                put.addColumn("data".getBytes(), "source".getBytes(), source.toString()
                        .getBytes());
                puts.add(put);

                if(puts.size() % 5000 == 0) {
                    htab.put(puts);

                    puts.clear();
                }

                if(rowkeySF.length() == 0) {
                    rowkeySF.append(rowkey);
                }else{
                    rowkeySF.append("," + rowkey);
                }
            }

            if(puts.size() > 0) {
                htab.put(puts);
                puts.clear();
            }

            pst = conn.prepareStatement(updateSql);
            Clob clob=ConnectionUtil.createClob(conn, rowkeySF.toString());
            pst.setClob(1, clob);

            pst.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(pst);
            DbUtils.commitAndCloseQuietly(conn);
            if(htab != null) {
                htab.close();
            }
        }
    }

    private Map<Integer, String> getSubTaskRegionMesh(String meshSf) throws Exception{
        Map<Integer, String> regionMeshMap = new HashMap<>();
        Connection manConn=null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            manConn = DBConnector.getInstance().getManConnection();
            String sql = "WITH TEMP AS\n" +
                    " (SELECT R.REGION_ID, M.GN / 100 MESH\n" +
                    "    FROM REGION R,\n" +
                    "         GRID G,\n" +
                    "         (SELECT TO_NUMBER(COLUMN_VALUE) * 100 GN\n" +
                    "            FROM TABLE(CLOB_TO_TABLE(?))) M\n" +
                    "   WHERE R.REGION_ID = G.REGION_ID\n" +
                    "     AND G.GRID_ID = M.GN)\n" +
                    "SELECT REGION_ID, LISTAGG(MESH, ',') WITHIN GROUP(ORDER BY MESH) MESHES\n" +
                    "  FROM TEMP\n" +
                    " GROUP BY REGION_ID";

            pst = manConn.prepareStatement(sql);
            Clob clob=ConnectionUtil.createClob(manConn, meshSf);
            pst.setClob(1, clob);

            rs = pst.executeQuery();
            rs.setFetchSize(5000);
            while (rs.next()) {
                int regionId = rs.getInt("REGION_ID");
                String meshes = rs.getString("MESHES");
                regionMeshMap.put(regionId, meshes);
            }
            log.debug("getSubTaskRegion:  " + regionMeshMap.size() );
        }catch (SQLException e) {
            log.error("query getSubTaskRegion..." + e.getMessage(), e);
            throw new SQLException("getSubTaskRegion..."  + e.getMessage(),
                    e);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(pst);
            DbUtils.closeQuietly(manConn);
        }
        return regionMeshMap;
    }
	

	public static void initContext(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-app-scripts.xml", "dubbo-scripts.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			log.info("......................start batch TipsRegionTmpBatchScript......................");
            initContext();
            TipsRegionTmpBatchScript script = new TipsRegionTmpBatchScript();
            script.getSubTaskRegion();
            script.getSubTaskTips("S_QSUBTASKID");
            script.getSubTaskTips("S_MSUBTASKID");
			log.info("......................all TipsRegionTmpBatchScript update Over......................");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(" excute  error "+e.getMessage(), e);
		} finally {
			log.info("......................all TipsRegionTmpBatchScript update Over......................");
			System.exit(0);
		}

	}

    class TaskUserRegion {
        private int userId;
        private int regionId;

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getRegionId() {
            return regionId;
        }

        public void setRegionId(int regionId) {
            this.regionId = regionId;
        }
    }

}
