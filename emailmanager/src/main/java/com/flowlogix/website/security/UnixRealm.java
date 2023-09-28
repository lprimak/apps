/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowlogix.website.security;

import com.flowlogix.website.ui.Constants;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
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
@Slf4j
@Named
@ApplicationScoped
public class UnixRealm extends AuthorizingRealm {
    @Inject
    Constants constants;

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
        return new PAM(constants.getPamAuthServiceName());
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
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Collection<UserAuth> principalsList = principals.byType(UserAuth.class);

        if (principalsList.isEmpty()) {
            throw new AuthorizationException("Empty principals list!");
        }

        var roles = new HashSet<String>();
        try {
            for (UserAuth userPrincipal : principalsList) {
                @Cleanup("dispose")
                PAM pam = getPam();
                UnixUser unixUser = pam.authenticate(userPrincipal.getUserName().get(), userPrincipal.getPassword().get());
                roles.addAll(unixUser.getGroups());
            }
        } catch (NoSuchElementException ex) {
            // unable to decrypt credentials from principal
        } catch (PAMException ex) {
            log.debug("PAM authentication failure", ex);
            SecurityUtils.getSubject().logout();
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roles);
        info.setObjectPermissions(Set.of(new WildcardPermission("mail:*")));

        return info;
    }
}
