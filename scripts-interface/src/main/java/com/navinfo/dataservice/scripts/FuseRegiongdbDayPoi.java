package com.navinfo.dataservice.scripts;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.diff.merge.DayRegionPoiMerge;
import com.navinfo.dataservice.engine.man.region.RegionService;

/**
 * @ClassName: refreshFmgdbRoad
 * @author Xiao Xiaowen
 * @date 2016年5月26日 下午5:42:06
 * @Description: TODO
 * 
 */
public class FuseRegiongdbDayPoi {
	
	private static Logger log = LoggerRepos.getLogger(FuseRegiongdbDayPoi.class);

	public static void main(String[] args) {

		try {

			JobScriptsInterface.initContext();

			List<Region> list = RegionService.getInstance().list();

			List<DayRegionPoiMerge> merges = new ArrayList<DayRegionPoiMerge>();
			
			for (Region region : list) {
				DayRegionPoiMerge merge = new DayRegionPoiMerge(region.getDailyDbId());
				
				merge.run();
				
				merges.add(merge);
			}
			
			int i=0;
			for(Region region : list){
				DayRegionPoiMerge merge = merges.get(i);
				
				log.info("region "+ region.getRegionId() + "," + region.getDailyDbId());

				log.info("   poi update:"+merge.getPoiCount());
				
				log.info("   log_operation create:"+merge.getLogOpCount());
				
				log.info("   log_detail create:"+merge.getLogDetailCount());
				
				log.info("   log_detail_grid create:"+merge.getLogDetailGridCount());
				
				log.info("   log_day_release create:"+merge.getLogDayReleaseCount());
				
				i++;
			}

			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}

	}
}
