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
package com.flowlogix.website.impl;

import com.flowlogix.website.security.UserAuth;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import com.flowlogix.website.EmailManagerLocal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import jakarta.inject.Inject;
import jakarta.mail.Address;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MailSessionDefinition;
import jakarta.mail.Transport;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.cdi.annotations.Principal;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Stateless
@MailSessionDefinition(name = "java:app/mail/HopeMail",
        host = "${MPCONFIG=hope-imap-host:}", transportProtocol = "smtp", storeProtocol = "imaps",
        properties = {
            "mail.smtp.auth=true", "mail.smtp.starttls.enable=true", "mail.debug=false"})
@Slf4j
public class EmailManagerImpl implements EmailManagerLocal {
    @Resource(name = "java:app/mail/HopeMail")
    Session mailSession;
    @Inject
    @Principal
    Supplier<UserAuth> user;
    @Inject
    @ConfigProperty(name = "hope-smtp-host", defaultValue = "none")
    private String smtpHost;
    @Inject
    @ConfigProperty(name = "hope-smtp-port", defaultValue = "587")
    private int smtpPort;
    @Inject
    @ConfigProperty(name = "hope-smtp-user", defaultValue = "none")
    private String smtpUser;
    @Inject
    @ConfigProperty(name = "hope-smtp-password", defaultValue = "none")
    private String smtpPassword;

    @Override
    @RequiresPermissions("mail:folder:write")
    public int eraseFolder(String folderName) throws MessagingException {
        @Cleanup
        Folder folder = new Folder(folderName, jakarta.mail.Folder.READ_WRITE);
        int erasedCount = 0;
        for (Message msg : folder.getFolder().getMessages()) {
            msg.setFlag(Flag.DELETED, true);
            ++erasedCount;
        }
        return erasedCount;
    }

    @Override
    @RequiresPermissions({"mail:send", "mail:folder:read"})
    public int sendDrafts(String draftFolderName, String sentFolderName) throws MessagingException {
        @Cleanup Folder folder = new Folder(draftFolderName, jakarta.mail.Folder.READ_WRITE);
        @Cleanup Folder sentFolder = folder.getAnotherFolder(sentFolderName, jakarta.mail.Folder.READ_WRITE);
        @Cleanup Transport transport = null;
        int numSent = 0;
        for (Message msg : folder.getFolder().getMessages()) {
            if (msg.isSet(Flag.DELETED)) {
                continue;
            }
            var addrs = new LinkedList<Address>();
            addAddresses(addrs, msg, Message.RecipientType.TO);
            addAddresses(addrs, msg, Message.RecipientType.CC);
            addAddresses(addrs, msg, Message.RecipientType.BCC);
            if (transport == null) {
                transport = connectTransport();
            }
            transport.sendMessage(msg, addrs.toArray(Address[]::new));
            sentFolder.getFolder().appendMessages(new Message[] { msg });
            msg.setFlag(Flag.DELETED, true);

            ++numSent;
        }

        return numSent;
    }

    @Override
    public boolean isMock() {
        return false;
    }

    @Override
    @RequiresPermissions("mail:folder:read")
    public void pingImap() throws MessagingException {
        @Cleanup var store = connectImap(false);
    }

    @Override
    public void pingSmtp() throws MessagingException {
        @Cleanup var transport = connectTransport();
    }

    private void addAddresses(List<Address> addrs, Message msg, RecipientType type) throws MessagingException {
        Address[] addrArray = msg.getRecipients(type);
        if (addrArray != null) {
            addrs.addAll(Arrays.asList(addrArray));
        }
    }

    private Store connectImap(boolean authenticate) throws MessagingException {
        var store = mailSession.getStore();
        try {
            log.debug(mailSession.getProperties().toString());
            var principal = Optional.ofNullable(user.get()).orElseThrow(AuthenticationFailedException::new);
            store.connect(principal.getUserName().orElseThrow(MessagingException::new),
                    principal.getPassword().orElseThrow(MessagingException::new));
            return store;
        } catch (AuthenticationFailedException e) {
            store.close();
            if (!authenticate && e.getMessage().contains("failed to connect")) {
                throw new MessagingException(e.getMessage());
            } else {
                throw e;
            }
        } catch (MessagingException e) {
            store.close();
            throw e;
        }
    }

    @SneakyThrows(MessagingException.class)
    private Transport connectTransport() {
        var transport = mailSession.getTransport();
        transport.connect(smtpHost, smtpPort, smtpUser, smtpPassword);
        return transport;
    }

    private class Folder {
        @Getter
        private final jakarta.mail.Folder folder;
        private final Store store;
        Folder(String folderName, int options) throws MessagingException {
            store = connectImap(true);
            try {
                folder = store.getFolder(folderName);
                folder.open(options);
            } catch (MessagingException e) {
                store.close();
                throw e;
            }
        }

        private Folder(Store store, String folderName, int options) throws MessagingException {
            this.store = null;
            this.folder = store.getFolder(folderName);
            folder.open(options);
        }

        public Folder getAnotherFolder(String folderName, int options) throws MessagingException {
            return new Folder(store, folderName, options);
        }

        public void close() throws MessagingException {
            folder.close(true);
            if (store != null) {
                store.close();
            }
        }
    }
}
