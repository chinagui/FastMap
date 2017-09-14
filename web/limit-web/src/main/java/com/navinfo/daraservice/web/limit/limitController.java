package com.navinfo.daraservice.web.limit;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * Created by ly on 2017/9/14.
 */

@Controller
public class limitController  extends BaseController {

    @RequestMapping(value = "/test")
    public ModelAndView test(HttpServletRequest request) throws ServletException, IOException {
        return new ModelAndView("jsonView", success());
    }
}