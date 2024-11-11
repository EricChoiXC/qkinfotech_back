package com.qkinfotech.core.tendering.model.apps.collection;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 征集结果
 */
@Getter
@Setter
@Entity
@Table(name = "apps_collection_result")
@SimpleModel(url = "apps/collection/result")
public class AppsCollectionResult extends BaseEntity {
    /**
     * 文档id
     */
    @JoinColumn(name = "f_main_id")
    @ManyToOne
    private AppsProjectMain fMainId;

    /**
     * 总金额
     */
    @Column(name = "f_total_price",length = 1000)
    private Float fTotalPrice;

    /**
     * 确认征集结果日期
     */
    @Column(name = "f_confirm_time",length = 100)
    private Date fConfirmTime;
}

