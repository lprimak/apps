package com.flowlogix.website.pages;

import lombok.Getter;

import com.flowlogix.website.EmailManagerLocal;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.validation.constraints.NotNull;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;


@RequiresPermissions({"mail:junk:erase", "mail:draft:send"})
public class EmailManager
{
    public EmailManager()
    {
        EmailManagerLocal _eraser = eraserImpl;
        try
        {
            eraserImpl.isMock();
        }
        catch(EJBException e)
        {
            _eraser = eraserMock;
        }
        this.emailManager = _eraser;
    }


    @SetupRender
    void init()
    {
        if(emailStatus == null)
        {
            emailStatus = "<None>";
        }
    }


    @SuppressWarnings("unused")
    @OnEvent(value = "eraseJunk")
    @AJAX(discardAfter = true, requireSession = false)
    private Block eraseJunkMail()
    {
        emailManager.eraseFolder(junkFolderName);
        setMockMessage("Erased Junk Mail");
        return status.getBody();
    }


    @SuppressWarnings("unused")
    @OnEvent(value = "sendDrafts")
    @AJAX(discardAfter = true, requireSession = false)
    private Block sendDrafts()
    {
        int numSent = emailManager.sendDrafts(draftFolderName, sentFolderName);
        if(numSent > 0)
        {
            setMockMessage(String.format("Draft E-Mail%s Sent (%d)", (numSent > 1)? "s": "", numSent));
        }
        else
        {
            setMockMessage("No Draft E-Mail to Send");
        }
        return status.getBody();
    }


    @SuppressWarnings("unused")
    @OnEvent(value="updatestatus", component="status")
    private Block updateStatus()
    {
        init();
        return status.getBody();
    }


    @OnEvent(value = "logout")
    private void logout()
    {
        SecurityUtils.getSubject().logout();
    }


    private void setMockMessage(final String junkErasedMessage)
    {
        if (emailManager.isMock())
        {
            emailStatus = junkErasedMessage + " (Mock)";
        }
        else
        {
            emailStatus = junkErasedMessage;
        }
    }


    @Getter @Persist(PersistenceConstants.FLASH) private String emailStatus;
    @EJB(beanName = "EmailManagerImpl") private EmailManagerLocal eraserImpl;
    @EJB(beanName = "EmailManagerMock") private EmailManagerLocal eraserMock;
    private final EmailManagerLocal emailManager;
    @InjectComponent private Zone status;
    @Inject private Request request;
    @Inject private ComponentResources cr;
    private @Parameter(defaultPrefix = BindingConstants.LITERAL, value = "#5AACFD") @NotNull String highlightColor;
    private @Mixin ColorHighlightOverride cho;
    private @Mixin DisableAfterSubmit das;
    private @Inject @Symbol(HopeModule.JUNK_FOLDER_NAME) String junkFolderName;
    private @Inject @Symbol(HopeModule.DRAFT_FOLDER_NAME) String draftFolderName;
    private @Inject @Symbol(HopeModule.SENT_FOLDER_NAME) String sentFolderName;
}
