package com.qkinfotech.core.org.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.org.model.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

@Component
public class Bean2JsonUtil {

    public JSONObject toJson(OrgBase org) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fId", org.getfId());
            jsonObject.put("fName", org.getfName());
            jsonObject.put("fType", org.getfType());
            Method getFullNameMethod = org.getClass().getMethod("getfFullName");
            if (getFullNameMethod != null) {
                try {
                    String fFullName = (String) getFullNameMethod.invoke(org);
                    jsonObject.put("fFullName", fFullName);
                } catch (Exception e) {
                    e.printStackTrace();
                    jsonObject.put("fFullName", org.getfName());
                }
            }
            return jsonObject;
        }catch(Exception e) {
            e.printStackTrace();
            if(e instanceof RuntimeException ex) {
                throw ex;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public JSONObject toJson(OrgElement org) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fId", org.getfId());
            jsonObject.put("fName", org.getfName());
            jsonObject.put("fType", org.getfType());
            return jsonObject;
        }catch(Exception e) {
            e.printStackTrace();
            if(e instanceof RuntimeException ex) {
                throw ex;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public JSONObject postToJson(OrgPost post) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fId", post.getfId());
            jsonObject.put("fName", post.getfName());
            jsonObject.put("fFullName", post.getfFullName());
            return jsonObject;
        }catch(Exception e) {
            e.printStackTrace();
            if(e instanceof RuntimeException ex) {
                throw ex;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public JSONObject groupToJson(OrgGroup group) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fId", group.getfId());
            jsonObject.put("fName", group.getfName());
            jsonObject.put("fFullName", group.getfFullName());
            JSONArray ja = new JSONArray();
            Set fMembers = group.getfMembers();
            for (Object member : fMembers) {
                if (member instanceof OrgGroupMember) {
                    ja.add(toJson(((OrgGroupMember) member).getfElement()));
                }
            }
            jsonObject.put("fMembers", ja);
            return jsonObject;
        }catch(Exception e) {
            e.printStackTrace();
            if(e instanceof RuntimeException ex) {
                throw ex;
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}
