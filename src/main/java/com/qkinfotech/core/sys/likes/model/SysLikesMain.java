package com.qkinfotech.core.sys.likes.model;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgPerson;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.C;

import java.util.Date;

@Getter
@Setter
@SimpleModel(url = "/sys/likes/main")
@Table(name = "sys_likes_main")
@Entity
public class SysLikesMain extends BaseEntity {

    @JoinColumn(name = "f_person_id")
    @ManyToOne(fetch= FetchType.LAZY)
    private OrgPerson fPerson;

    @Column
    private Date fLikeTime;

    @Column(nullable = false)
    private Boolean fIsLike = false;

    @Column
    private Date fUnlikeTime;

    @Column(nullable = false)
    private Boolean fIsUnlike = false;

    @Column(length = 200)
    private String fModelName;

    @Column(length = 200)
    private String fModelId;

}
