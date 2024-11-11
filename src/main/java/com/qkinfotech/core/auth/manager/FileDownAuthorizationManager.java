package com.qkinfotech.core.auth.manager;

import com.qkinfotech.core.auth.login.LoginUser;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.SpringUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class FileDownAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final AuthorizationDecision DENY= new AuthorizationDecision(false);
    private static final AuthorizationDecision All0W= new AuthorizationDecision(true);

    /**
     * 根据文件id是否拥有下载权限
     */
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        HttpServletRequest request = object.getRequest();
        //文件id
        String fileId = request.getParameter("fId");
        //todo 根据文件id检测是否拥有下载权限

        //获取登录信息
        SysUser userInfo = getUserInfo();
        if(null == userInfo){
            return DENY;
        }



        if(authentication.get().isAuthenticated()){
            return All0W;
        }
        return DENY;
    }


    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户id
     */
    private SysUser getUserInfo(){
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        LoginUser userInfo = (LoginUser)authentication.getPrincipal();
        SimpleService<SysUser> sysUserService = (SimpleService<SysUser>) SpringUtil.getContext().getBean("sysUserService");
        SysUser user = sysUserService.getById(userInfo.getFLoginUserId());
        if (user == null) {
            return null;
        } else {
            return user;
        }
    }


}
