package com.qkinfotech.core.mvc.models;

import com.qkinfotech.core.mvc.IBaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

public interface ITreeEntity<T> extends IBaseEntity {

	String fParent = "f_parent_id";

	String fFullIdPath = "f_full_id_path";

	String fFullNamePath = "f_full_name_path";
	
	@ManyToOne
	@JoinColumn(name = "f_parent_id")
	default public T getfParent() {
		return (T)this.getFeatureValue(fParent);
	}

	default public void setfParent(T fParent) {
		this.setFeatureValue(ITreeEntity.fParent, fParent);
	}

	@Column(name="f_full_id_path", length=1024)
	default public String getfFullIdPath() {
		return (String)this.getFeatureValue(fFullIdPath);
	}

	default public void setfFullIdPath(String fFullIdPath) {
		this.setFeatureValue(ITreeEntity.fParent, fFullIdPath);
	}

	@Column(name="f_full_name_path", length=1024)
	default  public String getfFullNamePath() {
		return (String)this.getFeatureValue(fFullNamePath);
	}

	default public void setfFullNamePath(String fFullNamePath) {
		this.setFeatureValue(ITreeEntity.fFullNamePath, fFullNamePath);
	}


}
