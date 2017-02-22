package com.navinfo.dataservice.engine.edit.operation.topo.depart.updowndepartlink;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.List;

/**
 * @author Crayeres
 * @version 1.0
 */
public class Check {

    private static Logger logger = Logger.getLogger(Check.class);

    private Command command;

    private Connection conn;

    public Check(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    public void preCheck() throws Exception {
        checkCRFI();
    }

    public void checkCRFI() throws Exception {
        List<Integer> viaNodes = CalLinkOperateUtils.calNodePids(command.getLinks());

        RdInterSelector selector = new RdInterSelector(conn);
        List<RdInter> inters = selector.loadInterByNodePid(StringUtils.getInteStr(viaNodes), false);
        if (!inters.isEmpty())
            throw new Exception("此点做了CRFI信息，不允许移动");
    }
}
