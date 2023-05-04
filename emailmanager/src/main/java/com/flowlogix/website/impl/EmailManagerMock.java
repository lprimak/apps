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
    public void eraseFolder(String folderName) {
        // just a fake test
        logUserName();
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
            log.info("User: {}", auth.get().getUserName());
        } else {
            log.info("User: <none>");
        }
    }
}
