package com.qkinfotech.core.mvc.models;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class VersionField {

	@Column(name="f_major_version") 
    protected int fMajorVersion;

	@Column(name="f_minor_version") 
    protected int fMinorVersion;

	@Column(name="f_current") 
    protected boolean fCurrent;

	public int getfMajorVersion() {
		return fMajorVersion;
	}

	public void setfMajorVersion(int fMajorVersion) {
		this.fMajorVersion = fMajorVersion;
	}

	public int getfMinorVersion() {
		return fMinorVersion;
	}

	public void setfMinorVersion(int fMinorVersion) {
		this.fMinorVersion = fMinorVersion;
	}

	public boolean isfCurrent() {
		return fCurrent;
	}

	public void setfCurrent(boolean fCurrent) {
		this.fCurrent = fCurrent;
	}

}
