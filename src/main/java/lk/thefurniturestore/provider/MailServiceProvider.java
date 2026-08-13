package lk.thefurniturestore.provider;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import lk.thefurniturestore.mail.Mailable;
import lk.thefurniturestore.util.Env;

import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MailServiceProvider {
    private ThreadPoolExecutor executor;
    private Authenticator authenticator;
    private final Properties properties = new Properties();
    private static MailServiceProvider mailServiceProvider;
    private final boolean enabled;

    private MailServiceProvider() {
        String host = Env.get("mail.host");
        String port = Env.get("mail.port");
        enabled = host != null && !host.isBlank() && port != null && !port.isBlank();

        if (!enabled) {
            return;
        }

        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.starttls.required", true);
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

    }

    public static MailServiceProvider getInstance() {
        if (mailServiceProvider == null) {
            mailServiceProvider = new MailServiceProvider();
        }
        return mailServiceProvider;
    }

    public void start() {
        if (!enabled) {
            System.out.println("Email service is disabled: configure MAIL_HOST and MAIL_PORT to enable email delivery.");
            return;
        }

        authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Env.get("mail.username"), Env.get("mail.password"));
            }
        };
        executor = new ThreadPoolExecutor(2, 5, 5,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.AbortPolicy());
        System.out.println("\u001B[32mEmailServiceProvider Initialized...\u001B[32m");
    }

    public Properties getProperties() {
        return properties;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    public void sendMail(Mailable mailable){
        if (executor == null) {
            throw new IllegalStateException("Email service is not configured.");
        }
        executor.execute(mailable);
    }
}
