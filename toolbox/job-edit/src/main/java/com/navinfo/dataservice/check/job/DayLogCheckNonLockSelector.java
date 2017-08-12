package com.navinfo.dataservice.check.job;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.impcore.selector.LogSelector;
import org.apache.log4j.Logger;

import java.sql.Connection;

/**
 * Created by ly on 2017/8/1.
 */
public class DayLogCheckNonLockSelector extends LogSelector {


    private Logger log = Logger
            .getLogger(DayLogCheckNonLockSelector.class);

    String lastOpId = null;

    public DayLogCheckNonLockSelector(OracleSchema logSchema,String lastOpId) {
        super(logSchema);

        this.lastOpId = lastOpId;
    }

    @Override
    protected int selectLog(Connection conn) throws Exception {

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(tempTable);

        if (lastOpId == null) {

            sb.append(" SELECT P.OP_ID,P.OP_DT,P.OP_SEQ FROM LOG_OPERATION P ");

        } else {
            sb.append(" SELECT P.OP_ID,P.OP_DT,P.OP_SEQ FROM LOG_OPERATION P WHERE P.OP_DT > (SELECT OP_DT FROM LOG_OPERATION ");
            sb.append(" WHERE OP_ID = '");
            sb.append(lastOpId);
            sb.append("')");
        }

        log.debug( sb.toString());

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
