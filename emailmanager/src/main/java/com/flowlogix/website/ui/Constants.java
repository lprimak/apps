package com.flowlogix.website.ui;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author lprimak
 */
@ApplicationScoped
@Getter
public class Constants {
    @Inject
    @ConfigProperty(name = "com.flowlogix.pam-service-name", defaultValue = "pwauth")
    String pamAuthServiceName;
    @Inject
    @ConfigProperty(name = "com.flowlogix.junk-folder-name", defaultValue = "Junk")
    String junkFolderName;
    @Inject
    @ConfigProperty(name = "com.flowlogix.draft-folder-name", defaultValue = "Drafts")
    String draftFolderName;
    @Inject
    @ConfigProperty(name = "com.flowlogix.sent-folder-name", defaultValue = "Sent")
    String sentFolderName;
}
