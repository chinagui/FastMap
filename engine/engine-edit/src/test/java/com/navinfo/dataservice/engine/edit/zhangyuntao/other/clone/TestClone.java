package com.navinfo.dataservice.engine.edit.zhangyuntao.other.clone;

import com.navinfo.dataservice.dao.glm.model.lc.LcNode;

/**
 * @Title: TestClone.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月27日 上午10:58:00
 * @version: v1.0
 */
public class TestClone {
	public static void main(String[] args) {
		LcNode sNode = new LcNode();
		sNode.setPid(1);
//		LcNode eNode = sNode.clone();
		LcNode eNode = null;
		sNode.setPid(2);
		System.out.println("sNodePid=" + sNode.getPid() + ", eNodePid=" + eNode.getPid());
	}
}
