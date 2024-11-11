package com.qkinfotech.core.tendering.model.masterModels;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目  类别 模式  中间表
 */
@Getter
@Setter
@Entity
@Table(name="masterdata_category_mode")
@SimpleModel(url = "masterdata/categoryMode")
public class MasterDataCategoryMode extends BaseEntity {
    /**
     * 类别
     * */
    @JoinColumn(name = "f_master_data_category_id")
    @ManyToOne
        private MasterDataCategory fMasterDataCategoryId;
    /**
     * 模式
     */
    @JoinColumn(name = "f_master_data_mode_id")
    @ManyToOne
    private MasterDataMode fMasterDataModeId;
}
