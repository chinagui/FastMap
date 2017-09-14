package com.navinfo.dataservice.engine.edit.operation.edge;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import org.springframework.util.ObjectUtils;

/**
 * @Title: TransactionFactory
 * @Package: com.navinfo.dataservice.engine.edit.operation
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 9/14/2017
 * @Version: V1.0
 */
public class TransactionFactory {

    private TransactionFactory() {
    }

    /**
     * 初始化Command信息
     *
     * @param request
     * @return
     * @throws Exception
     */
    public static AbstractCommand generateCommand(Transaction transaction, String request) throws Exception {
        AbstractCommand command = transaction.createCommand(request);
        command.setUserId(transaction.getUserId());
        command.setTaskId(transaction.getSubTaskId());
        command.setDbType(transaction.getDbType());
        command.setInfect(transaction.getInfect());
        command.setHasConn(!ObjectUtils.isEmpty(transaction.getConn()));
        return command;
    }
}
