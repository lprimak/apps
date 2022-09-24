package com.flowlogix.website;

import javax.ejb.Local;

@Local
public interface EmailManagerLocal
{
    void eraseFolder(String folderName);
    boolean isMock();

    int sendDrafts(String draftFolderName, String sentFolderName);
}
