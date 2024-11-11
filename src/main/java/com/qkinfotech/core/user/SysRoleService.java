package com.qkinfotech.core.user;

import com.qkinfotech.core.mvc.SimpleRepository;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.user.model.SysRole;
import com.qkinfotech.core.org.model.OrgElement;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色管理
 * @author cailei
 */
@Service
@Transactional
public class SysRoleService extends SimpleService<SysRole> {
    @Autowired
    public SysUserService sysUserService;
    public SysRoleService(SimpleRepository<SysRole> sysRoleRepository) {
        super(sysRoleRepository);
    }

    /**
     * 更新角色信息
     *
     * @param sysRole 角色信息
     */
    @Override
    public void save(SysRole sysRole) {
        //当前角色，是管理员角色为true时，需要把其他所有记录更新为false
        if (sysRole.getfAdminRole()) {
            List<SysRole> sysRoleList = this.repository.findAll();
            sysRoleList.forEach(item -> {
                if (!item.getfId().equals(sysRole.getfId())) {
                    item.setfAdminRole(false);
                    this.repository.save(item);
                }
            });
        }
        this.repository.save(sysRole);
    }

    /**
     * 检查用户是否管理员角色
     *
     */
    public boolean checkIsAdminRole(String userId) throws Exception {
        Specification<SysRole> spec = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("fAdminRole"), true);
            return query.where(predicate).getRestriction();
        };
        SysRole sysRole = this.findOne(spec);
        if (sysRole != null) {
            List<String> orgIds = sysRole.getfElements().stream().map(OrgElement::getfId).toList();
            return sysUserService.checkInOrg(userId, orgIds);
        } else {
            return false;
        }
    }
}
