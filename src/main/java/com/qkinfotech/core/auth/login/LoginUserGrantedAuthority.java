package com.qkinfotech.core.auth.login;

import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;

import com.qkinfotech.core.user.model.SysAuthority;
import com.qkinfotech.core.user.model.SysRole;

public class LoginUserGrantedAuthority implements GrantedAuthority {
	
	private static final long serialVersionUID = 1L;
	String authority;

	public LoginUserGrantedAuthority(SysAuthority authority) {
		this.authority = authority.getfName();
	}
	
	@Override
	public String getAuthority() {
		return authority;
	}

	@Override
	public String toString() {
		return authority;
	}

	@Override
	public int hashCode() {
		return Objects.hash(authority);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LoginUserGrantedAuthority other = (LoginUserGrantedAuthority) obj;
		return Objects.equals(authority, other.authority);
	}

}
