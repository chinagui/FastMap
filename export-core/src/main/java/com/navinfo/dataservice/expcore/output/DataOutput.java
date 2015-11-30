package com.navinfo.dataservice.expcore.output;

import java.sql.ResultSet;

import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.expcore.target.tablerename.TableReName;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-1-27
 * Time: 上午10:59
 */
public interface DataOutput {

	public void initTarget() throws ExportException;
	public void releaseTarget();
	
    /**
     * @param resultSet
     * @param tableName
     * @param reNameTo
     * @throws Exception
     */
    public void output(ResultSet resultSet, String tableName, String reNameTo)
            throws Exception;

    /**
     * @param resultSet
     * @param tableName
     * @throws Exception
     */
    public void output(ResultSet resultSet, String tableName)
            throws Exception;

    public void addTableReName(TableReName tableReName);

    public void clearTableReName();
}
