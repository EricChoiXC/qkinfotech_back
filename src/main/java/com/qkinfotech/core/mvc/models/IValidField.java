package com.qkinfotech.core.mvc.models;

import com.qkinfotech.core.mvc.IBaseEntity;

import jakarta.persistence.Column;

public interface IValidField extends IBaseEntity {

    String fValid = "f_valid";

    @Column(name="f_valid")
    default Boolean isfValid() {
		return (Boolean)this.getFeatureValue(fValid);
	}

	default void setfValid(Boolean fValid) {
		this.setFeatureValue(IValidField.fValid, fValid);
	}

}
