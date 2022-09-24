package com.flowlogix.website.ui;

/**
 *
 * @author lprimak
 */
public class Constants {
    public static final String PAM_AUTH_SERVICE_NAME =
            System.getProperty("com.flowlogix.pam-service-name", "pwauth");
    public static final String JUNK_FOLDER_NAME =
            System.getProperty("com.flowlogix.junk-folder-name", "Junk");
    public static final String DRAFT_FOLDER_NAME =
            System.getProperty("com.flowlogix.draft-folder-name", "Drafts");
    public static final String SENT_FOLDER_NAME =
            System.getProperty("com.flowlogix.sent-folder-name", "Sent");
}
