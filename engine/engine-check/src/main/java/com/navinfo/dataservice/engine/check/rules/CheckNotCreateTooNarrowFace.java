package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaixin on 2017/1/19 0019.
 */
public class CheckNotCreateTooNarrowFace extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow faceRow : checkCommand.getGlmList()) {
            if (faceRow instanceof ZoneFace) {
                ZoneFace face = (ZoneFace) faceRow;
                if (!face.status().equals(OperType.CREATE)) continue;

                List<IRow> faceTopos = face.getFaceTopos();
                List<Integer> linkPids = new ArrayList<>();
                for (IRow topoRow : faceTopos)
                    linkPids.add(((ZoneFaceTopo) topoRow).getLinkPid());

                for (IRow linkRow : checkCommand.getGlmList()) {
                    if (linkRow instanceof ZoneLink) {
                        ZoneLink link = (ZoneLink) linkRow;
                        if (!link.status().equals(OperType.CREATE) || !linkPids.contains(link.pid()) || link
                                .getMeshes().size() == 1)
                            continue;

                        double length = link.getLength();
                        if (length <= 1) {
                            setCheckResult("", "", 0);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
