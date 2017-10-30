package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName: FMBATPA20003
 * @author: zhangpengpeng
 * @date: 2017年10月26日
 * @Desc: 查询条件： 新增点门牌，或修改点门牌且修改内容为DP_NAME 批处理： 1)
 *        DP_NAME中包含SC_POINT_POINTADDRCK中TYPE=7中这些关键字且不以这些关键字结尾时，
 *        则address_flag赋值1； 2)
 *        DP_NAME中包含SC_POINT_POINTADDRCK中TYPE=8中含有指示性方向词语(如“东侧”、“东边”、“旁边”、“对面”、“
 *        东”等词语)且在最后位置时，则address_flag赋值2； 3)
 *        若DP_NAME没有出现任何全角的数字(0~9，〇零~九、十、百、千)或字母(不区分大小写)，则address_flag赋值2；
 *        当1和3同时出现时，address_flag赋值2； 以上，生成履历；
 */
public class FMBATPA20003 extends BasicBatchRule {

	public static MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");

	private List<String> pointAddrckType7 = new ArrayList<>();
	private List<String> pointAddrckType8 = new ArrayList<>();

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() throws Exception {
		pointAddrckType7 = metadataApi.getPointAddrck(7);
		pointAddrckType8 = metadataApi.getPointAddrck(8);
		Map<Long, BasicObj> rows = getRowList();
		for (Long key : rows.keySet()) {
			BasicObj obj = rows.get(key);
			if (!obj.objName().equals(ObjectName.IX_POINTADDRESS)) {
				continue;
			}
			IxPointaddress ixPonitaddress = (IxPointaddress) obj.getMainrow();
			if (ixPonitaddress.getHisOpType().equals(OperationType.INSERT)
					|| (ixPonitaddress.getHisOpType().equals(OperationType.UPDATE)
							&& ixPonitaddress.hisOldValueContains(IxPointaddress.DP_NAME))) {
				String dpName = ixPonitaddress.getDpName() == null ? "" : ixPonitaddress.getDpName();
				for (String keyWord : pointAddrckType7) {
					if(dpName.contains(keyWord) && !dpName.endsWith(keyWord)){
						ixPonitaddress.setAddressFlag(1);
						break;
					}
				}
				for( String keyWord: pointAddrckType8){
					if(dpName.contains(keyWord) && dpName.endsWith(keyWord)){
						ixPonitaddress.setAddressFlag(2);
						break;
					}
				}
		    	Pattern pattern = Pattern.compile("[〇零一二三四五六七八九十百千０-９ａ-ｚＡ-Ｚ]");
				Matcher match = pattern.matcher(dpName);
				if (!match.find()) {
					ixPonitaddress.setAddressFlag(2);
				}
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String dpName = "人Ｅ民";
    	Pattern pattern = Pattern.compile("[〇零一二三四五六七八九十百千０-９ａ-ｚＡ-Ｚ]");
		Matcher match = pattern.matcher(dpName);
		if(match.find()){
			System.out.println("haha");
		}
	}
}
