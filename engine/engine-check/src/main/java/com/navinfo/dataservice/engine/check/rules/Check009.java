package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFaceTopo;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ly on 2017/7/4.
 */
public class Check009 extends baseRule {

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

        List<LuFace> luFaces = new ArrayList<>();

        List<ZoneFace> zoneFaces = new ArrayList<>();

        for (IRow obj : checkCommand.getGlmList()) {

            if (obj instanceof LuFace && obj.status() == ObjStatus.INSERT) {

                luFaces.add((LuFace) obj);
            }

            if (obj instanceof ZoneFace && obj.status() == ObjStatus.INSERT) {

                zoneFaces.add((ZoneFace) obj);
            }
        }

        if (luFaces.size() == 1) {

            checkLuFace(luFaces.get(0));
        }

        if (zoneFaces.size() == 1) {

            checkZoneFace(zoneFaces.get(0));
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }

    private void checkLuFace(LuFace newFace) throws Exception {

        LuFaceSelector selector = new LuFaceSelector(this.getConn());

        Set<Integer> newLinkPids = new HashSet<>();

        for (IRow rowTopo : newFace.getFaceTopos()) {

            LuFaceTopo topo = (LuFaceTopo) rowTopo;

            newLinkPids.add(topo.getLinkPid());
        }

        for (int linkPid : newLinkPids) {

            List<LuFace> existFaces = selector.loadLuFaceByLinkId(linkPid, false);

            for (LuFace existFace : existFaces) {
                //组成link数不同
                if (newLinkPids.size() != existFace.getFaceTopos().size()) {
                    continue;
                }
                //组成link是否相同
                boolean isSame = true;

                for (IRow rowTopo : existFace.getFaceTopos()) {

                    LuFaceTopo topo = (LuFaceTopo) rowTopo;

                    if (!newLinkPids.contains(topo.getLinkPid())) {
                        isSame = false;
                        break;
                    }
                }
                if (isSame) {
                    this.setCheckResult("", "", 0);

                    return;
                }
            }

        }
    }

    private void checkZoneFace(ZoneFace newFace) throws Exception {

        ZoneFaceSelector selector = new ZoneFaceSelector(this.getConn());

        Set<Integer> newLinkPids = new HashSet<>();

        for (IRow rowTopo : newFace.getFaceTopos()) {

            ZoneFaceTopo topo = (ZoneFaceTopo) rowTopo;

            newLinkPids.add(topo.getLinkPid());
        }

        for (int linkPid : newLinkPids) {

            List<ZoneFace> existFaces = selector.loadZoneFaceByLinkId(linkPid, false);

            for (ZoneFace existFace : existFaces) {
                //组成link数不同
                if (newLinkPids.size() != existFace.getFaceTopos().size()) {
                    continue;
                }
                //组成link是否相同
                boolean isSame = true;

                for (IRow rowTopo : existFace.getFaceTopos()) {

                    ZoneFaceTopo topo = (ZoneFaceTopo) rowTopo;

                    if (!newLinkPids.contains(topo.getLinkPid())) {
                        isSame = false;
                        break;
                    }
                }
                if (isSame) {
                    this.setCheckResult("", "", 0);

                    return;
                }
            }

        }
    }

}