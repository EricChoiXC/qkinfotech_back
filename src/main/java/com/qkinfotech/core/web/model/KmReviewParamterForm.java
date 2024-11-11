package com.qkinfotech.core.web.model;

import java.util.ArrayList;
import java.util.List;

public class KmReviewParamterForm {

	// 文档标题
	private String docSubject;

	// 文档模板id
	private String fdTemplateId;

	// 文档内容文本
	private String docContent;

	// 表单数据参数
	private String formValues;

	// 文档状态
	private String docStatus;

	// 流程发起人
	private String docCreator;

	// 文档关键字
	private String fdKeyword;

	// 辅类别ID
	private String docProperty;

	// 流程参数
	private String flowParam;

	// 流程发起人的身份
	private String identity;
	/**
	 * 场所ID
	 */
	private String authAreaId;

	public String getDocSubject() {
		return docSubject;
	}

	public void setDocSubject(String docSubject) {
		this.docSubject = docSubject;
	}

	public String getFdTemplateId() {
		return fdTemplateId;
	}

	public void setFdTemplateId(String fdTemplateId) {
		this.fdTemplateId = fdTemplateId;
	}

	public String getDocContent() {
		return docContent;
	}

	public void setDocContent(String docContent) {
		this.docContent = docContent;
	}

	public String getFormValues() {
		return formValues;
	}

	public void setFormValues(String formValues) {
		this.formValues = formValues;
	}

	public String getDocStatus() {
		return docStatus;
	}

	public void setDocStatus(String docStatus) {
		this.docStatus = docStatus;
	}

	public String getDocCreator() {
		return docCreator;
	}

	public void setDocCreator(String docCreator) {
		this.docCreator = docCreator;
	}

	public String getFdKeyword() {
		return fdKeyword;
	}

	public void setFdKeyword(String fdKeyword) {
		this.fdKeyword = fdKeyword;
	}

	public String getDocProperty() {
		return docProperty;
	}

	public void setDocProperty(String docProperty) {
		this.docProperty = docProperty;
	}

	public String getFlowParam() {
		return flowParam;
	}

	public void setFlowParam(String flowParam) {
		this.flowParam = flowParam;
	}

	private String attachmentValues;

	
	public String getAttachmentValues() {
		return attachmentValues;
	}
	
	public void setAttachmentValues(String attachmentValues) {
		this.attachmentValues = attachmentValues;
	}
	
	private String fdSource;

	public String getFdSource() {
		return fdSource;
	}
	
	public void setFdSource(String fdSource) {
		this.fdSource = fdSource;
	}
	
	// 流程文档Id(非起草必填)
	private String fdId;

	public String getFdId() {
		return fdId;
	}

	public void setFdId(String fdId) {
		this.fdId = fdId;
	}

	public String getAuthAreaId() {
		return authAreaId;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}
}
