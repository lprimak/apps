/*
 * Copyright (C) 2011-2024 Flow Logix, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.jvnet.libpam.PAM;
import org.jvnet.libpam.PAMException;
import org.jvnet.libpam.UnixUser;

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

    @Override
    protected void onInit() {
        super.onInit();
        try {
            pamOperation(pam -> null);
        } catch (Throwable thr) {
            constants.setUnixRealmAvailable(false);
            log.warn("PAM realm unavailable", thr);
        }
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (!constants.isUnixRealmAvailable()) {
            return null;
        }

        final UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        final String password = String.valueOf(upToken.getPassword());
        UnixUser unixUser;
        try {
            unixUser = pamOperation(pam -> pam.authenticate(upToken.getUsername(), password));
        } catch (PAMException ex) {
            throw new AuthenticationException(ex);
        }
        return new SimpleAuthenticationInfo(new UserAuth(unixUser.getUserName(), password), upToken.getPassword(), getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        if (!constants.isUnixRealmAvailable()) {
            return null;
        }

        Collection<UserAuth> principalsList = principals.byType(UserAuth.class);

        if (principalsList.isEmpty()) {
            throw new AuthorizationException("Empty principals list!");
        }

        var roles = new HashSet<String>();
        try {
            for (UserAuth userPrincipal : principalsList) {
                UnixUser unixUser = pamOperation(pam -> pam.authenticate(userPrincipal.getUserName().get(),
                        userPrincipal.getPassword().get()));
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

    @FunctionalInterface
    interface PAMFunction<TT> {
        TT apply(PAM pam) throws PAMException;
    }

    private <TT> TT pamOperation(PAMFunction<TT> operation) throws PAMException {
        try {
            // PAM instances are not reusable.
            @Cleanup("dispose")
            PAM pam = new PAM(constants.getPamAuthServiceName());
            return operation.apply(pam);
        } finally {
            JNAOperation.begin();
        }
    }
}
