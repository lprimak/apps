/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowlogix.website.security;

import com.flowlogix.website.ui.Constants;
import java.util.Collection;
import java.util.HashSet;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.authz.permission.WildcardPermissionResolver;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.jvnet.libpam.PAM;
import org.jvnet.libpam.PAMException;
import org.jvnet.libpam.UnixUser;
import org.omnifaces.util.Beans;

/**
 *
 * @author lprimak
 */
public class UnixRealm extends AuthorizingRealm {
    private final String serviceName = Beans.getReference(Constants.class).getPamAuthServiceName();

    public UnixRealm() {
        if (getPermissionResolver() == null) {
            setPermissionResolver(new WildcardPermissionResolver());
        }
    }

    @Override
    @SneakyThrows(PAMException.class)
    protected void onInit() {
        super.onInit();
        getPam();
    }

    protected PAM getPam() throws PAMException {
        // PAM instances are not reusable.
        return new PAM(serviceName);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        final UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        final String password = String.valueOf(upToken.getPassword());
        UnixUser unixUser = null;
        try {
            @Cleanup("dispose")
            PAM pam = getPam();
            unixUser = pam.authenticate(upToken.getUsername(), password);
        } catch (PAMException ex) {
            throw new AuthenticationException(ex);
        }
        return new SimpleAuthenticationInfo(new UserAuth(unixUser.getUserName(), password), upToken.getPassword(), getName());
    }

    @Override
    @SneakyThrows(PAMException.class)
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        var roles = new HashSet<String>();
        var permissions = new HashSet<Permission>();
        permissions.add(new WildcardPermission("mail:*"));
        Collection<UserAuth> principalsList = principals.byType(UserAuth.class);

        if (principalsList.isEmpty()) {
            throw new AuthorizationException("Empty principals list!");
        }

        for (UserAuth userPrincipal : principalsList) {
            @Cleanup("dispose")
            PAM pam = getPam();
            UnixUser unixUser = pam.authenticate(userPrincipal.getUserName(), userPrincipal.getPassword());
            roles.addAll(unixUser.getGroups());
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roles);
        info.setObjectPermissions(permissions);

        return info;
    }
}
