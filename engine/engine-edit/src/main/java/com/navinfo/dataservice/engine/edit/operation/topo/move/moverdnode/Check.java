package com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;

public class Check {
	 private void throwException(String msg) throws Exception {
	        throw new Exception(msg);
	    }

		//PERMIT_CHECK_LINK_NODE_CRFI  如果link上的node已经参与制作了CRFI，则不允许对此node进行修形操作
	    public void checkCRFI(Connection conn, Integer nodePid) throws Exception {
	        RdInterSelector selector = new RdInterSelector(conn);
	        List<RdInter> inters = selector.loadInterByNodePid(String.valueOf(nodePid), false);
	        if (!inters.isEmpty())
	            throwException("此点做了CRFI信息，不允许移动");
	        List<IRow> forms = new AbstractSelector(RdNodeForm.class, conn).loadRowsByParentId(nodePid, false);
	        for (IRow f : forms) {
	            RdNodeForm form = (RdNodeForm) f;
	            if (form.getFormOfWay() == 3) {
	                throwException("此点做了CRFI信息，不允许移动");
	            }
	        }
	    }
}
