package com.qkinfotech.util;

import com.qkinfotech.core.user.SysUserService;
import com.qkinfotech.core.user.model.SysUser;
import jakarta.annotation.Resource;

/**
 * Token处理工具类
 */
public class TokenUtil {

    @Resource
    private SysUserService sysUserService;

    public SysUser getUser(String token){
        if(StringUtil.isNull(token)){
            return null;
        }
        //目前的Token生成规则是时间+用户名，则直接截取登录用户名
        String loginName = token.substring(14);
        if(StringUtil.isNull(loginName)){
            return null;
        }
        //根据登录用户获取用户
        return sysUserService.findByLoginName(loginName);
    }

}
