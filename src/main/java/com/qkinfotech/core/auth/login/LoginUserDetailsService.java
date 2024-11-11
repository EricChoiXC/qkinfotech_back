package com.qkinfotech.core.auth.login;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qkinfotech.core.mvc.SimpleRepository;
import com.qkinfotech.core.user.model.SysUser;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Component
@Transactional
public class LoginUserDetailsService implements UserDetailsService, ApplicationRunner {
	
	private SimpleRepository<SysUser> sysUserRepository = null;
	
	private ApplicationContext applicationContext;
	
	public LoginUserDetailsService(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	private UserDetails getLoginUser(String username) {
		SysUser user = findByLoginName(username);
		if (user == null) {
			return null;
		}
		return	new LoginUser(user);
	}
	
	private SysUser findByLoginName(String loginName) {
		
		Specification<SysUser> spec = new Specification<SysUser>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<SysUser> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				
				Predicate predicate = cb.equal(root.get("fLoginName"), loginName);
				Predicate predicate2 = cb.equal(root.get("fDisabled"), false);

				return cb.and(predicate, predicate2);
			}
		};
		
		return sysUserRepository.findOne(spec).orElse(null);
		
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		UserDetails userDetails = getLoginUser(username);
		if (userDetails == null) {
			throw new UsernameNotFoundException("User not found");
		}
		return userDetails;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		sysUserRepository = (SimpleRepository<SysUser>)applicationContext.getBean("sysUserRepository");	}

}
