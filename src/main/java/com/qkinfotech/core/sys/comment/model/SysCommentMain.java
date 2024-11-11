package com.qkinfotech.core.sys.comment.model;


import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgPerson;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@SimpleModel(url = "/sys/comment/main")
@Table(name = "sys_comment_main")
@Entity
public class SysCommentMain extends BaseEntity {

    @JoinColumn(name = "f_person_id")
    @ManyToOne(fetch= FetchType.LAZY)
    private OrgPerson fPerson;

    @Column(length = 100000)
    private String fContent;

    @JoinColumn(name = "f_parent_id")
    @ManyToOne(fetch= FetchType.LAZY)
    private SysCommentMain fParent;

    @JoinColumn(name = "f_reply_id")
    @ManyToOne(fetch= FetchType.LAZY)
    private SysCommentMain fReply;

    @Column
    private Date fCreateTime;

    @Column(length = 200)
    private String fModelName;

    @Column(length = 200)
    private String fModelId;

    @Column
    private Boolean fDelFlag = false;

    @OneToMany(mappedBy = "fParent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = "f_del_flag = false")
    @OrderBy(value = "fCreateTime asc")
    private Set<SysCommentMain> fChildren = new HashSet<SysCommentMain>();
}
