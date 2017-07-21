package com.navinfo.dataservice.web.fcc.controller;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.audio.AudioGetter;
import com.navinfo.dataservice.engine.photo.PhotoGetter;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by zhangjunfang on 2017/6/28.
 */

@Controller
public class AudioController extends BaseController {
    private static final Logger logger = Logger.getLogger(AudioController.class);

    @RequestMapping(value = "/audio/getSnapshotByRowkey")
    public void getSnapshotByRowkey(HttpServletRequest request,
                                    HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("audio/mpeg;charset=GBK");

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods",
                "POST, GET, OPTIONS, DELETE,PUT");

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String uuid = jsonReq.getString("rowkey");

            AudioGetter getter = new AudioGetter();

            byte[] data = getter.getAudioByRowkey(uuid);

            response.getOutputStream().write(data);

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        }

    }
}
