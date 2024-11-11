package com.qkinfotech.core.i18n;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.qkinfotech.core.i18n.model.SysMessage;
import com.qkinfotech.core.mvc.SimpleService;

@Component("messageSource")
public class DBMessageSource implements MessageSource, ApplicationRunner, ApplicationContextAware {

	private static Locale defaultLocale = Locale.CHINA;

	private Map<Locale, Map<String, String>> messages = new HashMap<>();

	public DBMessageSource() {

		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try {
			Resource[] resources = resolver.getResources("classpath*:/**/messages*.properties");

			for (Resource resource : resources) {
				System.out.println(resource.getURI().toString());
				String filename = resource.getFilename();
				Locale locale = getLangFromFileName(filename);
				System.out.println(locale + ":" + filename);

				if (locale != null) {
					Properties prop = new Properties();
					prop.load(resource.getInputStream());
					Map<String, String> msg = messages.get(locale);
					if (msg == null) {
						msg = new HashMap<>();
						messages.put(locale, msg);
					}

					for (final String name : prop.stringPropertyNames()) {
						msg.put(name, prop.getProperty(name));
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static Locale getLangFromFileName(String filename) {
		try {
			String[] tokens = StringUtils.tokenizeToStringArray(filename, ".");
			if (tokens.length == 1) {
				return null;
			}
			if (tokens.length == 2) {
				if ("messages.properties".equals(filename)) {
					return defaultLocale;
				}
				if(tokens[0].startsWith("messages") && (tokens[0].charAt(8) == '-' || tokens[0].charAt(8) == '_')) {
					return StringUtils.parseLocaleString(tokens[0].substring(9));
				}
			}
			if (tokens.length == 3) {
				if ("messages".equals(tokens[0]) && "properties".equals(tokens[2])) {
					return StringUtils.parseLocaleString(tokens[1]);
				}
			}
		}catch(Exception e){
		}
		return null;
	}

	@Override
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		Map<String, String> msg = messages.get(locale == null ? defaultLocale : locale);
		if (msg == null) {
			msg = messages.get(defaultLocale);
		}
		String s = msg.get(code);
		if (s != null) {
			s = defaultMessage;
		}
		if (s == null) {
			return null;
		}
		return MessageFormat.format(s, args);
	}

	@Override
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		Map<String, String> msg = messages.get(locale == null ? defaultLocale : locale);
		if (msg == null) {
			msg = messages.get(defaultLocale);
		}
		String s = msg.get(code);
		if (s != null) {
			// throw new NoSuchMessageException(code);
			return code;
		}
		return MessageFormat.format(s, args);
	}

	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		String defaultMessage = resolvable.getDefaultMessage();
		String[] codes = resolvable.getCodes();
		if (defaultMessage == null) {
			if (codes == null || codes.length == 0) {

			}
			defaultMessage = codes[codes.length - 1];
		}
		for (String code : codes) {
			String s = getMessage(code, resolvable.getArguments(), defaultMessage, locale);
			if (s != null) {
				return s;
			}
		}
		return resolvable.getDefaultMessage();
		// throw new NoSuchMessageException("");
	}

	private ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// 从数据库同步覆盖
		SimpleService<SysMessage> sysMessageService = (SimpleService<SysMessage>) context.getBean("sysMessageService");

		List<SysMessage> msgs = sysMessageService.findAll();
		for (SysMessage msg : msgs) {
			Locale locale = StringUtils.parseLocale(msg.getfLocale());
			Map<String, String> bundle = messages.get(locale);
			if (bundle == null) {
				bundle = new HashMap<String, String>();
				messages.put(locale, bundle);
			}
			bundle.put(msg.getfKey(), msg.getfValue());
		}
	}

}
