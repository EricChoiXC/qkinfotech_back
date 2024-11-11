package com.qkinfotech.core.tendering.model.apps.meeting;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


/**
 * 会议表
 */
@Getter
@Setter
@Entity
@Table(name = "meeting_main")
@SimpleModel(url = "meeting/main")
public class MeetingMain extends BaseEntity {

    /**
     * 所属会议
     */
    @Column(name = "f_model_name",length = 200)
    private String fModelName;


    /**
     * 所属会议id
     */
    @Column(name = "f_model_id",length = 200)
    private String fModelId;

    /**
     * 会议名称
     */
    @Column(name = "f_name",length = 36)
    private String fName;

    /**
     * 开始时间
     */
    @Column(name = "f_start_time",length = 200)
    private Date fStartTime;


    /**
     * 结束时间
     */
    @Column(name = "f_finish_time",length = 200)
    private Date fFinishTime;


    /**
     * 会议地点
     */
    @Column(name = "f_place",length = 200)
    private String fPlace;

    /**
     * 内网id
     */
    @Column(name = "f_in_id", length = 200)
    private String fInId;

    /**
     * 数据来源：pm/ekp/in
     */
    @Column(name = "f_from",length = 200)
    private String fFrom;

    /**
     * 是否已编辑：ekp/in来源使用，此处来源的为会议室预定，未填写包件信息，因此需要有一次修改权限
     */
    @Column(name = "f_edited")
    private Boolean fEdited;


}
