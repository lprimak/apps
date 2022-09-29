package com.flowlogix.website.ui;

import com.flowlogix.website.EmailManagerLocal;
import java.io.Serializable;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.omnifaces.util.JNDIObjectLocator;
import org.primefaces.PrimeFaces;

/**
 *
 * @author lprimak
 */
@Named @SessionScoped
@RequiresPermissions({"mail:junk:erase", "mail:draft:send"})
public class EmailManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String EMAIL_MESSAGE_ATTR = "com.flowlogix.emailmanager.email-message";
    private final EmailManagerLocal emailManager;
    private final JNDIObjectLocator locator = JNDIObjectLocator.builder().build();
    private final EmailManagerLocal eraserImpl = locator.getObject("java:module/EmailManagerImpl");
    private final EmailManagerLocal eraserMock = locator.getObject("java:module/EmailManagerMock");
    @Inject
    private Constants constants;
    private String emailStatus;

    public EmailManager() {
        var eraser = eraserImpl;
        try {
            eraserImpl.isMock();
        } catch (EJBException e) {
            eraser = eraserMock;
        }
        this.emailManager = eraser;
    }

    public void eraseJunk() {
        emailManager.eraseFolder(constants.getJunkFolderName());
        setMockMessage("Erased Junk Mail");
    }

    public void sendDrafts() {
        int numSent = emailManager.sendDrafts(constants.getDraftFolderName(),
                constants.getSentFolderName());
        if (numSent > 0) {
            setMockMessage(String.format("Draft E-Mail%s Sent (%d)", (numSent > 1) ? "s" : "", numSent));
        } else {
            setMockMessage("No Draft E-Mail to Send");
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

    private void setMockMessage(String junkErasedMessage) {
        if (emailManager.isMock()) {
            emailStatus = junkErasedMessage + " (Mock)";
        } else {
            emailStatus = junkErasedMessage;
        }
        PrimeFaces.current().executeScript("PF('poll').start()");
    }
}
