package com.qkinfotech.core.i18n;

import java.util.Locale;
import java.util.TimeZone;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.AbstractLocaleContextResolver;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("localeResolver")
public class CustomLocalResolver extends AbstractLocaleContextResolver {

	private static final String LOCALE_ATTRIBUTE_NAME = "_LOCALE_";

	private static final String TIMEZONE_ATTRIBUTE_NAME = "_TIMEZONE_";

	private static final String LOCALE_COOKIE_NAME = "locale";

	private static final String LOCALE_PARAMETER_NAME = "lang";

	private void parseRequestLocale(HttpServletRequest request) {
		if (request.getAttribute(LOCALE_ATTRIBUTE_NAME) == null) {
			Locale locale = null;
			TimeZone timeZone = null;

			String value = request.getParameter(LOCALE_PARAMETER_NAME);
			if (!StringUtils.hasText(value)) {
				Cookie cookie = WebUtils.getCookie(request, LOCALE_COOKIE_NAME);
				if (cookie == null) {
					return;
				}
				value = cookie.getValue();
			}
			String localePart = value;
			String timeZonePart = null;
			int separatorIndex = localePart.indexOf('/');
			if (separatorIndex == -1) {
				separatorIndex = localePart.indexOf(' ');
			}
			if (separatorIndex >= 0) {
				localePart = value.substring(0, separatorIndex);
				timeZonePart = value.substring(separatorIndex + 1);
			}
			try {
				locale = (!"-".equals(localePart) ? StringUtils.parseLocale(localePart) : null);
				if (timeZonePart != null) {
					timeZone = StringUtils.parseTimeZoneString(timeZonePart);
				}
				request.setAttribute(LOCALE_ATTRIBUTE_NAME, locale);
				request.setAttribute(TIMEZONE_ATTRIBUTE_NAME, timeZone);
			} catch (IllegalArgumentException ex) {
				logger.error(ex.getMessage());
			}
		}
	}

	@Override
	public LocaleContext resolveLocaleContext(final HttpServletRequest request) {
		parseRequestLocale(request);
		return new TimeZoneAwareLocaleContext() {
			@Override
			public Locale getLocale() {
				Locale locale = (Locale) request.getAttribute(LOCALE_ATTRIBUTE_NAME);
				if (locale != null) {
					return locale;
				}
				locale = (Locale) WebUtils.getSessionAttribute(request, LOCALE_ATTRIBUTE_NAME);
				if (locale != null) {
					return locale;
				}
				if (getDefaultLocale() != null) {
					return getDefaultLocale();
				}
				return request.getLocale();
			}

			@Override
			@Nullable
			public TimeZone getTimeZone() {
				TimeZone timeZone = (TimeZone) request.getAttribute(TIMEZONE_ATTRIBUTE_NAME);
				if (timeZone != null) {
					return timeZone;
				}
				timeZone = (TimeZone) WebUtils.getSessionAttribute(request, TIMEZONE_ATTRIBUTE_NAME);
				if (timeZone != null) {
					return timeZone;
				}
				return getDefaultTimeZone();
			}
		};
	}

	@Override
	public void setLocaleContext(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable LocaleContext localeContext) {

		Locale locale = null;
		TimeZone timeZone = null;
		if (localeContext != null) {
			locale = localeContext.getLocale();
			if (localeContext instanceof TimeZoneAwareLocaleContext timeZoneAwareLocaleContext) {
				timeZone = timeZoneAwareLocaleContext.getTimeZone();
			}
		}
		WebUtils.setSessionAttribute(request, LOCALE_ATTRIBUTE_NAME, locale);
		WebUtils.setSessionAttribute(request, TIMEZONE_ATTRIBUTE_NAME, timeZone);
	}

	@Override
	public TimeZone getDefaultTimeZone() {

		return super.getDefaultTimeZone();
	}

	@Override
	protected Locale getDefaultLocale() {

		// TODO 根据人员获取Locale

		return super.getDefaultLocale();
	}

}
