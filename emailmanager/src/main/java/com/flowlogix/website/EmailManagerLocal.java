package com.flowlogix.website;

import javax.ejb.Local;

@Local
public interface EmailManagerLocal
{
    public void eraseFolder(String folderName);
    public boolean isMock();

    public int sendDrafts(String draftFolderName, String sentFolderName);
}
