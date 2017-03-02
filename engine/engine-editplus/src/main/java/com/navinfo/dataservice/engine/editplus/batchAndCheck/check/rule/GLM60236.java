package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
/**
 * GLM60236
 * 检查条件：非删除POI且存在父子关系
 * 重复检查：
 * 一子多父
 * 循环检查：
 * 在一组父子关系中，其中一个POI在这组关系中既充当其它POI的父亲，又充当了其它POI的子，
 * 则认为这组POI存在循环建立父子关系，报LOG：POI存在循环建立父子关系！
 * 例如：一组父子关系为A->B->A，则认为 A->B，与B->A是循环建立了
 * @author zhangxiaoyi
 */
public class GLM60236 extends BasicCheckRule {
	Map<Long, Long> parentMap = new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//循环检查
			Long pid=poi.getPid();
			int whileNum=0;
			Set<Long> parentPids=new HashSet<Long>();
			Long pPid=pid;
			while(parentMap.containsKey(pPid)){
				//一般父子关系层次会在6层以内，增加判断，防止代码错误导致的死循环
				if(whileNum>6){return;}
				else{whileNum++;}
				pPid=parentMap.get(pPid);
				parentPids.add(pPid);
				if(parentPids.contains(pid)){
					String targets=parentPids.toString().replace("[", "(").replace(",", "];[IX_POI,").replace("(", "[IX_POI,");
					log.info(targets);
					setCheckResult(poi.getGeometry(), targets,poi.getMeshId(),"POI存在循环建立父子关系");
					return;
				}
			}
		}
	}
			//重复检查
