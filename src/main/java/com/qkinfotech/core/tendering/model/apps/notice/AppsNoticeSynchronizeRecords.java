package com.qkinfotech.core.tendering.model.apps.notice;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 公告信息同步
 */
@Getter
@Setter
@Entity
@Table(name = "apps_notice_synchronize_records")
@SimpleModel(url = "apps/notice/synchronize/records")
public class AppsNoticeSynchronizeRecords extends BaseEntity {
    /**
     * 同步数据
     */
    @Column(name = "f_synchronized_data",length = 1000)
    private String synchronizedData;

    /**
     * 同步时间
     */
    @Column(name = "f_synchronized_date",length = 200)
    private Date synchronizedDate;


}
