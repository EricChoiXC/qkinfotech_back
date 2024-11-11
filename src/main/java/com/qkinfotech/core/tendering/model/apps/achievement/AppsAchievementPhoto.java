package com.qkinfotech.core.tendering.model.apps.achievement;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleModel;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目效果图
 */
@Getter
@Setter
@Entity
@Table(name = "apps_achievement_photo")
@SimpleModel(url = "apps/achievement/photo")
public class AppsAchievementPhoto extends BaseEntity {
    /**
     *业绩id
     */
    @Column(name = "f_performance_id", length = 2000)
    private String fPerformanceId;

    /**
     * 照片附件id
     */
    @JoinColumn(name = "f_photo_attachment_id")
    @ManyToOne
    private AttachmentMain fPhotoAttachmentId;
}
