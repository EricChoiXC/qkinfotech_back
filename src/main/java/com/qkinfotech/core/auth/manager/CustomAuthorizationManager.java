package com.qkinfotech.core.auth.manager;

import com.qkinfotech.core.auth.login.LoginUser;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.SpringUtil;
import com.qkinfotech.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 自定义url权限过滤器
 */
@Component
public class CustomAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final AuthorizationDecision DENY= new AuthorizationDecision(false);
    private static final AuthorizationDecision All0W= new AuthorizationDecision(true);

    /**
     * 验证当前用户是否是项目经理，如果是则放权，如果不是则返回无权限
     */
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        HttpServletRequest request = object.getRequest();
        //获取登录信息
        SysUser userInfo = getUserInfo();
        if(null == userInfo){
            return DENY;
        }
        //如果是admin账户，则给该账户直接放行
        if("admin".equals(userInfo.getfLoginName())){
            return All0W;
        }
        //获取项目id     该参数必须带到url中，否则获取不到，会提示没有权限
        String projectId = request.getParameter("fId");
        if(StringUtil.isNull(projectId)){
            return DENY;
        }
        if(isProjectLeader(projectId,userInfo.getfId())){
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

    /**
     * 判断登录用户是否是该项目的项目经理
     * @param projectId         项目id
     * @param userId            用户id
     * @return                  是否是项目经理
     */
    private Boolean isProjectLeader(String projectId,String userId) {
        SimpleService<AppsProjectMain> projectMainService = (SimpleService<AppsProjectMain>) SpringUtil.getContext().getBean("appsProjectMainService");
        //获取项目信息
        AppsProjectMain projectMain = projectMainService.getById(projectId);
        if(null == projectMain){
            return false;
        }
        //判断当前登录用户是否是项目经理
        if(null != projectMain.getfDeptManager() && userId.equals(projectMain.getfDeptManager().getfId())){
            return true;
        }
        return false;
    }
}
