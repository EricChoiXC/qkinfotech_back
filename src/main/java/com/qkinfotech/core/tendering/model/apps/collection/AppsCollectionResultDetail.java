package com.qkinfotech.core.tendering.model.apps.collection;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgPerson;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 征集结果明细
 */
@Getter
@Setter
@Entity
@Table(name = "apps_collection_result_detail")
@SimpleModel(url = "apps/collection/result/detail")
public class AppsCollectionResultDetail extends BaseEntity {
    /**
     * 征集结果id
     */
    @JoinColumn(name = "f_result_id")
    @ManyToOne
    private AppsCollectionResult fResultId;

    /**
     * 评审排序
     */
    @Column(name = "f_index",length = 200)
    private String fIndex;

    /**
     * 公司
     */
    @JoinColumn(name = "f_company")
    @ManyToOne
    private OrgPerson fCompany;

    /**
     * 方案征集费
     */
    @Column(name = "f_collection_price",length = 36)
    private Float fCollectionPrice;

    /**
     * 奖金
     */
    @Column(name = "f_price",length = 36)
    private Float fPrice;

    /**
     * 备注
     */
    @Column(name = "f_remark",length = 200)
    private String fRemark;

}
