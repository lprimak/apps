package com.flowlogix.website.impl;


import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import com.flowlogix.website.EmailManagerLocal;

import com.flowlogix.website.security.UserAuth;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.MailSessionDefinition;
import javax.mail.Transport;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Stateless
@MailSessionDefinition(name = "java:app/mail/HopeMail",
        host = "${MPCONFIG=hope-imap-host:}", transportProtocol = "smtps", storeProtocol = "imaps")
@Slf4j
public class EmailManagerImpl implements EmailManagerLocal {
    @Resource(name = "java:app/mail/HopeMail")
    Session mailSession;
    @Inject
    @ConfigProperty(name = "hope-smtp-host", defaultValue = "none")
    private String smtp_host;
    @Inject
    @ConfigProperty(name = "hope-smtp-user", defaultValue = "none")
    private String smtp_user;
    @Inject
    @ConfigProperty(name = "hope-smtp-password", defaultValue = "none")
    private String smtp_password;

    @PostConstruct
    @SneakyThrows(MessagingException.class)
    void init() {
        @Cleanup var transport = mailSession.getTransport();
        transport.connect(smtp_host, smtp_user, smtp_password);
        @Cleanup var store = mailSession.getStore();
        UserAuth user = (UserAuth) SecurityUtils.getSubject().getPrincipal();
        Objects.requireNonNull(user, "not authenticated");
        store.connect(user.getUserName(), user.getPassword());
    }

    @Override
    @SneakyThrows(MessagingException.class)
    public void eraseFolder(String folderName) {
        @Cleanup
        Folder folder = new Folder(folderName, javax.mail.Folder.READ_WRITE);
        for (Message msg : folder.getFolder().getMessages()) {
            msg.setFlag(Flag.DELETED, true);
        }
    }

    @Override
    @SneakyThrows(MessagingException.class)
    public int sendDrafts(String draftFolderName, String sentFolderName) {
        @Cleanup Folder folder = new Folder(draftFolderName, javax.mail.Folder.READ_WRITE);
        @Cleanup Transport transport = mailSession.getTransport();
        transport.connect(smtp_host, smtp_user, smtp_password);
        @Cleanup Folder sentFolder = folder.getAnotherFolder(sentFolderName, javax.mail.Folder.READ_WRITE);
        int numSent = 0;
        for (Message msg : folder.getFolder().getMessages()) {
            if (msg.isSet(Flag.DELETED)) {
                continue;
            }
            var addrs = new LinkedList<Address>();
            addAddresses(addrs, msg, Message.RecipientType.TO);
            addAddresses(addrs, msg, Message.RecipientType.CC);
            addAddresses(addrs, msg, Message.RecipientType.BCC);
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

    private void addAddresses(List<Address> addrs, Message msg, RecipientType type) throws MessagingException {
        Address[] addrArray = msg.getRecipients(type);
        if (addrArray != null) {
            addrs.addAll(Arrays.asList(addrArray));
        }
    }

    private class Folder {
        @Getter
        private final javax.mail.Folder folder;
        private final Store store;
        public Folder(String folderName, int options) throws MessagingException {
            store = mailSession.getStore();
            try {
                log.debug(mailSession.getProperties().toString());
                UserAuth user = (UserAuth) SecurityUtils.getSubject().getPrincipal();
                store.connect(user.getUserName(), user.getPassword());
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
