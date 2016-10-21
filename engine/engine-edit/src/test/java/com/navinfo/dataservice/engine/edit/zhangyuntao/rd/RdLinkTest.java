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
    public void testDelete() {
        String parameter = "{\"command\":\"DELETE\",\"dbId\":42,\"type\":\"RDLINK\",\"objId\":100008436}";
        TestUtil.run(parameter);
    }

    @Test
    public void update() {
        String parameter = "{\"command\":\"BREAK\",\"dbId\":42,\"objId\":100008435,\"data\":{\"longitude\":116.4702206995429,\"latitude\":40.08258242178863},\"type\":\"RDLINK\"}";
        parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"RDLINK\",\"objId\":589615,\"data\":{\"kind\":3,\"pid\":589615,\"objStatus\":\"UPDATE\"}}";
        parameter = "{\"command\":\"UPDATE\",\"dbId\":42,\"type\":\"RDLINK\",\"objId\":589615,\"data\":{\"laneNum\":4,\"pid\":589615,\"objStatus\":\"UPDATE\"}}";

        try {
            Connection conn = DBConnector.getInstance().getConnectionById(42);
            RdLinkSelector selector = new RdLinkSelector(conn);
            RdLink link = (RdLink) selector.loadById(589615, false, null);
            JSONObject json = JSONObject.fromObject("{\"urban\":1,\"pid\":589615,\"objStatus\":\"UPDATE\"}");
            SpeedLimitUtils.updateRdLink(link, json, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // TestUtil.run(parameter);
    }

    @Test
    public void repair() {
        String parameter = "{\"command\":\"REPAIR\",\"dbId\":42,\"objId\":100008849,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46721,40.083],[116.46730363368988,40.082890151613405],[116.46738,40.08272]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RDLINK\"}";
        // parameter =
        // "{\"command\":\"REPAIR\",\"dbId\":42,\"objId\":100008881,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46375,40.08197],[116.46422982215881,40.08203232873719],[116.46456,40.08214]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RDLINK\"}";
        parameter = "{\"command\":\"REPAIR\",\"dbId\":42,\"objId\":100008888,\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46471,40.08145],[116.46497547626494,40.081500801483934],[116.46513,40.0816]]},\"interLinks\":[],\"interNodes\":[]},\"type\":\"RDLINK\"}";
        TestUtil.run(parameter);
    }

    @Test
    public void create() {
        String parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46699,40.08309],[116.46714,40.08249]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
        parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46726,40.08326],[116.46621,40.0831]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
        parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46726,40.08326],[116.46661,40.083]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
        parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46377652883528,40.08195639652649],[116.46438270807268,40.082114417518376]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
        parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46462410688399,40.08142281644134],[116.4649111032486,40.08155210738431]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
        parameter = "{\"command\":\"CREATE\",\"dbId\":42,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.46958351135254,40.0753458651827],[116.47236227989198,40.07571530019432]]},\"catchLinks\":[]},\"type\":\"RDLINK\"}";
        parameter = "{\"type\":\"RDBRANCH\",\"command\":\"UPDATE\",\"dbId\":42,\"data\":{\"details\":[{\"estabType\":2,\"pid\":100000438,\"objStatus\":\"UPDATE\"}],\"pid\":23037}}";
        parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"RDLINK\",\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"kind\":7,\"laneNum\":1,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.36877,40.04402],[116.36601,40.0505],[116.36493,40.05416]]}}}";
        parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"data\":{\"eNodePid\":0,\"sNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.56435132026672,40.352218653591116],[116.5644371509552,40.350133686360806],[116.5659713745117,40.34962674341031]]},\"catchLinks\":[{\"nodePid\":200000175,\"lon\":116.5644371509552,\"lat\":40.350133686360806}]},\"type\":\"RDLINK\"}";
        TestUtil.run(parameter);
    }

    @Test
    public void depart() {
        String parameter = "{\"command\":\"DEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"objId\":307000087,\"data\":{\"linkPid\":208000085,\"catchNodePid\":0,\"longitude\":116.30355,\"latitude\":40.55671}}";
        TestUtil.run(parameter);
    }

    @Test
    public void search(){
        String parameter = "http://192.168.4.188:8000/service/render/obj/getByTileWithGap?parameter={\"dbId\":108,\"gap\":80,\"types\":[\"RDLINK\"],\"z\":16,\"x\":53949,\"y\":24796}";
        try {
            TestSearch.testSearchGap(parameter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updownDepart(){
        String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\",\"data\":{\"linkPids\":[209000217]}}";
        parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"30.4\",\"data\":{\"linkPids\":[209000217]}}";
        parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\",\"data\":{\"linkPids\":[303001100,308001096]}}";
        TestUtil.run(parameter);
    }
}
