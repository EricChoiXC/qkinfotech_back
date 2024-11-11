package com.qkinfotech.core.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import com.qkinfotech.util.DESUtil;

public class CustomAuthenticationProvider extends DaoAuthenticationProvider {
	
	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		// 默认数据库校验，此处可以添加其他的校验方式，比如： LDAP
		String realPassword = DESUtil.decrypt(userDetails.getPassword());
		
		
		super.additionalAuthenticationChecks(userDetails, authentication);
	}

}
