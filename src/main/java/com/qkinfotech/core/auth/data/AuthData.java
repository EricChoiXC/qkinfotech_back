package com.qkinfotech.core.auth.data;

import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.org.model.OrgElement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthData extends BaseEntity {

	private static final long serialVersionUID = 1L;

	private OrgElement orgElement;
	
	private String targetId;// 文档id，附件id（不是文件）

	private String key;
	
	private boolean read;// 文档：查看，附件：阅读

	private boolean share;// 文档、附件：分享

	private boolean update;// 文档：更新，附件重新上传
	
	private boolean delete; // 文档、附件：删除

	private boolean deposit; // 附件：带水印下载

	private boolean download; // 附件：下载

}
