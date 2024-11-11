package com.qkinfotech.core.tendering.apps.project.manager;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.QueryBuilder;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.SpringUtil;
import com.qkinfotech.util.SqlUtil;
import com.qkinfotech.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class AppsProjectPackageManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final AuthorizationDecision DENY= new AuthorizationDecision(false);
    private static final AuthorizationDecision All0W= new AuthorizationDecision(true);

    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        AuthorizationManager.super.verify(authentication, object);
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        HttpServletRequest request = object.getRequest();
        String loginUserName = request.getRemoteUser();

        SysUser userInfo = getUserInfo(loginUserName);
        if(null == userInfo){
            return DENY;
        }
        //如果是admin账户，则给该账户直接放行
        if("admin".equals(userInfo.getfLoginName())){
            return All0W;
        }

        String projectId = request.getParameter("fMainId");
        if(StringUtil.isNotNull(projectId) && isProjectLeader(projectId,userInfo.getfId())){
            return All0W;
        }

        String packageId = request.getParameter("fId");
        if (StringUtil.isNotNull(packageId) && isPackageProjectLeader(packageId, userInfo.getfId())) {
            return All0W;
        }


        return DENY;
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户id
     */
    private SysUser getUserInfo(String loginUserName){
        JSONObject getUserJson = new JSONObject();
        SqlUtil.setParameter(getUserJson,"fLoginName",loginUserName,"query","eq");
        SqlUtil.setParameter(getUserJson,"fDisabled","0","query","eq");

        QueryBuilder<SysUser> qb = QueryBuilder.parse(SysUser.class, getUserJson);
        SimpleService<SysUser> sysUserService = (SimpleService<SysUser>) SpringUtil.getContext().getBean("sysUserService");
        SysUser user = sysUserService.findOne(qb.specification());
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

    /**
     * 判断登录用户是否是该项目的项目经理
     * @param packageId         包件id
     * @param userId            用户id
     * @return                  是否是项目经理
     */
    private Boolean isPackageProjectLeader(String packageId,String userId) {
        SimpleService<AppsProjectPackage> projectPackageService = (SimpleService<AppsProjectPackage>) SpringUtil.getContext().getBean("appsProjectPackageService");
        //获取项目信息
        AppsProjectPackage projectPackage = projectPackageService.getById(packageId);
        if(null == projectPackage){
            return false;
        }
        //判断当前登录用户是否是项目经理
        if(null != projectPackage.getfMainId().getfDeptManager() && userId.equals(projectPackage.getfMainId().getfDeptManager().getfId())){
            return true;
        }
        return false;
    }
}
