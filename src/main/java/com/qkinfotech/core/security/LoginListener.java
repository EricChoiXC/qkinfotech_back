package com.qkinfotech.core.security;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureCredentialsExpiredEvent;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;
import org.springframework.security.authentication.event.AuthenticationFailureExpiredEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationFailureProviderNotFoundEvent;
import org.springframework.security.authentication.event.AuthenticationFailureProxyUntrustedEvent;
import org.springframework.security.authentication.event.AuthenticationFailureServiceExceptionEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;

public class LoginListener implements ApplicationListener<AbstractAuthenticationEvent> {

	@Override
	public void onApplicationEvent(AbstractAuthenticationEvent event) {
		if (event instanceof InteractiveAuthenticationSuccessEvent) {
			return;
		} else if(event instanceof AuthenticationSuccessEvent) {
			return;
		} else if(event instanceof AuthenticationFailureBadCredentialsEvent) {
			return;
		} else if(event instanceof AuthenticationFailureCredentialsExpiredEvent) {
			return;
		} else if(event instanceof AuthenticationFailureDisabledEvent) {
			return;
		} else if(event instanceof AuthenticationFailureExpiredEvent) {
			return;
		} else if(event instanceof AuthenticationFailureLockedEvent) {
			return;
		} else if(event instanceof AuthenticationFailureProviderNotFoundEvent) {
			return;
		} else if(event instanceof AuthenticationFailureProxyUntrustedEvent) {
			return;
		} else if(event instanceof AuthenticationFailureServiceExceptionEvent) {
			return;
		} else if(event instanceof AbstractAuthenticationFailureEvent) {
			return;
		} else if(event instanceof LogoutSuccessEvent) {
			return;
		} else if(event instanceof AbstractAuthenticationEvent) {
			
		}
	}

}
