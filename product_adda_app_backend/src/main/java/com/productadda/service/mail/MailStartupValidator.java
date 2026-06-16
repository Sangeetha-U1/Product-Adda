package com.productadda.service.mail;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;

@Component
public class MailStartupValidator implements ApplicationRunner {

    /*
     * =============================================================================
     * SMTP ENABLE FLAG
     * =============================================================================
     */

    @Value("${app.mail.smtp-enabled}")
    private boolean smtpEnabled;

    /*
     * =============================================================================
     * SMTP CONFIGURATION
     * =============================================================================
     */

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.ssl.enable}")
    private boolean sslEnabled;

    /*
     * =============================================================================
     * VALIDATE SMTP CONNECTION ON APPLICATION STARTUP
     * =============================================================================
     */

    @Override
    public void run(
            ApplicationArguments args)
            throws Exception {

        /*
         * =========================================================================
         * SKIP SMTP AUTHENTICATION
         * =========================================================================
         */

        if (!smtpEnabled) {

            System.out.println("""
                    
                    ===========================================================================
                    SMTP AUTHENTICATION SKIPPED
                    
                    Reason   : app.mail.smtp-enabled=false
                    
                    ===========================================================================
                    """);

            return;
        }

        /*
         * =========================================================================
         * BUILD MAIL PROPERTIES
         * =========================================================================
         */

        Properties properties = new Properties();

        properties.put(
                "mail.smtp.auth",
                "true");

        properties.put(
                "mail.smtp.host",
                host);

        properties.put(
                "mail.smtp.port",
                String.valueOf(
                        port));

        properties.put(
                "mail.smtp.ssl.enable",
                String.valueOf(
                        sslEnabled));

        /*
         * =========================================================================
         * CREATE MAIL SESSION
         * =========================================================================
         */

        Session session = Session.getInstance(
                properties,
                new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                username,
                                password);
                    }
                });

        /*
         * =========================================================================
         * VALIDATE SMTP AUTHENTICATION
         * =========================================================================
         */

        Transport transport = session.getTransport(
                "smtp");

        transport.connect();

        transport.close();

        /*
         * =========================================================================
         * SUCCESS LOG
         * =========================================================================
         */

        System.out.println("""
                
                ===========================================================================
                SMTP AUTHENTICATION SUCCESSFUL
                
                Host     : %s
                Port     : %s
                Username : %s
                SSL      : %s
                
                ===========================================================================
                """
                .formatted(
                        host,
                        port,
                        username,
                        sslEnabled
                                ? "ENABLED"
                                : "DISABLED"));
    }
}