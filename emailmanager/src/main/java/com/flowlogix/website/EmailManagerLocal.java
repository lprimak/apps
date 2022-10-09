package com.flowlogix.website;

import javax.ejb.Local;
import javax.mail.MessagingException;

@Local
public interface EmailManagerLocal
{
    void eraseFolder(String folderName);
    boolean isMock();

    int sendDrafts(String draftFolderName, String sentFolderName);

    void pingImap() throws MessagingException;
    void pingSmtp() throws MessagingException;
}
