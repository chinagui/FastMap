package com.navinfo.dataservice.scripts.tmp.Tips;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName: TipsConnnexityArwTmpScript.java
 * @author zjf
 * @date 2017-9-22 下午1:50:13
 * @Description: 脚本程序——脚本说明：已作业（stage<>0）的车信Tips（1301），
 * 当arwB字段不在值域范围内时，批处理成空字符串
 * 
 */
public class TipsConnnexityArwBTmpScript {

    private static final Logger log = Logger.getLogger(TipsConnnexityArwBTmpScript.class);
    private String tableName = HBaseConstant.tipTab;
    private Set<String> arwBValues = null;
    private String arwBDefaultValue = "";

    public TipsConnnexityArwBTmpScript() {
        arwBValues = new HashSet<>();
        arwBValues.add("");
        arwBValues.add("9");
        arwBValues.add("a");
        arwBValues.add("b");
        arwBValues.add("c");
        arwBValues.add("d");
        arwBValues.add("e");
        arwBValues.add("f");
        arwBValues.add("g");
        arwBValues.add("h");
        arwBValues.add("i");
        arwBValues.add("j");
        arwBValues.add("k");
        arwBValues.add("l");
        arwBValues.add("m");
        arwBValues.add("n");
        arwBValues.add("o");
        arwBValues.add("p");
        arwBValues.add("r");
        arwBValues.add("s");
        arwBValues.add("t");
        arwBValues.add("u");
        arwBValues.add("v");
        arwBValues.add("w");
        arwBValues.add("x");
        arwBValues.add("y");
        arwBValues.add("z");
        arwBValues.add("o");
        arwBValues.add("0");
        arwBValues.add("1");
        arwBValues.add("2");
        arwBValues.add("3");
        arwBValues.add("4");
        arwBValues.add("5");
    }

    /**
     * @Description:查询子任务数据
     * @return
     * @throws SQLException
     * @time:2017-9-13 上午11:51:13
     */
    private void batchConnexityData() throws Exception{
        Connection tipsConn=null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        Table htab = null;
        String[] queryColNames = { "deep" };
        Statement tmpIdxStmt = null;
        PreparedStatement tmpIdxAddStmt = null;
        try {
            htab = HBaseConnector.getInstance().getConnection()
                    .getTable(TableName.valueOf(tableName));

            tipsConn = DBConnector.getInstance().getTipsIdxConnection();

            String createSql = "CREATE TABLE TMP_TIPS_CONNEXITY_ARWB (ROWKEY VARCHAR2(50))";
            tmpIdxStmt = tipsConn.createStatement();
            tmpIdxStmt.execute(createSql);
            tmpIdxStmt.close();

            String insertSql = "insert into TMP_TIPS_CONNEXITY_ARWB (ROWKEY) VALUES (?)";
            tmpIdxAddStmt = tipsConn.prepareStatement(insertSql);

            String sql = "SELECT D.ID\n" +
                    "  FROM TIPS_INDEX D\n" +
                    " WHERE D.S_SOURCETYPE = 1301\n" +
                    "   AND D.STAGE <> 0";
            pst = tipsConn.prepareStatement(sql);
            rs = pst.executeQuery();
            rs.setFetchSize(5000);
            List<Put> putList = new ArrayList<>();
            while (rs.next()) {
                String rowkey = rs.getString("ID");
                if(rowkey.equals("111301103487")) {
                    System.out.println();
                }
                JSONObject oldTip = HbaseTipsQuery.getHbaseTipsByRowkey(htab,
                        rowkey, queryColNames);
                JSONObject deep = oldTip.getJSONObject("deep");
                JSONArray info = deep.getJSONArray("info");
                JSONArray newInfo = new JSONArray();
                boolean hasError = false;
                if(info != null && (!info.isEmpty())) {
                    for(int i = 0; i < info.size(); i++) {
                        JSONObject infoObj = info.getJSONObject(i);
                        String arwB = infoObj.getString("arwB");
                        if(arwB == null || !arwBValues.contains(arwB)) {
                            infoObj.put("arwB", arwBDefaultValue);
                            hasError = true;
                        }
                        newInfo.add(infoObj);
                    }
                }
                if(hasError) {
                    deep.put("info", newInfo);
                    Put put = new Put(rowkey.getBytes());
                    put.addColumn("data".getBytes(), "deep".getBytes(), deep.toString().getBytes());
                    putList.add(put);

                    tmpIdxAddStmt.setString(1, rowkey);
                    tmpIdxAddStmt.addBatch();

                    log.info("***********************************");
                    log.info("***********************************");
                    log.info("arwB error " + rowkey);
                }

                if(putList.size() > 0 && putList.size() % 5000 == 0) {
                    htab.put(putList);
                    putList.clear();

                    tmpIdxAddStmt.executeBatch();
                    tmpIdxAddStmt.clearBatch();
                }
            }

            if(putList.size() > 0) {
                htab.put(putList);
                putList.clear();

                tmpIdxAddStmt.executeBatch();
                tmpIdxAddStmt.clearBatch();
            }
        }catch (SQLException e) {
            DbUtils.closeQuietly(tmpIdxStmt);
            log.error("batchConnexityData to batch erro..." + e.getMessage(), e);
            throw new SQLException("batchConnexityData to batch erro..."  + e.getMessage(),
                    e);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(pst);
            DbUtils.closeQuietly(tmpIdxAddStmt);
            DbUtils.commitAndCloseQuietly(tipsConn);
        }
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
            log.info("......................start batch TipsConnnexityArwBTmpScript......................");
            initContext();
            TipsConnnexityArwBTmpScript script = new TipsConnnexityArwBTmpScript();
            script.batchConnexityData();
            log.info("......................all TipsConnnexityArwBTmpScript update Over......................");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(" excute  error "+e.getMessage(), e);
        } finally {
            log.info("......................all TipsConnnexityArwBTmpScript update Over......................");
            System.exit(0);
        }

    }

}
