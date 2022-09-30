package com.flowlogix.website.ui;

import javax.annotation.sql.DataSourceDefinition;
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
@DataSourceDefinition(name = "java:app/jdbc/Hope", className = "org.postgresql.ds.PGSimpleDataSource",
        serverName = "${MPCONFIG=hope-db-hostname:nova.hope.nyc.ny.us}", portNumber = 5432,
        databaseName = "hope",
        user = "${MPCONFIG=hope-db-username:}", password = "${MPCONFIG=hope-db-password:}",
        maxPoolSize = 32, minPoolSize = 8,
        properties = {
            "fish.payara.is-connection-validation-required = true",
            "fish.payara.connection-validation-method = auto-commit"})
public class Constants {
    @Inject
    @ConfigProperty(name = "com.flowlogix.pam-service-name", defaultValue = "login")
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

    @Inject
    @ConfigProperty(name = "com.flowlogix.cipher-key", defaultValue = " ")
    String cipherKey;
}
