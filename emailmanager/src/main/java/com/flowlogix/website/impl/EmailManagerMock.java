package com.flowlogix.website.impl;

import com.flowlogix.website.EmailManagerLocal;
import com.flowlogix.website.security.UserAuth;
import javax.ejb.Stateless;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;


@Stateless
@Slf4j
public class EmailManagerMock implements EmailManagerLocal
{
    @Override
    public void eraseFolder(String folderName)
    {
        // just a fake test
        logUserName();
    }

    
    @Override
    public int sendDrafts(String draftFolderName, String sentFolderName)
    {
        logUserName();
        return 0;
    }
    
    
    @Override
    public boolean isMock()
    {
        return true;
    }
    
    
    private void logUserName()
    {
        UserAuth auth = SecurityUtils.getSubject().getPrincipals().oneByType(UserAuth.class);
        log.info("User: " + auth.getUserName());        
    }
}
