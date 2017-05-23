package com.navinfo.dataservice.dao.glm.selector.cmg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuilding;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.exception.DAOException;

public class CmgBuildingSelector extends AbstractSelector{
	
	   /**
     * 日志记录类
     */
    private Logger logger = Logger.getLogger(CmgBuildfaceSelector.class);

    public CmgBuildingSelector(Connection conn) {
        super(CmgBuilding.class, conn);
    }

    public List<CmgBuilding> loadCmgBuildingByFacePid(int facePid,boolean isLock) throws Exception{
    	List<CmgBuilding> result=new ArrayList<CmgBuilding>();
        String sql = "SELECT t1.* FROM CMG_BUILDING t1, CMG_BUILDFACE t2 where t1.PID = t2.BUILDING_PID AND "
                + "t2.FACE_PID = :1 and t1.U_RECORD <> 2 and t2.U_RECORD <> 2";
        if (isLock) {
            sql += " for update nowait";
        }
        
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        
        try {
            pstmt = getConn().prepareStatement(sql);
            pstmt.setInt(1, facePid);
            resultSet = pstmt.executeQuery();
            
            while(resultSet.next()){
            	CmgBuilding cmgBuild=new CmgBuilding();
            	ReflectionAttrUtils.executeResultSet(cmgBuild, resultSet);
            	result.add(cmgBuild);
            }
        } catch (SQLException e) {
            logger.error("method listTheAssociatedFaceOfTheLink error. [ sql : " + sql + " ] ");
            throw new DAOException(e.getMessage());
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return result;
    }
}
