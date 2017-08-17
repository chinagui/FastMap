package com.navinfo.dataservice.engine.editplus;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule.*;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRuleCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.NiValException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
//import com.navinfo.dataservice.engine.editplus.operation.imp.UploadOperationByGather;


public class CheckTest extends ClassPathXmlAppContextInit{

    public CheckTest() {
        // TODO Auto-generated constructor stub
    }

    @Before
    public void init() {
    	initContext(new String[]{"dubbo-editplus.xml"});
    }

    //	@Test
    //	public void test() {
    //		UploadOperationByGather operation = new UploadOperationByGather((long) 0);
    //		try {
    //			Date startTime = new Date();
    //			JSONObject ret = operation.importPoi("F://testpoi.txt");
    //			System.out.println(ret);
    //			Date endTime = new Date();
    //			System.out.println("total time:"+ (endTime.getTime() - startTime.getTime()));
    ////			System.out.println(UuidUtils.genUuid());
    //		} catch (Exception e) {
    //			e.printStackTrace();
    //		}
    //
    //	}

    @Test
    public void check() throws Exception {
        FMDGC008 check = new FMDGC008();
        Connection conn = DBConnector.getInstance().getConnectionById(13);
        CheckRuleCommand command = new CheckRuleCommand();
        command.setConn(conn);
        check.setCheckRuleCommand(command);

        Set<Long> pids = new HashSet<Long>();
        String sql = "SELECT PID FROM IX_POI WHERE U_RECORD <> 2  AND MESH_ID = 595676 AND ROWNUM <= 20";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet resultSet = pstmt.executeQuery();
        while (resultSet.next()) {
            pids.add(resultSet.getLong("PID"));
        }

        CheckRule checkRule = new CheckRule();
        checkRule.setObjNameSet(ObjectName.IX_POI);
        check.setCheckRule(checkRule);

        Map<Long, BasicObj> pois = ObjBatchSelector.selectByPids(conn, "IX_POI", null, true, pids, false, false);
        Map<String, Map<Long, BasicObj>> map = new HashMap<>();
        map.put(ObjectName.IX_POI, pois);
        command.setAllDatas(map);

        check.run();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("start check test");
        CheckTest test = new CheckTest();
        test.init();
        Connection conn = DBConnector.getInstance().getConnectionById(13);

        String sql = "SELECT pid FROM poi_edit_status WHERE medium_subtask_id=61";
        PreparedStatement state = conn.prepareStatement(sql);
        ResultSet rs = state.executeQuery();
        Set<Long> pids = new HashSet<Long>();
        while (rs.next()) {
            pids.add(rs.getLong("PID"));
        }

        Map<Long, BasicObj> pois = ObjBatchSelector.selectByPids(conn, "IX_POI", null, true, pids, false, false);
        OperationResult operationResult = new OperationResult();
        operationResult.putAll(pois.values());

        CheckCommand checkCommand = new CheckCommand();
        List<String> ruleIdList = new ArrayList<String>();
        ruleIdList.add("FM-11Win-01-17");
        checkCommand.setRuleIdList(ruleIdList);
        checkCommand.setSaveResult(false);

        Check check = new Check(conn, operationResult);
        check.operate(checkCommand);
        Map<String, Map<Long, Set<String>>> errorPid = check.getErrorPidMap();
        if (errorPid != null) {
            System.out.println(check.getReturnExceptions().size());
            System.out.println(check.getReturnExceptions().get(0).getInformation());
            for (NiValException tmp : check.getReturnExceptions()) {
                System.out.println(tmp.getTargets());
            }
        } else {
            System.out.println("null");
        }
        //DbUtils.commitAndCloseQuietly(conn);
        System.out.println("end check test");
        //==========suchenguang git test==========
    }

}
