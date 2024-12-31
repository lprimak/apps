/*
 * Copyright (C) 2011-2025 Flow Logix, Inc. All Rights Reserved.
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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.config.Ini;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.omnifaces.util.Servlets;
import java.io.IOException;
import static org.apache.shiro.web.env.IniWebEnvironment.DEFAULT_WEB_INI_RESOURCE_PATH;

@Slf4j
@Named
@ApplicationScoped
public class DemoRealm extends AuthorizingRealm {
    @Inject
    Constants constants;

    private IniRealm iniRealm;

    private static final class IniRealm extends org.apache.shiro.realm.text.IniRealm {
        IniRealm(Ini ini) {
            super(ini);
            setCredentialsMatcher(new SimpleCredentialsMatcher());
        }

        public AuthenticationInfo getAuthInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
            var authenticationInfo = super.doGetAuthenticationInfo(authenticationToken);
            if (!getCredentialsMatcher().doCredentialsMatch(authenticationToken, authenticationInfo)) {
                throw new IncorrectCredentialsException();
            }
            return authenticationInfo;
        }

        public AuthorizationInfo getAuthorizationInfo(PrincipalCollection principalCollection) {
            return super.doGetAuthorizationInfo(principalCollection);
        }
    }

    @SneakyThrows(IOException.class)
    protected void onInit() {
        var ini = new Ini();
        try (var stream = Servlets.getContext().getResourceAsStream(DEFAULT_WEB_INI_RESOURCE_PATH)) {
            ini.load(stream);
        }
        iniRealm = new IniRealm(ini);
        iniRealm.init();
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        if (constants.isUnixRealmAvailable()) {
            return null;
        }

        if (authenticationToken instanceof UsernamePasswordToken upToken) {
            AuthenticationInfo auth = iniRealm.getAuthInfo(authenticationToken);
            if (auth != null) {
                var principalCollection = new SimplePrincipalCollection();
                principalCollection.add(new UserAuth(upToken.getUsername(),
                        String.valueOf(upToken.getPassword())), getName());
                principalCollection.add(upToken.getUsername(), iniRealm.getName());
                return new SimpleAuthenticationInfo(principalCollection, upToken.getPassword());
            } else {
                throw new IncorrectCredentialsException();
            }
        } else {
            throw new AuthenticationException("Account type is incorrect");
        }
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        if (constants.isUnixRealmAvailable()) {
            return null;
        }

        return iniRealm.getAuthorizationInfo(principalCollection);
    }
}
