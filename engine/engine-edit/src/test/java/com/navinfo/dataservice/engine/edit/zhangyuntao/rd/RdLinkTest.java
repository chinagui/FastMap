package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.utils.batch.SpeedLimitUtils;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.sql.Connection;

import static com.navinfo.dataservice.dao.glm.iface.ObjType.RDLINK;

/**
 * @author zhangyt
 * @Title: RdLinkTest.java
 * @Description: TODO
 * @date: 2016年8月3日 上午10:32:29
 * @version: v1.0
 */
public class RdLinkTest extends InitApplication {

    public RdLinkTest() {
    }

    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void testUpdate() {
        String parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":575750,\"data\":{\"forms\":[{\"auxiFlag\":76,\"rowId\":\"3AE1FBE7366B92F7E050A8C08304EE4C\",\"objStatus\":\"UPDATE\"}],\"pid\":575750}}";
        TestUtil.run(parameter);
    }

    @Test
    public void update() {
        String parameter = "{\"command\":\"BREAK\",\"dbId\":42,\"objId\":100008435,\"data\":{\"longitude\":116.4702206995429,\"latitude\":40.08258242178863},\"type\":\"RDLINK\"}";
        parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"RDLINK\",\"objId\":589615,\"data\":{\"kind\":3,\"pid\":589615,\"objStatus\":\"UPDATE\"}}";
        parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":305003119,\"data\":{\"imiCode\":1,\"pid\":305003119,\"objStatus\":\"UPDATE\"}}";

        //        try {
        //            Connection conn = DBConnector.getInstance().getConnectionById(42);
        //            RdLinkSelector selector = new RdLinkSelector(conn);
        //            RdLink link = (RdLink) selector.loadById(589615, false, null);
        //            JSONObject json = JSONObject.fromObject("{\"urban\":1,\"pid\":589615,\"objStatus\":\"UPDATE\"}");
        //            SpeedLimitUtils.updateRdLink(link, json, null);
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
        TestUtil.run(parameter);
    }

