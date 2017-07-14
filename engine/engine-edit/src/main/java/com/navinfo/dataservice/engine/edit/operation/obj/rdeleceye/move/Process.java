package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move.CheckConnectivity;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author zhangyt
 * @Title: Process.java
 * @Description: TODO
 * @date: 2016年7月29日 下午3:45:06
 * @version: v1.0
 */
public class Process extends AbstractProcess<Command> {

    public Process() {
        super();
    }

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    @Override
    public boolean prepareData() throws Exception {
        // 根据EleceyePid加载需要更新的RdElectroniceye
        RdLinkSelector selector = new RdLinkSelector(getConn());
        this.getCommand().setEleceye((RdElectroniceye) new RdElectroniceyeSelector(this.getConn()).loadById(this
                .getCommand().getPid(), true));
        this.getCommand().setLink((RdLink) selector.loadById(getCommand().getContent().getInt("linkPid"), true));

        check(selector);

        return false;
    }

    private void check(RdLinkSelector selector)  throws Exception
    {
        if (getCommand().getEleceye().getLinkPid() != 0) {
            RdLink sourceLink = (RdLink) selector.loadById(getCommand().getEleceye().getLinkPid(), true);
            RdLink targetLink = getCommand().getLink();

            Geometry sourceGeometry = GeoTranslator.transform(
                    getCommand().getEleceye().getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
            Geometry targetGeometry = GeoTranslator.geojson2Jts(getCommand().getContent().getJSONObject("geometry"));

            CheckConnectivity checkConnectivity =
                    new CheckConnectivity(getConn(), "电子眼", sourceLink, sourceGeometry, targetLink, targetGeometry);
            checkConnectivity.check();
        }
    }

    @Override
    public String exeOperation() throws Exception {
        new Operation(this.getCommand()).run(this.getResult());
        return null;
    }

}
