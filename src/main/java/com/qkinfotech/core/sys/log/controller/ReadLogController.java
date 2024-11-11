package com.qkinfotech.core.sys.log.controller;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.sys.log.service.ReadLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/readLog")
@Slf4j
public class ReadLogController {

    @Autowired
    private ReadLogService readLogService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/readDocument")
    public void readDocument() throws Exception {
        try {
            readLogService.readDocument(request, response);
        } catch (Exception e) {
            logger.error("生成阅读记录失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    @RequestMapping("/readAttachment")
    public void readAttachment() throws Exception {
        try {
            readLogService.readAttachment(request, response);
        } catch (Exception e) {
            logger.error("生成阅读记录失败：" + e.getMessage());
            e.printStackTrace();
        }
    }



}
