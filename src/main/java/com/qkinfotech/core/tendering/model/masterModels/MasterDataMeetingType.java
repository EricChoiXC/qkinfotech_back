package com.qkinfotech.core.tendering.model.masterModels;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="masterdata_meeting_type")
@SimpleModel(url = "masterdata/meetingType")
public class MasterDataMeetingType extends BaseEntity {
    @Column(name = "f_name", length = 36)
    private String fName;

    @Column(name = "f_key", length = 36)
    private String fKey;

    /**
     *   1 : 资格预审
     *   2 : 汇报评审
     */
    @Column(name = "f_meeting_key", length = 36)
    private String fMeetingKey;

}
