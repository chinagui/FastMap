package com.navinfo.dataservice.web.es;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.engine.es.EsConnector;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
public class EsController extends BaseController{

    @RequestMapping(value = "/test")
    public ModelAndView test(HttpServletRequest request) throws ServletException, IOException {
        return new ModelAndView("jsonView", success());
    }
}
