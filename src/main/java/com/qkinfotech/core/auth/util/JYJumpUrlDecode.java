package com.qkinfotech.core.auth.util;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Getter
public final class JYJumpUrlDecode {

	String loginKey;
	Long time;
	String url;

	/**
	 * 解密一键登录字符串, 获取登录信息
	 */
	public JYJumpUrlDecode(String queryString) throws Exception {

		queryString = URLDecoder.decode(queryString, StandardCharsets.UTF_8);
		String jsonString = ApiRsaUtil.decrypt(queryString);
		JSONObject jsonObject = JSONObject.parseObject(jsonString);

		this.loginKey = jsonObject.getString("s");
		if (this.loginKey == null || this.loginKey.length() == 0)
			throw new Exception("登录ID错误！");

		this.url = jsonObject.getString("u");
		if (this.url == null || this.url.length() == 0)
			throw new Exception("跳转信息错误！");

		this.time = jsonObject.getLong("t");
		if (this.time == null) {
			// 忽略
		} else {
			long l = System.currentTimeMillis() - this.time;
			if (l < 0 || l > 5 * 60 * 1000) {
				throw new Exception("链接已过期");
			}
		}

	}

}
