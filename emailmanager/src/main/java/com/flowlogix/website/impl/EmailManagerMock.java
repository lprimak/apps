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
package com.flowlogix.website.impl;

import com.flowlogix.website.EmailManagerLocal;
import com.flowlogix.website.security.UserAuth;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cdi.annotations.Principal;

@Slf4j
@Stateless
public class EmailManagerMock implements EmailManagerLocal {
    @Inject
    @Principal
    Supplier<UserAuth> auth;

    @Override
    public int eraseFolder(String folderName) {
        // just a fake test
        logUserName();
        return 0;
    }

    @Override
    public int sendDrafts(String draftFolderName, String sentFolderName) {
        logUserName();
        return 0;
    }

    @Override
    public boolean isMock() {
        return true;
    }

    @Override
    public void pingImap() throws MessagingException {
    }

    @Override
    public void pingSmtp() throws MessagingException {
    }

    private void logUserName() {
        if (auth.get() != null) {
            log.info("User: {}", auth.get().getUserName().orElse("<invalid>"));
        } else {
            log.info("User: <none>");
        }
    }
}
