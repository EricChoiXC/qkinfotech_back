package com.qkinfotech.core.auth.token.webservice;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

/**
 * 生成TOKEN接口类
 * @author 蔡咏钦
 */
@WebService
public interface ITokenWebservice {

    /**
     * 生成TOKEN
     * @param loginName 登录名
     * @return TOKEN
     * @throws Exception
     */
    @WebMethod
    public String generateToken(String loginName) throws Exception;
}
