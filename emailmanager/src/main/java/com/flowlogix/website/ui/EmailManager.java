package com.flowlogix.website.ui;

import com.flowlogix.website.EmailManagerLocal;
import javax.ejb.EJBException;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import lombok.Getter;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.omnifaces.util.JNDIObjectLocator;
import org.primefaces.PrimeFaces;

/**
 *
 * @author lprimak
 */
@Model
@RequiresPermissions({"mail:junk:erase", "mail:draft:send"})
public class EmailManager {
    private final EmailManagerLocal emailManager;
    private final JNDIObjectLocator locator = JNDIObjectLocator.builder().build();
    private final EmailManagerLocal eraserImpl = locator.getObject("java:module/EmailManagerImpl");
    private final EmailManagerLocal eraserMock = locator.getObject("java:module/EmailManagerMock");
    @Inject
    private Constants constants;
    private @Getter String emailStatus = "<None>";

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

    private void setMockMessage(String junkErasedMessage) {
        if (emailManager.isMock()) {
            emailStatus = junkErasedMessage + " (Mock)";
        } else {
            emailStatus = junkErasedMessage;
        }
        PrimeFaces.current().executeScript("PF('poll').start()");
    }
}