//			//父找子，先找到叶子子
//			List<IxPoiChildren> childs = poiObj.getIxPoiChildrens();
//			if(childs==null||childs.size()==0){
//				//没有子，只能找父
//				//无父无子 不检查
//				if(!parentMap.containsKey(pid)){return;}
//				Set<Long> oldParentSet=new HashSet<Long>();
//				Set<Long> newParentSet=parentMap.get(pid);
//				int oldtotal=oldParentSet.size();
//				int newtotal=newParentSet.size();
//				whileNum=0;
//				while(true){
//					//一般父子关系层次会在6层以内，增加判断，防止代码错误导致的死循环
//					if(whileNum>6){return;}
//					else{whileNum++;}
//					oldParentSet.addAll(newParentSet);
//					//旧父包含新父，说明重复，相同的子，通过不同父子关系树能找到相同父
//					if(oldParentSet.size()<oldtotal+newtotal){
//						for(Long pid2:oldParentSet){targets=targets+";[IX_POI,"+pid2+"]";}
//						setCheckResult(poi.getGeometry(), targets,poi.getMeshId(),"POI存在父子关系重复");
//						return;
//					}
//					newParentSet=new HashSet<Long>();
//					Set<Long> tmpParentSet=newParentSet;
//					for(Long tmp:tmpParentSet){
//						if(parentMap.containsKey(tmp)){newParentSet.addAll(parentMap.get(tmp));}
//					}
//					oldtotal=oldParentSet.size();
//					newtotal=newParentSet.size();
//					//没有新的父，即所有路径的父子关系均找到，查询结束
//					if(newtotal==0){return;}
//				}
//			}else if(!parentMap.containsKey(pid)){//有子无父
//				Set<Long> oldChildSet=new HashSet<Long>();
//				Set<Long> newChildSet=new HashSet<Long>();
//				for(IxPoiChildren c:childs){newChildSet.add(c.getChildPoiPid());}
//				int oldtotal=oldChildSet.size();
//				int newtotal=newChildSet.size();
//				whileNum=0;
//				while(true){
//					//一般父子关系层次会在6层以内，增加判断，防止代码错误导致的死循环
//					if(whileNum>6){return;}
//					else{whileNum++;}
//					oldChildSet.addAll(newChildSet);
//					//旧子包含新子，说明重复，相同的父，通过不同父子关系树能找到相同子
//					if(oldChildSet.size()<oldtotal+newtotal){
//						for(Long pid2:oldChildSet){targets=targets+";[IX_POI,"+pid2+"]";}
//						setCheckResult(poi.getGeometry(), targets,poi.getMeshId(),"POI存在父子关系重复");
//						return;
//					}
//					newChildSet=new HashSet<Long>();
//					Set<Long> tmpChildSet=newChildSet;
//					for(Long tmp:tmpChildSet){
//						BasicObj basicObj=referObjs.get(tmp);
//						if(basicObj.isDeleted()){continue;}
//						IxPoiObj cpoiObj=(IxPoiObj) basicObj;
//						List<IxPoiChildren> cChilds = cpoiObj.getIxPoiChildrens();
//						if(cChilds==null||cChilds.size()==0){continue;}
//						for(IxPoiChildren c:cChilds){newChildSet.add(c.getChildPoiPid());}
//					}
//					oldtotal=oldChildSet.size();
//					newtotal=newChildSet.size();
//					//没有新的子，即所有路径的父子关系均找到，查询结束
//					if(newtotal==0){return;}
//				}
//			}else{//有父有子
//				//先找到叶子子，即没有子只有父的集合
//				Set<Long> oldChildSet=new HashSet<Long>();
//				Set<Long> newChildSet=new HashSet<Long>();
//				Set<Long> overChildSet=new HashSet<Long>();
//				for(IxPoiChildren c:childs){newChildSet.add(c.getChildPoiPid());}
//				int oldtotal=oldChildSet.size();
//				int newtotal=newChildSet.size();
//				whileNum=0;
//				while(true){
//					//一般父子关系层次会在6层以内，增加判断，防止代码错误导致的死循环
//					if(whileNum>6){return;}
//					else{whileNum++;}
//					oldChildSet.addAll(newChildSet);
//					//旧子包含新子，说明重复，相同的父，通过不同父子关系树能找到相同子
//					if(oldChildSet.size()<oldtotal+newtotal){
//						for(Long pid2:oldChildSet){targets=targets+";[IX_POI,"+pid2+"]";}
//						setCheckResult(poi.getGeometry(), targets,poi.getMeshId(),"POI存在父子关系重复");
//						return;
//					}
//					newChildSet=new HashSet<Long>();
//					Set<Long> tmpChildSet=newChildSet;
//					for(Long tmp:tmpChildSet){
//						BasicObj basicObj=referObjs.get(tmp);
//						if(basicObj.isDeleted()){continue;}
//						IxPoiObj cpoiObj=(IxPoiObj) basicObj;
//						List<IxPoiChildren> cChilds = cpoiObj.getIxPoiChildrens();
//						//没有子，说明是叶子子
//						if(cChilds==null||cChilds.size()==0){overChildSet.add(cpoiObj.objPid());continue;}
//						for(IxPoiChildren c:cChilds){newChildSet.add(c.getChildPoiPid());}
//					}
//					oldtotal=oldChildSet.size();
//					newtotal=newChildSet.size();
//					//没有新的子，即所有路径的父子关系均找到，所有叶子子均获取到，退出循环
//					if(newtotal==0){break;}
//				}
//				//以所有叶子子分别为起点，查是否重复
//				for(Long cPid:overChildSet){
//					//没有子，只能找父
//					//无父无子 不检查
//					targets="[IX_POI,"+cPid+"]";
//					Set<Long> oldParentSet=new HashSet<Long>();
//					Set<Long> newParentSet=parentMap.get(cPid);
//					int oldtotal2=oldParentSet.size();
//					int newtotal2=newParentSet.size();
//					whileNum=0;
//					while(true){
//						//一般父子关系层次会在6层以内，增加判断，防止代码错误导致的死循环
//						if(whileNum>6){return;}
//						else{whileNum++;}
//						oldParentSet.addAll(newParentSet);
//						//旧父包含新父，说明重复，相同的子，通过不同父子关系树能找到相同父
//						if(oldParentSet.size()<oldtotal2+newtotal2){
//							for(Long pid2:oldParentSet){targets=targets+";[IX_POI,"+pid2+"]";}
//							setCheckResult(poi.getGeometry(), targets,poi.getMeshId(),"POI存在父子关系重复");
//							return;
//						}
//						newParentSet=new HashSet<Long>();
//						Set<Long> tmpParentSet=newParentSet;
//						for(Long tmp:tmpParentSet){
//							if(parentMap.containsKey(tmp)){newParentSet.addAll(parentMap.get(tmp));}
//						}
//						oldtotal2=oldParentSet.size();
//						newtotal2=newParentSet.size();
//						//没有新的父，即所有路径的父子关系均找到，查询结束
//						if(newtotal2==0){return;}
//					}
//				}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			IxPoiObj poiObj=(IxPoiObj) obj;
			List<IxPoiChildren> childs = poiObj.getIxPoiChildrens();
			if(childs==null||childs.size()==0){continue;}
			for(IxPoiChildren c:childs){pidList.add(c.getChildPoiPid());}
		}
		parentMap = IxPoiSelector.getAllParentChildByPids(getCheckRuleCommand().getConn(), pidList);
	}

}
