package com.flowlogix.website;

import javax.ejb.Local;
import javax.mail.MessagingException;

@Local
public interface EmailManagerLocal
{
    void eraseFolder(String folderName) throws MessagingException;
    boolean isMock();

    int sendDrafts(String draftFolderName, String sentFolderName) throws MessagingException;

    void pingImap() throws MessagingException;
    void pingSmtp() throws MessagingException;
}
