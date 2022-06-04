package com.flowlogix.website.services;

import com.flowlogix.website.security.UnixRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.FactoryDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.got5.tapestry5.jquery.JQuerySymbolConstants;
import org.tynamo.security.SecuritySymbols;


public class HopeModule
{
    public static void bind(ServiceBinder binder)
    {
        // binder.bind(MyServiceInterface.class, MyServiceImpl.class);

        // Make bind() calls on the binder object to define most IoC services.
        // Use service builder methods (example below) when the implementation
        // is provided inline, or requires more initialization than simply
        // invoking the constructor.
    }

    
    @Contribute(SymbolProvider.class)
    @ApplicationDefaults
    public void setAppDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(SymbolConstants.PRODUCTION_MODE, "false");
        configuration.add(SymbolConstants.APPLICATION_VERSION, "0.0.1-tap5.3.7");
        configuration.add(SymbolConstants.HMAC_PASSPHRASE, HopeModule.class.getName());
        configuration.add(SecuritySymbols.LOGIN_URL, "flowlogix/security/login");
        configuration.add(JQuerySymbolConstants.SUPPRESS_PROTOTYPE, Boolean.FALSE.toString());
        configuration.add(JQuerySymbolConstants.JQUERY_ALIAS, "$j");
        configuration.add(JQuerySymbolConstants.USE_MINIFIED_JS, Boolean.FALSE.toString());
    }

    
    @Contribute(SymbolProvider.class)
    @FactoryDefaults
    public void setFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(PAM_AUTH_SERVICE_NAME, "pwauth");
        configuration.add(JUNK_FOLDER_NAME, "Junk");
        configuration.add(DRAFT_FOLDER_NAME, "Drafts");
        configuration.add(SENT_FOLDER_NAME, "Sent");
    }
    
    
    @Contribute(WebSecurityManager.class)
    public void initPamRealm(Configuration<Realm> configuration,
    @Symbol(PAM_AUTH_SERVICE_NAME) String serviceName)
    {
        configuration.add(new UnixRealm(serviceName));
    }
    
    
    public static final String PAM_AUTH_SERVICE_NAME = "com.flowlogix.pam-service-name";
    public static final String JUNK_FOLDER_NAME = "com.flowlogix.junk-folder-name";
    public static final String DRAFT_FOLDER_NAME = "com.flowlogix.draft-folder-name";
    public static final String SENT_FOLDER_NAME = "com.flowlogix.sent-folder-name";
}
