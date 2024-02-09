package com.flowlogix.website.ui;

import com.flowlogix.website.EmailManagerLocal;
import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import static lombok.AccessLevel.PACKAGE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
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
            displayMessage("Erased Junk Mail (%d)".formatted(erasedCount));
        } catch (MessagingException e) {
            log.debug("failed to erase junk mail", e);
            displayMessage("Failed to erase junk mail: " + e.getMessage());
        }
    }

    @RequiresPermissions("mail:draft:send")
    public void sendDrafts() {
        try {
            int numSent = emailManager.get().sendDrafts(constants.getDraftFolderName(),
                    constants.getSentFolderName());
            if (numSent > 0) {
                displayMessage(String.format("Draft E-Mail%s Sent (%d)", (numSent > 1) ? "s" : "", numSent));
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
