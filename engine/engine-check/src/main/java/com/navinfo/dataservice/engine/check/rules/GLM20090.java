package com.navinfo.dataservice.engine.check.rules;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 立交检查：已有立交的地方，不可以再次制作立交；如果需要制作，可以先删除原始立交，然后重新制作
* @ClassName: GLM20090 
* @author Zhang Xiaolong
* @date 2017年1月11日 上午9:58:08 
* @Description: TODO
 */
public class GLM20090 extends baseRule {

	public GLM20090() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		
		for(IRow obj:checkCommand.getGlmList()){
			//收费站 create
			if(obj instanceof RdGsc ){
				RdGsc rdGsc = (RdGsc)obj;
				Geometry gscGeo = rdGsc.getGeometry();
				List<IRow> gscLinkList = rdGsc.getLinks();
				boolean flag = checkIsHasGsc(gscGeo,gscLinkList);
				if(flag){
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}

	}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
	}
	
	/**
	 * 检查线上是否已经在已知点位存在立交点
	 * 
	 * @param gscGeo
	 * @param collection
	 *            立交组成线的对象集合
	 * @param conn
	 * @return boolean
	 * @throws Exception
	 */
	public boolean checkIsHasGsc(Geometry gscGeo, Collection<IRow> collection)
			throws Exception {
		boolean flag = false;

		RdGscSelector selector = new RdGscSelector(getConn());

		for (IRow row : collection) {
			RdGscLink gscLink = (RdGscLink) row;
			List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPid(gscLink.getLinkPid(), gscLink.getTableName(), true);
			for (RdGsc gsc : rdGscList) {
				if (gsc.getGeometry().distance(GeoTranslator.transform(gscGeo, 1, 0))<1) {
					return true;
				}
			}
		}

		return flag;
	}
}
