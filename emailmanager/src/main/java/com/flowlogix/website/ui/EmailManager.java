package com.flowlogix.website.ui;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.primefaces.PrimeFaces;

/**
 *
 * @author lprimak
 */
@Named @SessionScoped
@RequiresPermissions({"mail:junk:erase", "mail:draft:send"})
public class EmailManager implements Serializable {
    private static final long serialVersionUID = 1L;
    @Inject
    private Constants constants;
    @Inject
    private EmailManagerProducer emailManager;
    private String emailStatus;

    public void eraseJunk() {
        emailManager.get().eraseFolder(constants.getJunkFolderName());
        displayMessage("Erased Junk Mail");
    }

    public void sendDrafts() {
        int numSent = emailManager.get().sendDrafts(constants.getDraftFolderName(),
                constants.getSentFolderName());
        if (numSent > 0) {
            displayMessage(String.format("Draft E-Mail%s Sent (%d)", (numSent > 1) ? "s" : "", numSent));
        } else {
            displayMessage("No Draft E-Mail to Send");
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
}
