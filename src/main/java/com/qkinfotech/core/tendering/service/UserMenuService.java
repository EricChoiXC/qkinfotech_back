package com.qkinfotech.core.tendering.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleRepository;
import com.qkinfotech.core.tendering.model.sys.menu.SysMenu;
import com.qkinfotech.core.user.SysRoleService;
import com.qkinfotech.core.user.SysUserService;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户菜单Service
 * @author cailei
 */
@Service
public class UserMenuService {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SimpleRepository<SysMenu> sysMenuRepository;

    @Transactional(readOnly = true)
    public JSONArray getUserMenuTree(String fUserId) throws Exception {
        Specification<SysMenu> spec = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("fMenuType"), "top-menu");
            return query.where(predicate).getRestriction();
        };
        SysMenu sysMenu = sysMenuRepository.findOne(spec).orElse(null);
        if (sysMenu != null) {
            return processMenuJson(fUserId, sysMenu.getfMenuJson());
        }
        return new JSONArray();
    }

    private JSONArray processMenuJson(String fUserId, JSONArray menuJsons) throws Exception {
        //检查用户是否管理员角色，管理员角色不受权限管控
        boolean isAdminRole = sysRoleService.checkIsAdminRole(fUserId);
        List<JSONObject> processedList = new ArrayList<>();
        for (int i = 0; i < menuJsons.size(); i++) {
            JSONObject jsonObject = menuJsons.getJSONObject(i);
            JSONObject processedObject = processJsonObject(fUserId, jsonObject, isAdminRole, false);
            if (processedObject != null) {
                processedList.add(processedObject);
            }
        }
        return new JSONArray(processedList);
    }

    /**
     * 权限验证
     *
     * @param userId     用户Id
     * @param jsonObject 菜单JSON
     * @param parentAuth 上级是否有权限,默认false
     * @return
     * @throws Exception
     */
    private JSONObject processJsonObject(String userId, JSONObject jsonObject, boolean isAdminRole, boolean parentAuth) throws Exception {
        JSONObject newJson = copyJsonObject(jsonObject);
        List<MenuAuthDTO> parentMenuAuth = newJson.getList("auth", MenuAuthDTO.class);
        List<String> parentAuths = parentMenuAuth.stream().map(t -> t.fId).toList();
        // check用户菜单权限：
        // 1、管理员或者auth为空但是上级有权限，则默认为有权限；
        // 2、auth不为空，则校验用户是否在auth权限中；
        boolean checkAuth = false;
        if (isAdminRole || (parentAuths.isEmpty() && parentAuth)) {
            checkAuth = true;
        } else if (!parentAuths.isEmpty() && sysUserService.checkInOrg(userId, parentAuths)) {
            checkAuth = true;
        }
        //验证当前用户是否在授权的信息中
        if (checkAuth) {
            JSONArray childrenArray = newJson.getJSONArray("children");
            if (newJson.containsKey("aside")) {
                String aside = newJson.getString("aside");
                SysMenu menu = sysMenuRepository.getById(aside);
                if (menu != null) {
                    childrenArray = menu.getfMenuJson();
                }
            }
            if (childrenArray != null && !childrenArray.isEmpty()) {
                JSONArray children = new JSONArray();
                JSONArray menu = new JSONArray();
                for (int i = 0; i < childrenArray.size(); i++) {
                    JSONObject childObj = childrenArray.getJSONObject(i);
                    // 递归处理子对象（如果子对象也有children）
                    JSONObject processedChild = processJsonObject(userId, childObj, isAdminRole, true);
                    if (processedChild != null) {
                        String menuType = childObj.getString("action");
                        if ("page".equals(menuType)) {
                            menu.add(processedChild);
                        } else {
                            children.add(processedChild);
                        }
                    }

                    //                    List<MenuAuthDTO> menuAuth = childObj.getList("auth", MenuAuthDTO.class);
//                    List<String> auths = menuAuth.stream().map(t -> t.fId).toList();
//                    //验证当前用户是否在授权的信息中
//                    if (isAdminRole || parentAuth || sysUserService.checkInOrg(userId, auths)) {
//                        String menuType = childObj.getString("menuType");
//                        if ("page".equals(menuType)) {
//                            menu.add(childObj);
//                        } else {
//                            // 递归处理子对象（如果子对象也有children）
//
//                            children.add(childObj);
//                        }
//
//                    }
                }
                // 移除原始的children字段，并添加新的字段
                newJson.remove("children");
                newJson.remove("menu");
                if (!children.isEmpty()) {
                    newJson.put("children", children);
                }
                if (!menu.isEmpty()) {
                    newJson.put("menu", menu);
                }
            }
            newJson.remove("menuType");
            newJson.remove("auth");
            return newJson;
        }
        return null;
    }

    /**
     * copy JsonObject,防止更新JsonObject时，会更新原始数据
     *
     * @param jsonObject json数据
     */
    private JSONObject copyJsonObject(JSONObject jsonObject) {
        String jsonString = jsonObject.toJSONString();
        return JSONObject.parseObject(jsonString);
    }

    @Data
    public static class MenuAuthDTO {
        private String fId;
        private String fName;
    }
}
