package com.qkinfotech;

import java.io.IOException;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.qkinfotech.core.log.model.SysLog;
import com.qkinfotech.core.user.SysUserService;
import com.qkinfotech.core.user.model.SysUser;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

//@Component
//@Transactional
public class TestMain<T> implements ApplicationListener<ApplicationReadyEvent> {
	
	Logger logger = LoggerFactory.getLogger(TestMain.class);
	
	@Autowired
	//SimpleService<SysUser> sysUserService;
	SysUserService sysUserService;
	
	public void Show(SysUser user) {
		System.out.println(user.getfLoginName());
	}
	
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		
		Specification<SysUser> spec = new Specification<SysUser>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<SysUser> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				
				Predicate predicate = cb.equal(root.get("fLoginName"), "admin");

				return query.where(predicate).getRestriction();
			}
		};
		
		sysUserService.scroll(spec, this::Show);
		
		
		//System.out.println(sysStorageService);
		
		
		// logger.info("ready", new Exception("aaaa"));
		
		//List<SysLog> logs = sysLogRepository.findByfLevel("WARN");

		//sysLogRepository.batchDelete(logs);
		//sysLogRepository.batchDeleteByIds(null);
		//logger.info("logs:" + logs.size());
		
		//sysLogRepository.save(new SysLog());
		
		
		//test.test();
		
		ConfigurableApplicationContext context = event.getApplicationContext();
		
		String[] beanNames = context.getBeanDefinitionNames();
		//String[] beanNames = context.getBeanNamesForType(SimpleJpaRepository.class);
//		List<String>beanNames = new ArrayList();
//		beanNames.addAll(List.of(context.getBeanNamesForType(SimpleController.class)));
//		beanNames.addAll(List.of(context.getBeanNamesForType(SimpleService.class)));
//		beanNames.addAll(List.of(context.getBeanNamesForType(SimpleRepository.class)));
//		
//		
		for(String beanName : beanNames) {
			Object bean =  context.getBean(beanName);
			System.out.println(beanName + ":" + bean.getClass().getName());
		}
		
	}

	
	public static void main(String[] args) throws IOException {
		
		PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
		Resource[] m = r.getResources("classpath*:**/*.xml");
		
		
		StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        MetadataSources sources = new MetadataSources(registry);

        // 添加需要解析的POJO类
        sources.addAnnotatedClass(SysLog.class);

        Metadata metadata = sources.buildMetadata();

//        SchemaManagementTool schemaManagementTool = metadata.getDatabase().getSchemaManagementTool();
//        //schemaManagementTool.generateSchemaCreationScript(metadata, null);
//
//        // 输出SQL到文件
//        File output = new File("schema.sql");
//        schemaManagementTool.getSchemaCreator(null).doCreation(metadata, false, Collections.emptyMap(), true, output);



        StandardServiceRegistryBuilder.destroy(registry);
		
	}
}
