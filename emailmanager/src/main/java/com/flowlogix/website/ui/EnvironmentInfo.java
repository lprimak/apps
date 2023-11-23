package com.flowlogix.website.ui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author lprimak
 */
@Named
@ApplicationScoped
public class EnvironmentInfo {
    private final String version;
    private final String gitId;
    private final String buildTime;

    public EnvironmentInfo() {
        Properties props = new Properties();
        try ( InputStream is = getClass().getClassLoader().getResourceAsStream("git.properties")) {
            props.load(new BufferedInputStream(is));
        } catch (IOException ex) {
            ExceptionUtils.asRuntimeException(ex);
        }

        version = props.getProperty("git.build.version");
        gitId = props.getProperty("git.commit.id.abbrev");
        buildTime = props.getProperty("git.build.time");
    }

    public String getVersion() {
        return String.join("-", version, gitId);
    }

    public String getBuildTime() {
        return buildTime;
    }
}
