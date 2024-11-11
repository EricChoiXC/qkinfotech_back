package com.qkinfotech.core.auth.token.webservice;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.QueryBuilder;
import com.qkinfotech.core.user.model.SysUser;
import com.qkinfotech.util.DESUtil;
import com.qkinfotech.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 生成pmToken接口
 * @author 蔡咏钦
 * @createTime 2024-07-03
 */
@Component
public class TokenWebserviceImp implements ITokenWebservice{

    @Autowired
    private SimpleService<SysUser> sysUserService;

    @Override
    public String generateToken(String loginName) throws Exception {
        JSONObject result = new JSONObject();
        try {
            if (StringUtil.isNull(loginName)) {
                result.put("result", false);
                result.put("message", "未提供用户名");
            } else {
                JSONObject qbjson = new JSONObject();
                JSONObject query = new JSONObject();
                JSONArray and = new JSONArray();

                JSONObject param = new JSONObject();
                param.put("fLoginName", loginName);
                query.put("eq", param);
                and.add(query);

                JSONObject param2 = new JSONObject();
                JSONObject query2 = new JSONObject();
                param2.put("fDisabled", false);
                query2.put("eq", param2);
                and.add(query2);

                qbjson.put("query", and);
                QueryBuilder<SysUser> qb = QueryBuilder.parse(SysUser.class, qbjson);
                SysUser user = sysUserService.findOne(qb.specification());
                if (user == null) {
                    result.put("result", false);
                    result.put("message", "该用户不存在");
                } else {
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                    String undesStr = df.format(new Date()) + loginName;
                    String pmToken = DESUtil.encrypt(undesStr);
                    result.put("result", true);
                    result.put("pmToken", pmToken);
                }
            }
        } catch (Exception e) {
            result.put("result", false);
            result.put("message", e.getMessage());
        }
        return result.toString();
    }
}
