package com.navinfo.dataservice.engine.edit.operation.topo.move.movecmgnode;

import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildfaceSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import java.util.List;

/**
 * Created by crayeres on 2017/4/12.
 */
public class Process extends AbstractProcess<Command> {

    @Override
    public boolean prepareData() throws Exception {
        // 加载CMG-NODE
        CmgBuildnode cmgnode = (CmgBuildnode) new AbstractSelector(CmgBuildnode.class, getConn()).
                loadById(getCommand().getCmgnode().pid(), false);
        getCommand().setCmgnode(cmgnode);
        // 加载CMG-LINK
        List<CmgBuildlink> cmglinks = new CmgBuildlinkSelector(getConn()).
                listTheAssociatedLinkOfTheNode(cmgnode.pid(), false);
        getCommand().setCmglinks(cmglinks);
        // 加载CMG-FACE
        List<CmgBuildface> cmgfaces = new CmgBuildfaceSelector(getConn()).
                listTheAssociatedFaceOfTheNode(cmgnode.pid(), false);
        getCommand().setCmgfaces(cmgfaces);
        return super.prepareData();
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(getCommand(), getConn()).run(getResult());
    }
}
