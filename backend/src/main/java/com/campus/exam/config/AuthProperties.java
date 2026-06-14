package com.campus.exam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    /**
     * Whether unauthenticated users may self-register with their student/employee id.
     * The id is unique, so one person can only create one account.
     */
    private boolean publicRegistrationEnabled = true;

    /**
     * Demo-only switch for an unauthenticated password reset endpoint.
     */
    private boolean demoPasswordResetEnabled = false;

    public boolean isPublicRegistrationEnabled() {
        return publicRegistrationEnabled;
    }

    public void setPublicRegistrationEnabled(boolean publicRegistrationEnabled) {
        this.publicRegistrationEnabled = publicRegistrationEnabled;
    }

    public boolean isDemoPasswordResetEnabled() {
        return demoPasswordResetEnabled;
    }

    public void setDemoPasswordResetEnabled(boolean demoPasswordResetEnabled) {
        this.demoPasswordResetEnabled = demoPasswordResetEnabled;
    }
}
