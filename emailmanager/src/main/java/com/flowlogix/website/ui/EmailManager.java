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
package com.flowlogix.website.ui;

import com.flowlogix.website.EmailManagerLocal;
import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.omnifaces.util.JNDIObjectLocator;
import org.omnifaces.util.Lazy;
import org.primefaces.PrimeFaces;

/**
 *
 * @author lprimak
 */
@Named @SessionScoped @Slf4j
public class EmailManager implements Serializable {
    private static final long serialVersionUID = 1L;
    @Inject
    private Constants constants;
    private final JNDIObjectLocator locator = JNDIObjectLocator.builder().build();
    private final Lazy<EmailManagerLocal> emailManager = new Lazy<>(this::createEmailManager);
    private String emailStatus;

    @RequiresPermissions("mail:junk:erase")
    public void eraseJunk() {
        try {
            int erasedCount = emailManager.get().eraseFolder(constants.getJunkFolderName());
            if (erasedCount > 0) {
                displayMessage("Erased Junk Mail (%d)".formatted(erasedCount));
            } else {
                displayMessage("No Junk E-Mail to Erase");
            }
        } catch (MessagingException e) {
            log.debug("failed to erase junk mail", e);
            displayMessage("Failed to erase junk mail: " + e.getMessage());
        }
    }

    @RequiresPermissions("mail:draft:send")
    public void sendDrafts() {
        try {
            int sentCount = emailManager.get().sendDrafts(constants.getDraftFolderName(),
                    constants.getSentFolderName());
            if (sentCount > 0) {
                displayMessage(String.format("Draft E-Mail%s Sent (%d)", (sentCount > 1) ? "s" : "", sentCount));
            } else {
                displayMessage("No Draft E-Mail to Send");
            }
        } catch (MessagingException e) {
            log.debug("failed to send drafts", e);
            displayMessage("Failed to send drafts: " + e.getMessage());
        }
    }

    public boolean isStartPolling() {
        boolean startPolling = emailStatus != null;
        if (startPolling) {
            highlightStatus(false);
        }
        return startPolling;
    }

    public String getEmailStatus() {
        return emailStatus != null ? emailStatus : "<None>";
    }

    public void highlightStatus(boolean resetStatus) {
        if (resetStatus) {
            emailStatus = null;
        }
        PrimeFaces.current().executeScript("$(emailStatus).effect('highlight', {color: '#5AACFD'}, 1000)");
    }

    private void displayMessage(String junkErasedMessage) {
        if (emailManager.get().isMock()) {
            emailStatus = junkErasedMessage + " (Mock)";
        } else {
            emailStatus = junkErasedMessage;
        }
        PrimeFaces.current().executeScript("PF('poll').start()");
    }

    private EmailManagerLocal createEmailManager() {
        EmailManagerLocal eraserImpl = locator.getObject("java:module/EmailManagerImpl");
        try {
            eraserImpl.pingImap();
            return eraserImpl;
        } catch (AuthenticationFailedException e) {
            return eraserImpl;
        } catch (MessagingException e) {
            log.warn("Ping Imap", e);
            return locator.getObject("java:module/EmailManagerMock");
        }
    }
}
