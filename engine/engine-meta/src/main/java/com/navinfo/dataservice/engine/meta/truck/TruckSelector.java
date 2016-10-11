package com.navinfo.dataservice.engine.meta.truck;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.sql.DBUtils;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 
 * @author jch
 *
 */
public class TruckSelector {
    private Logger log = LoggerRepos.getLogger(this.getClass());

    private Connection conn;

    public TruckSelector() {

    }

    public TruckSelector(Connection conn) {
        this.conn = conn;
    }

    /**
     * 查詢卡车标识，具体业务逻辑如下：
	  (1) 作业poi分类在元数据库sc_point_truck.kind中，
		      如果sc_point_truck.type=1，则poi.truck赋值sc_point_truck.truck;
		      如果sc_point_truck.type=3，且poi.brands.code=sc_point_truck.chain，则poi.truck赋值sc_point_truck.truck;
		     如果sc_point_truck.type=4，且poi.gasStation.fuelType包含"0"，则poi.truck赋值sc_point_truck.truck;
	  (2)作业poi分类不在元数据库sc_point_truck.kind中，
			如果type=2，且poi.brands.code=sc_point_truck.chain，则poi.truck赋值sc_point_truck.truck;
	  (3)以上，如果均不满足，则poi.truck赋默认值0；
     * @param kind
     * @param chain
     * @param fuelType
     * @return
     * @throws Exception
     */
    @SuppressWarnings("resource")
	public int getTruck(String kind,String chain,String fuelType) throws Exception {
        String sql = "SELECT chain,type,truck FROM sc_point_truck t WHERE t.kind=:1";
        String sqlChain = "SELECT truck FROM sc_point_truck t WHERE t.type=2 and t.chain=:1";
        ResultSet resultSet = null;
        PreparedStatement pstmt = null;
        int truck=0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, kind);
            resultSet = pstmt.executeQuery();
            if (resultSet.getRow()>0){
            	while (resultSet.next()) {
                    String chainCode = resultSet.getString("chain");
                    int type = resultSet.getInt("type");
                    if (1==type){
                    	return resultSet.getInt("truck");
                    }else if (3==type&&chain.equals(chainCode)){
                    	return resultSet.getInt("truck");
                    }else if (4==type&&fuelType.contains("0")){
                    	return resultSet.getInt("truck");
                    }     
                }
            }
            else{
            	 pstmt = conn.prepareStatement(sqlChain);
            	 pstmt.setString(1, chain);
                 resultSet = pstmt.executeQuery();
                 while (resultSet.next()) {
                	 return resultSet.getInt("truck"); 
                 }
            }
            return truck;
        } catch (Exception e) {
            throw e;
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
    }
}
