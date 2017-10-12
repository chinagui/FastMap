package com.navinfo.dataservice.engine.translate.list;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.dao.tranlsate.selector.TranslateSelector;
import com.navinfo.navicommons.database.Page;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;

/**
 * @Title: Operation
 * @Package: com.navinfo.dataservice.engine.translate.list
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/12/2017
 * @Version: V1.0
 */
public class Operation {

    public Page loadPage(JSONObject params) throws Exception{
        Page page = null;
        Connection conn = null;
        try {
            conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
            TranslateSelector selector = new TranslateSelector(conn);
            page = selector.list(params);
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(conn);
        }
        return page;
    }
}
