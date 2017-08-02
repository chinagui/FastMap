package com.navinfo.dataservice.check.job;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.impcore.selector.LogSelector;

import java.sql.Connection;
import java.util.Date;

/**
 * Created by ly on 2017/8/1.
 */
public class DayLogCheckNonLockSelector extends LogSelector {

    Date startData ;

    public DayLogCheckNonLockSelector(OracleSchema logSchema, Date startData) {
        super(logSchema);

        this.startData = startData;
    }

    @Override
    protected int selectLog(Connection conn) throws Exception {

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(tempTable);
        sb.append(" SELECT P.OP_ID,P.OP_DT,P.OP_SEQ FROM LOG_OPERATION P ");

        String timeSqlFormat = DateUtils.dateToString(startData, DateUtils.DATE_COMPACTED_FORMAT);

        sb.append(" WHERE p.op_dt > to_date('"+timeSqlFormat+"', 'yyyymmddhh24miss')\r\n") ;

        return run.update(conn, sb.toString());
    }


    @Override
    protected int extendLog(Connection conn) throws Exception {
        return 0;
    }

    @Override
    protected String getOperationLockSql() {
        return null;
    }

    @Override
    protected String getUnlockLogSql(boolean commitStatus) {
        return null;
    }

}
