package com.navinfo.dataservice.engine.edit.operation.batch.rdlink;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;

import com.navinfo.dataservice.dao.glm.iface.Result;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

import com.navinfo.dataservice.engine.edit.operation.batch.BatchRuleType;

/**
 * 
 * @Title: Operation.java
 * @Description: RdLink 批处理操作类
 * @author 赵凯凯
 * @date 2016年8月15日 下午2:31:50
 * @version V1.0
 */
public class Operation implements IOperation {

	private Command command;
	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;

	}

	@Override
	public String run(Result result) throws Exception {
		this.batchRdLink(result);
		return null;
	}

	private void batchRdLink(Result result) throws Exception {
		if (StringUtils.isNotEmpty(this.command.getRuleId())) {
			BatchRuleType ruleType = Enum.valueOf(BatchRuleType.class,
					this.command.getRuleId());
			switch (ruleType) {
			case BATCHUBAN:
				this.batchUrbanLink(result);
				break;
			default:
				break;
			}
		} else {
			throw new Exception("规则号不能为空");
		}
	}

	private void batchUrbanLink(Result result) {
		// 1.通过face查找符合的link
		List<RdLink> links = null;
		// 算法调用公共的接口...正在开发
		for (RdLink link : links) {
			if (link.getUrban() == 1)
				continue;
			link.changedFields().put("urban", 1);
			result.insertObject(link, ObjStatus.UPDATE, link.getPid());
		}

	}

}
