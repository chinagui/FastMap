package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * Created by chaixin on 2016/11/14 0014.
 */
public class RestrictionTest extends InitApplication{
    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void create(){
        String parameter = "{\"command\":\"CREATE\",\"type\":\"RDRESTRICTION\",\"dbId\":17,\"data\":{\"inLinkPid\":735948,\"nodePid\":477758,\"outLinkPids\":[735946],\"infos\":\"1\"}}";
        TestUtil.run(parameter);
    }

    @Test
    public void search() throws Exception {
        String parameter = "http://192.168.4.188:8000/service/render/obj/getByTileWithGap?parameter={\"dbId\":17,\"gap\":80,\"types\":[\"RDRESTRICTION\"],\"z\":19,\"x\":431713,\"y\":198401}";
        TestSearch.testSearchGap(parameter);
    }
}
