/*
 * Copyright (C) 2011-2024 Flow Logix, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flowlogix.website.ui;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author lprimak
 */
@ApplicationScoped
@Getter
@DataSourceDefinition(name = "java:app/jdbc/Hope", className = "org.postgresql.ds.PGSimpleDataSource",
        url = "${MPCONFIG=hope-db-url:jdbc:postgresql://nova.hope.nyc.ny.us/hope?sslmode=disable}",
        user = "${MPCONFIG=hope-db-username:}", password = "${MPCONFIG=hope-db-password:}",
        maxPoolSize = 32, minPoolSize = 8,
        properties = {
            "fish.payara.is-connection-validation-required = true",
            "fish.payara.connection-validation-method = auto-commit",
            "fish.payara.fail-all-connections = true"})
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

    @Getter @Setter
    boolean unixRealmAvailable = true;
}