    @Test
    public void repair() {
        String parameter = "{\"command\":\"REPAIR\",\"dbId\":42,\"objId\":100008849,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46721,40.083],[116.46730363368988,40.082890151613405],[116.46738,40.08272]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RDLINK\"}";
        parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.44574403762817,39.91689151500802],[116.44597470760347,39.91648007577787]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
        TestUtil.run(parameter);
    }

    @Test
    public void create() {
        String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46699,40.08309],[116.46714,40.08249]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
        parameter = "{\"command\":\"UPDATE\",\"dbId\":249,\"type\":\"RDLINK\",\"objId\":466004,\"data\":{\"direct\":1,\"pid\":466004,\"objStatus\":\"UPDATE\",\"speedlimits\":[{\"speedClass\":0,\"rowId\":\"3F836C5B12D14604E050A8C083041544\",\"objStatus\":\"UPDATE\",\"speedClassWork\":1}]}}";
        TestUtil.run(parameter);
    }

    @Test
    public void depart() {
        String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"13.4\",\"data\":{\"linkPids\":[575024,567326,567327,665041]}}";
        parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"8.3\",\"data\":{\"linkPids\":[86366612,87757818,87757819,86366614,663904,663906,663908,677530,87758006,87758007,87729181]}}";
        parameter = "{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDSAMENODE\",\"objId\":70828,\"data\":{\"parts\":[{\"objStatus\":\"INSERT\"},{\"objStatus\":\"INSERT\"},{\"objStatus\":\"INSERT\"}],\"rowId\":\"3AE1F4D1014992F7E050A8C08304EE4C\",\"pid\":70828,\"objStatus\":\"UPDATE\"}}";
        TestUtil.run(parameter);
    }

    @Test
    public void search() {
        String parameter = "http://192.168.4.188:8000/service/render/obj/getByTileWithGap?parameter={\"dbId\":17,\"gap\":80,\"types\":[\"RDHGWGLIMIT\"],\"z\":18,\"x\":215889,\"y\":99231}";
        try {
            TestSearch.testSearchGap(parameter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updownDepart() {
        String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\",\"data\":{\"linkPids\":[209000217]}}";
        parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\",\"data\":{\"linkPids\":[307002979]}}";
        TestUtil.run(parameter);
    }

    @Test
    public void move() {
        String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":304000034,\"data\":{\"longitude\":116.37564,\"latitude\":38.51548},\"type\":\"ZONENODE\"}";
        TestUtil.run(parameter);
    }

    @Test
    public void batch() {
        //        String requester = "{\"dbId\":19,\"type\":\"RDLINK\",\"command\":\"BATCH\",\"linkPids\":[205002774,205002775],\"data\":{\"kind\":2,\"names\":[{\"rowId\":\"\",\"nameGroupid\":3562426,\"name\":\"112 Line Guo Ave\",\"seqNum\":2,\"nameClass\":1,\"inputTime\":\"\",\"nameType\":0,\"srcFlag\":9,\"routeAtt\":0,\"code\":0,\"objStatus\":\"INSERT\"},{\"rowId\":[\"42FC03C80E964FB4A63FC500348252F0\",\"BEF3A918871140C1BBAEBC7A54F9752E\"],\"nameGroupid\":111,\"objStatus\":\"UPDATE\"},{\"rowId\":[\"F16A0EEA8FB04EC2A1F1F099EBA60392\",\"CBEACA83C9664EB19024F7E8A3B80CBA\"],\"objStatus\":\"DELETE\"}]}}";
        String requester = "{ \"command\": \"UPDATE\", \"dbId\": 17, \"type\": \"RDLINK\", \"linkPids\": [302002756, 303002740, 304002735], \"data\": [{ \"kind\": 6, \"pid\": 302002756, \"objStatus\": \"UPDATE\", \"names\": [{ \"rowId\": \"8E19A3597DCB4FE4AB46769F6BAA8766\", \"objStatus\": \"UPDATE\", \"nameGroupid\": 307358, \"name\": \"212 City Ave\" }, { \"linkPid\": 302002756, \"rowId\": \"7E0F87B8EDFB469D810A15C5BAF429B4\", \"nameGroupid\": 3539279, \"name\": \"１７线街\", \"seqNum\": 2, \"nameClass\": 1, \"inputTime\": \"\", \"nameType\": 0, \"srcFlag\": 9, \"routeAtt\": 0, \"code\": 0, \"objStatus\": \"DELETE\", \"pid\": 302002756 }, { \"linkPid\": 302002756, \"rowId\": \"\", \"nameGroupid\": 3539279, \"name\": \"１７线街\", \"seqNum\": 2, \"nameClass\": 1, \"inputTime\": \"\", \"nameType\": 0, \"srcFlag\": 9, \"routeAtt\": 0, \"code\": 0, \"objStatus\": \"INSERT\" }] }, { \"kind\": 6, \"pid\": 303002740, \"objStatus\": \"UPDATE\", \"names\": [{ \"rowId\": \"037FED0808AA4CAD953EB4543C2B9889\", \"objStatus\": \"UPDATE\", \"nameGroupid\": 307358, \"name\": \"212 City Ave\" }, { \"linkPid\": 303002740, \"rowId\": \"05598049E4924812A0B8A95D409747FB\", \"nameGroupid\": 3539279, \"name\": \"１７线街\", \"seqNum\": 2, \"nameClass\": 1, \"inputTime\": \"\", \"nameType\": 0, \"srcFlag\": 9, \"routeAtt\": 0, \"code\": 0, \"objStatus\": \"DELETE\", \"pid\": 303002740 }, { \"linkPid\": 303002740, \"rowId\": \"\", \"nameGroupid\": 3539279, \"name\": \"１７线街\", \"seqNum\": 2, \"nameClass\": 1, \"inputTime\": \"\", \"nameType\": 0, \"srcFlag\": 9, \"routeAtt\": 0, \"code\": 0, \"objStatus\": \"INSERT\" }] }, { \"kind\": 6, \"pid\": 304002735, \"objStatus\": \"UPDATE\", \"names\": [{ \"rowId\": \"0682BB237F7645B7B3ABF29EB8AB39B7\", \"objStatus\": \"UPDATE\", \"nameGroupid\": 307358, \"name\": \"212 City Ave\" }, { \"linkPid\": 304002735, \"rowId\": \"8C7CBE80D2CC4D239DE09C7CD5AC2BAB\", \"nameGroupid\": 3539279, \"name\": \"１７线街\", \"seqNum\": 2, \"nameClass\": 1, \"inputTime\": \"\", \"nameType\": 0, \"srcFlag\": 9, \"routeAtt\": 0, \"code\": 0, \"objStatus\": \"DELETE\", \"pid\": 304002735 }, { \"linkPid\": 304002735, \"rowId\": \"\", \"nameGroupid\": 3539279, \"name\": \"１７线街\", \"seqNum\": 2, \"nameClass\": 1, \"inputTime\": \"\", \"nameType\": 0, \"srcFlag\": 9, \"routeAtt\": 0, \"code\": 0, \"objStatus\": \"INSERT\" }] }] }";
        requester = "{\"command\":\"BATCH\",\"dbId\":249,\"type\":\"RDLINK\",\"data\":[{\"intRtics\":[{\"linkPid\":490986,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":490986,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":490986},{\"intRtics\":[{\"linkPid\":18758970,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":18758970,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":18758970},{\"intRtics\":[{\"linkPid\":18758969,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":18758969,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":18758969},{\"intRtics\":[{\"linkPid\":490982,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":490982,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":490982},{\"intRtics\":[{\"linkPid\":487629,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":487629,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":487629},{\"intRtics\":[{\"linkPid\":481388,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":481388,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":481388},{\"intRtics\":[{\"linkPid\":470696,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":470696,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":470696},{\"intRtics\":[{\"linkPid\":481390,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":481390,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":481390},{\"intRtics\":[{\"linkPid\":469049,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":469049,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":469049},{\"intRtics\":[{\"linkPid\":481383,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":481383,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":481383},{\"intRtics\":[{\"linkPid\":470699,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":470699,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":470699},{\"intRtics\":[{\"linkPid\":19376295,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":19376295,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":19376295},{\"intRtics\":[{\"linkPid\":19376294,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":19376294,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":19376294},{\"intRtics\":[{\"linkPid\":472111,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":472111,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":472111},{\"intRtics\":[{\"linkPid\":469046,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":469046,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":469046},{\"intRtics\":[{\"linkPid\":469043,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":469043,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":469043},{\"intRtics\":[{\"linkPid\":469042,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":469042,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":469042},{\"intRtics\":[{\"linkPid\":470447,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":470447,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":470447},{\"intRtics\":[{\"linkPid\":469036,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":469036,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":469036},{\"intRtics\":[{\"linkPid\":86755008,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":86755008,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":86755008},{\"intRtics\":[{\"linkPid\":86755007,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":86755007,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":86755007},{\"intRtics\":[{\"linkPid\":86755005,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":86755005,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":86755005},{\"intRtics\":[{\"linkPid\":472168,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":472168,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":472168},{\"intRtics\":[{\"linkPid\":17734893,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":17734893,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":17734893},{\"intRtics\":[{\"linkPid\":20420443,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":20420443,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":20420443},{\"intRtics\":[{\"linkPid\":20420442,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":20420442,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":20420442},{\"intRtics\":[{\"linkPid\":17734897,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":17734897,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":17734897},{\"intRtics\":[{\"linkPid\":17734876,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":17734876,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":17734876},{\"intRtics\":[{\"linkPid\":17734900,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":17734900,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":17734900},{\"intRtics\":[{\"linkPid\":17734899,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":\"1\",\"rangeType\":1,\"objStatus\":\"UPDATE\"},{\"linkPid\":17734899,\"rowId\":\"DDA3B301E2AE47DBA6B3E75C6282CE05\",\"code\":1,\"rank\":3,\"rticDir\":2,\"updownFlag\":0,\"rangeType\":1,\"objStatus\":\"UPDATE\"}],\"pid\":17734899}]}";
        TestUtil.run(requester);
    }
}
