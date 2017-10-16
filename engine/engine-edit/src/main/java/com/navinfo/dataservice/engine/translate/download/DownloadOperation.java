package com.navinfo.dataservice.engine.translate.download;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.tranlsate.entity.TranslateLog;
import com.navinfo.dataservice.dao.tranlsate.selector.TranslateOperator;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;

/**
 * @Title: DownloadOperation
 * @Package: com.navinfo.dataservice.engine.translate.download
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/16/2017
 * @Version: V1.0
 */
public class DownloadOperation {

    public TranslateLog download(JSONObject json) throws Exception{
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getMkConnection();

            TranslateOperator operator = new TranslateOperator(conn);
            TranslateLog log = operator.get(json);

            return log;
        } catch (Exception e) {
            DbUtils.rollback(conn);
            throw e;
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }
}
