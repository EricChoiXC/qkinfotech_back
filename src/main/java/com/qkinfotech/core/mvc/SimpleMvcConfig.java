package com.qkinfotech.core.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.weaving.LoadTimeWeaverAware;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.util.ProxyUtils;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SimpleMvcConfig implements LoadTimeWeaverAware, ApplicationListener<ApplicationReadyEvent> {

	private Environment environment;

	private class Scanner extends ClassPathBeanDefinitionScanner {

		public Scanner(BeanDefinitionRegistry registry) {
			super(registry, false);
		}

		public int scan(String... basePackages) {
			doScan(basePackages);
			return 0;
		}

		public Set<BeanDefinitionHolder> doScan(String... basePackage) {
			Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackage);
			return beanDefinitionHolders;
		}

		// 不注册
		protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
			return;
		}
	}

	public SimpleMvcConfig(Environment environment, ConfigurableListableBeanFactory beanFactory, EntityManager entityManager) throws ClassNotFoundException {

		ValueHolder em = new ValueHolder(entityManager);

		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

		Scanner scanner = new Scanner(registry);
		scanner.addIncludeFilter(new AnnotationTypeFilter(SimpleModel.class));
		String[] basePackages = getScanPackages(beanFactory);
		Set<BeanDefinitionHolder> beanDefinitionHolders = scanner.doScan(basePackages);

		// 注册 Repository
		for (BeanDefinitionHolder item : beanDefinitionHolders) {
			String modelClassName = item.getBeanDefinition().getBeanClassName();
			Class<?> modelClass = Class.forName(modelClassName);
			String beanNamePrefix = StringUtils.uncapitalize(modelClass.getSimpleName());
			ValueHolder clazz = new ValueHolder(modelClass, null);
			registerBean(registry, beanNamePrefix + "Repository", SimpleRepository.class, clazz, em);
		}
		// 注册 Service
		for (BeanDefinitionHolder item : beanDefinitionHolders) {
			String modelClassName = item.getBeanDefinition().getBeanClassName();
			Class<?> modelClass = Class.forName(modelClassName);
			String beanNamePrefix = StringUtils.uncapitalize(modelClass.getSimpleName());
			ValueHolder repository = new ValueHolder(beanFactory.getBean(beanNamePrefix + "Repository"));
			// ValueHolder repository = new ValueHolder(null,
			// SimpleRepository.class.getName()+"<" + modelClass.getName() + ">");
			registerBean(registry, beanNamePrefix + "Service", SimpleService.class, repository);
		}
		// 注册 Controller
		for (BeanDefinitionHolder item : beanDefinitionHolders) {
			String modelClassName = item.getBeanDefinition().getBeanClassName();
			Class<?> modelClass = Class.forName(modelClassName);
			if(StringUtils.hasText(modelClass.getAnnotation(SimpleModel.class).url())) {
				String beanNamePrefix = StringUtils.uncapitalize(modelClass.getSimpleName());
				ValueHolder service = new ValueHolder(beanFactory.getBean(beanNamePrefix + "Service"));
				registerBean(registry, beanNamePrefix + "Controller", SimpleController.class, service);
			}
		}
		logger.debug(">>>>>>>>>>>>>>Auto MVC <<<<<<<");
	}

	protected String[] getScanPackages(ConfigurableListableBeanFactory beanFactory) {
		Set<String> basePackages = new LinkedHashSet<>();

		String[] names = beanFactory.getBeanNamesForAnnotation(SpringBootApplication.class);
		for (String name : names) {
			SpringBootApplication springBootApplication = beanFactory.findAnnotationOnBean(name, SpringBootApplication.class);
			String[] scanBasePackages = springBootApplication.scanBasePackages();
			for (String pkg : scanBasePackages) {
				String[] tokenized = StringUtils.tokenizeToStringArray(environment.resolvePlaceholders(pkg), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
				Collections.addAll(basePackages, tokenized);
			}
			Collections.addAll(basePackages, ClassUtils.getPackageName(beanFactory.getBeanDefinition(name).getBeanClassName()));
		}

		names = beanFactory.getBeanNamesForAnnotation(ComponentScan.class);
		for (String name : names) {
			ComponentScan componentScan = beanFactory.findAnnotationOnBean(name, ComponentScan.class);

			String[] scanBasePackages = componentScan.value();
			for (String pkg : scanBasePackages) {
				String[] tokenized = StringUtils.tokenizeToStringArray(environment.resolvePlaceholders(pkg), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
				Collections.addAll(basePackages, tokenized);
			}
		}
		return StringUtils.toStringArray(basePackages);
	}

	private void registerBean(BeanDefinitionRegistry registry, String beanName, Class<?> beanClass, ValueHolder... args) {
		if (!registry.containsBeanDefinition(beanName)) {
			GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
			beanDefinition.setBeanClass(beanClass);

			ConstructorArgumentValues cav = new ConstructorArgumentValues();
			for (ValueHolder arg : args) {
				cav.addGenericArgumentValue(arg);
			}
			beanDefinition.setConstructorArgumentValues(cav);
			registry.registerBeanDefinition(beanName, beanDefinition);
		}
	}

	private Map<String, String> methodMappingInfo;

	protected void registerUrlHandler(RequestMappingHandlerMapping requestMappingHandlerMapping, String url, Object handlerBean, List<String> methods) {
		if (!StringUtils.hasText(url)) {
			return;
		}
		if (!url.endsWith("/")) {
			url += "/";
		}
		Map<RequestMappingInfo, HandlerMethod> mappings = requestMappingHandlerMapping.getHandlerMethods();
		for (Map.Entry<String, String> entry : methodMappingInfo.entrySet()) {
			String urlPath = entry.getKey();
			String methodName = entry.getValue();

			if (methods == null || methods.size() == 0 || methods.contains(urlPath)) {
				String fullPath = (url + urlPath).replace("//", "/");
				RequestMappingInfo mapping = RequestMappingInfo.paths(fullPath).options(requestMappingHandlerMapping.getBuilderConfiguration()).build();
				if (!mappings.containsKey(mapping)) {
					Method method = Stream.of(handlerBean.getClass().getMethods()).filter(o -> methodName.equals(o.getName())).findFirst().orElse(null);
					requestMappingHandlerMapping.registerMapping(mapping, handlerBean, method);
					logger.trace(mapping.toString());
				}
			}
		}
	}

	private <A extends Annotation> A findAnnotation(AnnotatedElement type, Class<A> annotationType) {
		MergedAnnotation<A> annotation = MergedAnnotations.from(type, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
		if (annotation.isPresent()) {
			return annotation.synthesize();
		}
		return null;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

		methodMappingInfo = new HashMap<String, String>();
		Method[] methods = SimpleController.class.getMethods();
		for (Method method : methods) {
			RequestMapping rm = findAnnotation(method, RequestMapping.class);
			if (rm != null) {
				for (String url : rm.value()) {
					methodMappingInfo.put(url, method.getName());
				}
			}
		}

		ConfigurableApplicationContext applicationContext = event.getApplicationContext();

		RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);

		String[] beanNames = applicationContext.getBeanNamesForType(SimpleController.class);

		for (String beanName : beanNames) {
			SimpleController bean = (SimpleController) applicationContext.getBean(beanName);
			if (ProxyUtils.getUserClass(bean).equals(SimpleController.class)) {
				Class modelClass = bean.getModelClass();
				SimpleModel rcm = (SimpleModel) modelClass.getAnnotation(SimpleModel.class);

				registerUrlHandler(requestMappingHandlerMapping, rcm.url(), bean, List.of(rcm.methods()));
			}
		}

	}

	@Override
	public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {
		
	}
}
