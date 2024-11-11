package com.qkinfotech.core.sys.likes.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SimpleModel(url = "/sys/likes/count")
@Table(name = "sys_likes_count")
@Entity
public class SysLikesCount extends BaseEntity {

    @Column(length = 200)
    private String fModelName;

    @Column(length = 200)
    private String fModelId;

    @Column
    private Integer fLikeCount = 0;

    @Column
    private Integer fUnlikeCount = 0;
}
