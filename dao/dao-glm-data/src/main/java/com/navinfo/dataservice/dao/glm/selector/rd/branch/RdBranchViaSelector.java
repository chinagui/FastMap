package com.navinfo.dataservice.dao.glm.selector.rd.branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdBranchViaSelector extends AbstractSelector {

    private Connection conn;

    public RdBranchViaSelector(Connection conn) {
        super(conn);
        this.conn = conn;
        this.setCls(RdBranchVia.class);
    }

    public List<List<RdBranchVia>> loadRdBranchViaByLinkPid(int linkPid,
                                                            boolean isLock) throws Exception {
        List<List<RdBranchVia>> list = new ArrayList<List<RdBranchVia>>();

        List<RdBranchVia> listVia = new ArrayList<RdBranchVia>();

        String sql = "select a.*,b.s_node_pid,b.e_node_pid,c.node_pid in_node_pid,d.mesh_id from rd_branch_via a,rd_link b,rd_branch c,rd_link d "
                + " where a.link_pid = b.link_pid and a.link_pid = :1 and a.branch_pid = c.branch_pid and c.in_link_pid = d.link_pid "
                + " order by a.branch_pid,a.seq_num  ";

        if (isLock) {
            sql += " for update nowait ";
        }

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
        	pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, linkPid);

            resultSet = pstmt.executeQuery();

            int preBranchPid = 0;

            boolean isChanged = false;

            int preSNodePid = 0;

            int preENodePid = 0;

            int viaSeqNum = 0;

            while (resultSet.next()) {
                RdBranchVia via = new RdBranchVia();

                int tmpBranchPid = resultSet.getInt("branch_pid");

                if (preBranchPid == 0) {
                    preBranchPid = tmpBranchPid;
                } else if (preBranchPid != tmpBranchPid) {
                    isChanged = true;

                    preBranchPid = tmpBranchPid;
                }

                int tempLinkPid = resultSet.getInt("link_pid");

                if (tempLinkPid == linkPid) {
                    viaSeqNum = resultSet.getInt("seq_num");

                } else {
                    preSNodePid = resultSet.getInt("s_node_pid");

                    preENodePid = resultSet.getInt("e_node_pid");
                }

                if (viaSeqNum == 0) {
                    continue;
                }

                ReflectionAttrUtils.executeResultSet(via, resultSet);

                if (!isChanged) {
                    listVia.add(via);

                } else {

                    listVia = this.repaireViaDirect(listVia, preSNodePid,
                            preENodePid, linkPid);

                    list.add(listVia);

                    listVia = new ArrayList<RdBranchVia>();

                    listVia.add(via);

                    isChanged = false;
                }

            }

            if (listVia.size() > 0) {

                listVia = this.repaireViaDirect(listVia, preSNodePid, preENodePid,
                        linkPid);

                list.add(listVia);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
        return list;
    }

    // 维护经过线方向
    public List<RdBranchVia> repaireViaDirect(List<RdBranchVia> vias,
                                              int preSNodePid, int preENodePid, int linkPid) {
        List<RdBranchVia> newVias = new ArrayList<RdBranchVia>();

        for (RdBranchVia v : vias) {
            if (v.getLinkPid() == linkPid) {

                if (preSNodePid != 0 && preENodePid != 0) {
                    if (v.igetsNodePid() == preSNodePid
                            || v.igetsNodePid() == preENodePid) {

                    } else {
                        int tempPid = v.igetsNodePid();

                        v.isetsNodePid(v.igeteNodePid());

                        v.iseteNodePid(tempPid);
                    }
                } else {
                    if (v.igeteNodePid() == v.igetInNodePid()) {
                        int tempPid = v.igetsNodePid();

                        v.isetsNodePid(v.igeteNodePid());

                        v.iseteNodePid(tempPid);
                    }
                }
            }

            newVias.add(v);
        }

        return newVias;
    }
}
