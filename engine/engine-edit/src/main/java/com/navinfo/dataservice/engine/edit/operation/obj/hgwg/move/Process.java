package com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.hgwg.RdHgwgLimitSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import java.sql.Connection;

/**
 * Created by chaixin on 2016/11/8 0008.
 */
public class Process extends AbstractProcess<Command> {
    public Process() {
    }

    public Process(AbstractCommand command, Result result, Connection conn) throws Exception {
        super(command, result, conn);
    }

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    @Override
    public boolean prepareData() throws Exception {
        RdHgwgLimitSelector selector = new RdHgwgLimitSelector(this.getConn());
        RdHgwgLimit hgwgLimit = (RdHgwgLimit) selector.loadById(this.getCommand().getContent().getInt("pid"), true);
        this.getCommand().setHgwgLimit(hgwgLimit);

        RdLinkSelector linkSelector = new RdLinkSelector(getConn());
        RdLink sourceLink = (RdLink) linkSelector.loadById(hgwgLimit.getLinkPid(), false);
        RdLink targetLink = (RdLink) linkSelector.loadById(getCommand().getContent().getInt("linkPid"), false);

        Geometry sourceGeometry = GeoTranslator.transform(hgwgLimit.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
        Geometry targetGeometry = GeoTranslator.transform(GeoTranslator.createPoint(new Coordinate(getCommand().getContent().
                getDouble("longitude"), getCommand().getContent().getDouble("latitude"))), 1, 5);

        CheckConnectivity checkConnectivity =
                new CheckConnectivity(getConn(), "限高限重", sourceLink, sourceGeometry, targetLink, targetGeometry);
        checkConnectivity.check();
        return true;
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(this.getCommand()).run(this.getResult());
    }
}
