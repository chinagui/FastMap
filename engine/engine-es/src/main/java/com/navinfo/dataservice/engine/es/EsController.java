package com.navinfo.dataservice.engine.es;

import com.navinfo.dataservice.api.es.model.TrackPoint;
import com.navinfo.dataservice.commons.constant.EsConstant;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;

import java.util.List;

public class EsController {
    private static final Logger logger = Logger.getLogger(EsController.class);

    public void insert(List<TrackPoint> trackPoints) throws Exception{
        if(trackPoints==null || trackPoints.isEmpty()){
            return;
        }

        TransportClient client = EsConnector.getInstance().getClient();
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for(TrackPoint trackPoint : trackPoints){
            JSONObject json=null;
            try {
                json = trackPoint.toJSON();
            }catch (Exception ex){
                logger.error("convert json error:"+trackPoint.getId());
                logger.error(ex);
            }
            bulkRequest.add(client.prepareIndex(EsConstant.index_trackpoints, EsConstant.index_trackpoints, trackPoint.getId()).setSource(json));

        }

        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            logger.error(bulkResponse.buildFailureMessage());
        }
    }
}
