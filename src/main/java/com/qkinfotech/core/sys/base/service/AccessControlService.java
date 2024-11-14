package com.qkinfotech.core.sys.base.service;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.BaseEntity;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.org.model.OrgElement;
import com.qkinfotech.core.sys.base.model.AccessControl;
import com.qkinfotech.util.SpringUtil;
import com.qkinfotech.util.StringUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class AccessControlService implements PluginService{

    /**
     * 保存/更新文档时更新权限
     * @param entity
     * @param value JSONObject或JSONArray:
     * {
     *     beanName : "主Model的beanName",
     *     aclBeanName : "权限Model的beanName"（优先使用aclBeanName，无则在beanName基础上增加'Acl'读取bean）,
     *     parameter : {
     *       action : "add/delete/update",
     *       fKeys : ["权限key，可传文本，用';'分割"],
     *       fAuths : ["权限，可传文本，用';'分割"],
     *       fOrgs : ["用户fId，可传文本，用';'分割"]
     *     }
     * }
     */
    @Override
    public void save(BaseEntity entity, Object value) {
        if (entity != null) {
            if (value instanceof JSONObject json) {
                SimpleService aclService = getAclSimpleService(json.getString("beanName"), json.getString("aclBeanName"));
                if (aclService != null) {
                    Object parameter = json.get("parameter");
                    if (parameter instanceof JSONObject paraJson) {
                        if ("add".equals(paraJson.getString("action"))){
                            add(entity, paraJson, aclService);
                        } else if ("delete".equals(paraJson.getString("action"))){
                            delete(entity, paraJson, aclService);
                        } else if ("update".equals(paraJson.getString("action"))){
                            update(entity, paraJson, aclService);
                        }
                    } else if (parameter instanceof JSONArray) {
                        save(entity, (JSONArray) parameter, aclService);
                    }
                }
            }
        }
    }

    /**
     * 删除文档时删除所有相关权限
     * @param entity
     * @param parameter
     */
    @Override
    public void delete(BaseEntity entity, Object parameter) {

    }

    /**
     * 删除文档时删除所有相关权限，json里提供原Model的beanName存在beanName中；也可直接提供权限表beanName存在aclBeanName中；优先使用aclBeanName
     * @param ids
     * @param json
     * {
     *     beanName : "主Model的beanName",
     *     aclBeanName : "权限Model的beanName"（优先使用aclBeanName，无则在beanName基础上增加'Acl'读取bean）
     * }
     */
    @Override
    public void deleteAll(Set<String> ids, JSONObject json) {
        String beanName = json.getString("beanName");
        String aclBeanName = json.getString("aclBeanName");
        SimpleService aclService = getAclSimpleService(beanName, aclBeanName);
        if (aclService != null) {
            if (ids != null && ids.size() > 0) {
                Specification spec = new Specification() {
                    @Override
                    public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                        CriteriaBuilder.In in = cb.in(root.get("fDocId"));
                        if (!ids.isEmpty()) {
                            in.value(ids);
                        }
                        return in;
                    }
                };
                aclService.delete(spec);
                aclService.flush();
            }
        }

    }

    public void save(BaseEntity entity, JSONArray parameter, SimpleService aclService) {
        for (Object o : parameter) {
            if (o instanceof JSONObject json) {
                if ("add".equals(json.getString("action"))){
                    add(entity, json, aclService);
                } else if ("delete".equals(json.getString("action"))){
                    delete(entity, json, aclService);
                } else if ("update".equals(json.getString("action"))){
                    update(entity, json, aclService);
                }
            }
        }
    }

    public void add(BaseEntity entity, JSONObject parameter, SimpleService aclService) {
        SimpleService<OrgElement> orgElementService = (SimpleService) SpringUtil.getContext().getBean("orgElementService");
        List<String> fOrgs = parameter.getList("fOrgs", String.class);

        List<String> fKeys = new ArrayList<>();
        try {
            Object keys = parameter.get("fKeys");
            if (keys instanceof String) {
                fKeys.addAll(List.of(((String) keys).split(";")));
            } else {
                fKeys.addAll(parameter.getList("fKeys", String.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> fAuths = new ArrayList<>();
        try {
            Object auths = parameter.get("fAuths");
            if (auths instanceof String) {
                fAuths.addAll(List.of(((String) auths).split(";")));
            } else {
                fAuths.addAll(parameter.getList("fAuths", String.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String fKey : fKeys) {
            for (String fAuth : fAuths) {
                for (String fOrg : fOrgs) {
                    OrgElement element = orgElementService.getById(fOrg);
                    if (element != null) {
                        add(entity, fKey, fAuth, element, aclService);
                    }
                }
            }
        }
        aclService.flush();

    }

    public void add(BaseEntity entity, String fKey, String fAuth, OrgElement fOrg, SimpleService simpleService) {
        try {
            AccessControl accessControl = (AccessControl) simpleService.getEntityClass().newInstance();
            accessControl.setfDocId(entity.getfId());
            accessControl.setfAuth(fAuth);
            accessControl.setfKey(fKey);
            accessControl.setfOrg(fOrg);
            simpleService.save(accessControl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void delete(BaseEntity entity, JSONObject parameter, SimpleService aclService) {

        List<String> fOrgs = parameter.getList("fOrgs", String.class);

        List<String> fKeys = new ArrayList<>();
        try {
            Object keys = parameter.get("fKeys");
            if (keys instanceof String) {
                fKeys.addAll(List.of(((String) keys).split(";")));
            } else {
                fKeys.addAll(parameter.getList("fKeys", String.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> fAuths = new ArrayList<>();
        try {
            Object auths = parameter.get("fAuths");
            if (auths instanceof String) {
                fAuths.addAll(List.of(((String) auths).split(";")));
            } else {
                fAuths.addAll(parameter.getList("fAuths", String.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        delete(entity, fKeys, fAuths, fOrgs, aclService);
    }

    public void delete(BaseEntity entity, List<String> fKeys, List<String> fAuths, List<String> fOrgs, SimpleService simpleService) {
        Specification specification = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                Predicate eq = cb.equal(root.get("fDocId"), entity.getfId());
                CriteriaBuilder.In in1 = cb.in(root.get("fKey"));
                if (!fKeys.isEmpty()) {
                    in1.value(fKeys);
                }
                CriteriaBuilder.In in2 = cb.in(root.get("fAuth"));
                if (!fAuths.isEmpty()) {
                    in2.value(fAuths);
                }
                if (!fOrgs.isEmpty()) {
                    CriteriaBuilder.In in3 = cb.in(root.get("fOrg").get("fId"));
                    in3.value(fOrgs);
                    return cb.and(eq, in1, in2, in3);
                } else {
                    return cb.and(eq, in1, in2);
                }
            }
        };
        simpleService.delete(specification);
        simpleService.flush();
    }

    public void delete(BaseEntity entity, JSONArray parameter) {
        for (Object o : parameter) {
            if (o instanceof JSONObject json) {
                delete(entity, json);
            }
        }

    }

    /**
     * 更新权限表，基于fKey和fAuth，覆盖替换本文档权限下对应的权限
     * @param entity
     * @param parameter
     */
    public void update(BaseEntity entity, JSONObject parameter, SimpleService aclService) {
        if (entity != null) {
            List<String> fKeys = new ArrayList<>();
            try {
                Object keys = parameter.get("fKeys");
                if (keys instanceof String) {
                    fKeys.addAll(List.of(((String) keys).split(";")));
                } else {
                    fKeys.addAll(parameter.getList("fKeys", String.class));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<String> fAuths = new ArrayList<>();
            try {
                Object auths = parameter.get("fAuths");
                if (auths instanceof String) {
                    fAuths.addAll(List.of(((String) auths).split(";")));
                } else {
                    fAuths.addAll(parameter.getList("fAuths", String.class));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            delete(entity, fKeys, fAuths, new ArrayList<>(), aclService);

            add(entity, parameter, aclService);
        }
    }


    public SimpleService getAclSimpleService(String beanName, String aclBeanName) {
        if (StringUtil.isNotNull(beanName) && StringUtil.isNull(aclBeanName)) {
            aclBeanName = beanName.substring(0, beanName.lastIndexOf("Service")) + "AclService";
        }
        if (StringUtil.isNotNull(aclBeanName)) {
            SimpleService aclService = (SimpleService) SpringUtil.getContext().getBean(aclBeanName);
            return aclService;
        }
        return null;
    }
}
