package com.qkinfotech.core.tendering.vo;

import lombok.Data;

import java.util.Date;

@Data
public class AppsCalendarVo {

    /**
     * 日程标题
     */
    private String subject;
    /**
     * 日程显示时间
     */
    private Date date;
    /**
     * 日程主键id
     */
    private String fId;
    /**
     * 日程开始时间
     */
    private String docStartTime;
    /**
     * 日程结束时间
     */
    private String docFinishTime;
    private String fdAppKey;
    private String fdAppUUId;
}
