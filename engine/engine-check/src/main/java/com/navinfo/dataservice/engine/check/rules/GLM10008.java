package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlopeVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * 制作坡度的退出link和延长LINK不能为交叉口内link、不能有IMI属性，不能为CRFI的构成link
 * 
 * @author wangdongbin
 *
 */
public class GLM10008 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
//			if (obj instanceof RdInter) {
//				RdInter rdInter = (RdInter) obj;
//				checkRdInter(rdInter);
//			}    // 3.29修改，取消新增CRFI的触发时机
			if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink);
			} else if (obj instanceof RdLinkForm) {
				RdLinkForm rdLinkForm = (RdLinkForm) obj;
				checkLimit(rdLinkForm);
			} else if (obj instanceof RdSlope) {
				RdSlope rdSlope = (RdSlope) obj;
				checkSlope(rdSlope);
			} else if (obj instanceof RdSlopeVia) {
				RdSlopeVia rdSlopeVia = (RdSlopeVia) obj;
				checkSlopeVia(rdSlopeVia);
			}
		}

	}

	/**
	 * 检查坡度延长线
	 * @param rdSlopeVia
	 * @throws Exception 
	 */
	private void checkSlopeVia(RdSlopeVia rdSlopeVia) throws Exception {
		// 获取坡度延长线
		List<Integer> linkPids = new ArrayList<Integer>();
		linkPids.add(rdSlopeVia.getLinkPid());
		checkLinks(linkPids,rdSlopeVia.getSlopePid());
	}

	/**
	 * 检查坡度退出线
	 * @param rdSlope
	 * @throws Exception 
	 */
	private void checkSlope(RdSlope rdSlope) throws Exception {
		// 获取坡度退出线
		List<Integer> linkPids = new ArrayList<Integer>();
		if (rdSlope.status().equals(ObjStatus.INSERT)) {
			linkPids.add(rdSlope.getLinkPid());
			List<IRow> slopVias = rdSlope.getSlopeVias();
			for (IRow vias:slopVias) {
				RdSlopeVia rdSlopeVia = (RdSlopeVia) vias;
				linkPids.add(rdSlopeVia.getLinkPid());
			}
		} else if (rdSlope.status().equals(ObjStatus.UPDATE)) {
			if (rdSlope.changedFields().containsKey("linkPid")) {
				linkPids.add(Integer.parseInt(rdSlope.changedFields().get("linkPid").toString()));
			}
		}
		if (linkPids.size()>0) {
			checkLinks(linkPids,rdSlope.getPid());
		}
	}

	private void checkLinks(List<Integer> linkPids, int slopePid) throws Exception {
		// 检查link是否为交叉口内link、有IMI属性，或为CRFI的构成link
		StringBuilder sb = new StringBuilder();
		sb.append("select 1");
		sb.append(" from rd_link l, rd_link_form f");
		sb.append(" where l.link_pid = f.link_pid");
		sb.append(" and (l.imi_code != 0 or f.form_of_way = 50 or exists (select 1 from rd_inter_link i where i.link_pid in ("+StringUtils.join(linkPids, ",")+") and i.u_record <> 2))");
		sb.append(" and l.link_pid in ("+StringUtils.join(linkPids, ",")+")");
		sb.append(" and l.u_record <> 2");
		sb.append(" and f.u_record <> 2");
		
		String sql = sb.toString();
		log.info("RdSlope后检查GLM10008:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			String target = "[RD_SLOPE," + slopePid + "]";
			this.setCheckResult("", target, 0);
		}
	}

	/**
	 * 检查LINK形态
	 * @param rdLinkForm
	 * @throws Exception
	 */
	private void checkLimit(RdLinkForm rdLinkForm) throws Exception {
		// 制作坡度的退出link和延长LINK不能为交叉口内link
		int linkPid = 0;
		if (rdLinkForm.status().equals(ObjStatus.INSERT)) {
			if (rdLinkForm.getFormOfWay() == 50) {
				linkPid = rdLinkForm.getLinkPid();
			}
		} else if (rdLinkForm.status().equals(ObjStatus.UPDATE)) {
			if (rdLinkForm.changedFields().containsKey("formOfWay")) {
				if (Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString()) == 50) {
					linkPid = rdLinkForm.getLinkPid();
				}
			}
		}
		if (linkPid != 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("select 1");
			sb.append(" from rd_slope s");
			sb.append(" where (s.link_pid = " + linkPid);
			sb.append(" or exists (select 1 from rd_slope_via v ");
			sb.append(" where v.slope_pid=s.pid and v.u_record <> 2 and v.link_pid = "+linkPid+"))");
			sb.append(" and s.u_record <> 2");
			
			String sql = sb.toString();
			log.info("RdLinkForm后检查GLM10008:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				String target = "[RD_LINK," + linkPid + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}

	/**
	 * 检查Link
	 * @param rdLink
	 * @throws Exception
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		// 制作坡度的退出link和延长LINK不能有IMI属性
		if (rdLink.status().equals(ObjStatus.UPDATE)) {
			if (rdLink.changedFields().containsKey("imiCode")) {
				int imiCode = Integer.parseInt(rdLink.changedFields().get("imiCode").toString());
				if (imiCode != 0) {
					int rdLinkPid = rdLink.getPid();
					StringBuilder sb = new StringBuilder();
					sb.append("select 1");
					sb.append(" from rd_slope s");
					sb.append(" where (s.link_pid = " + rdLinkPid);
					sb.append(" or exists (select 1 from rd_slope_via v ");
					sb.append(" where v.slope_pid=s.pid and v.u_record <> 2 and v.link_pid = "+rdLinkPid+"))");
					sb.append(" and s.u_record <> 2");
					
					String sql = sb.toString();
					log.info("RdLink后检查GLM10008:" + sql);

					DatabaseOperator getObj = new DatabaseOperator();
					List<Object> resultList = new ArrayList<Object>();
					resultList = getObj.exeSelect(this.getConn(), sql);

					if(resultList.size()>0){
						String target = "[RD_LINK," + rdLink.getPid() + "]";
						this.setCheckResult("", target, 0);
					}
				}
			}
			
		}
		
	}

	/**
	 * 检查CRFI
	 * @param rdInter
	 * @throws Exception
	 */
//	private void checkRdInter(RdInter rdInter) throws Exception {
//		// 制作坡度的退出link和延长LINK不能为CRFI的构成link
//		List<IRow> links = rdInter.getLinks();
//		List<Integer> linkPids = new ArrayList<Integer>();
//		for (IRow link:links) {
//			RdInterLink interLink = (RdInterLink) link;
//			linkPids.add(interLink.getLinkPid());
//		}
//		String pids =  StringUtils.join(linkPids, ",");
//		StringBuilder sb = new StringBuilder();
//		sb.append("select 1");
//		sb.append(" from rd_slope s");
//		sb.append(" where (s.link_pid in ("+ pids +")");
//		sb.append(" or exists");
//		sb.append(" (select 1");
//		sb.append(" from rd_slope_via v");
//		sb.append(" where v.slope_pid = s.pid");
//		sb.append(" and v.link_pid in ("+ pids +")))");
//		sb.append(" and s.u_record <> 2");
//		
//		String sql = sb.toString();
//		log.info("RdInter后检查GLM10008:" + sql);
//
//		DatabaseOperator getObj = new DatabaseOperator();
//		List<Object> resultList = new ArrayList<Object>();
//		resultList = getObj.exeSelect(this.getConn(), sql);
//
//		if(resultList.size()>0){
//			String target = "[RD_INTER," + rdInter.getPid() + "]";
//			this.setCheckResult("", target, 0);
//		}
//	}

}
