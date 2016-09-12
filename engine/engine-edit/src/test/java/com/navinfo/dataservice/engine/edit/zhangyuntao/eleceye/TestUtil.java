package com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.sql.Connection;

public class TestUtil {
    public static void run(String requester) {
        Transaction t = new Transaction(requester);
        try {
            String msg = t.run();
            String log = t.getLogs();
            JSONObject json = new JSONObject();
            json.put("result", msg);
            json.put("log", log);
            json.put("check", t.getCheckLog());
            json.put("pid", t.getPid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getByCondition(String parameter) throws Exception{
        Connection conn = null;
        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);
            String objType = jsonReq.getString("type");
            int dbId = jsonReq.getInt("dbId");
            JSONObject data = jsonReq.getJSONObject("data");
            conn = DBConnector.getInstance().getConnectionById(dbId);
            SearchProcess p = new SearchProcess(conn);
            JSONArray array = p.searchDataByCondition(ObjType.valueOf(objType),
                    data);
        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
