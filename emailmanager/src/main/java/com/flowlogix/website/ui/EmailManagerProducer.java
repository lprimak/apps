package com.flowlogix.website.ui;

import com.flowlogix.website.EmailManagerLocal;
import javax.enterprise.context.ApplicationScoped;
import javax.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.JNDIObjectLocator;
import org.omnifaces.util.Lazy;

/**
 *
 * @author lprimak
 */
@ApplicationScoped
@Slf4j
public class EmailManagerProducer {
    private final JNDIObjectLocator locator = JNDIObjectLocator.builder().build();
    private final Lazy<EmailManagerLocal> emailManager = new Lazy<>(this::createEmailManager);

    public EmailManagerLocal get() {
        return emailManager.get();
    }

    private EmailManagerLocal createEmailManager() {
        try {
            EmailManagerLocal eraserImpl = locator.getObject("java:module/EmailManagerImpl");
            eraserImpl.pingImap();
            return eraserImpl;
        } catch (MessagingException e) {
            log.warn("Ping Imap", e);
            return locator.getObject("java:module/EmailManagerMock");
        }
    }
}
