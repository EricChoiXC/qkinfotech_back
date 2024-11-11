package com.qkinfotech.core.tendering.model.apps.shortcuts;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.org.model.OrgElement;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 快捷方式可使用者
 */
@Getter
@Setter
@Entity
@Table(name = "apps_shortcuts_used")
@SimpleModel(url = "apps/shortcuts/used")
public class AppsShortcutsUsed  extends BaseEntity {

    @JoinColumn(name = "f_used_id")
    @ManyToOne(fetch=FetchType.LAZY)
    private OrgElement fUsed;

}
