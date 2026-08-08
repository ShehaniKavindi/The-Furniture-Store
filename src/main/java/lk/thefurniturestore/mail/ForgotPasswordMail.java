package lk.thefurniturestore.mail;

import io.rocketbase.mail.model.HtmlTextEmail;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import lk.thefurniturestore.util.Env;

public class ForgotPasswordMail extends Mailable {
    private final String to;
    private final String resetCode;

    public ForgotPasswordMail(String to, String resetCode) {
        this.to = to;
        this.resetCode = resetCode;
    }

    @Override
    public void build(Message message) throws MessagingException {
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject("Forgot Password - " + Env.get("app.name"));

        String appURL = Env.get("app.url");

        HtmlTextEmail htmlTextEmail = getEmailTemplateBuilder()
                .header()
                .logo("https://upload.wikimedia.org/wikipedia/commons/e/eb/SmartTradePI.png").logoHeight(40).and()
                .text("PASSWORD RECOVERY").h1().center().and()
                .text("We received a forgot password request for your account.").center().and()
                .text("Use this one-time reset code: " + resetCode).center().and()
                .button("Reset password", appURL + "/reset-password.html?email=" + to).blue().center().and()
                .text("If you did not request this, please ignore this email.").center().and()
                .copyright(Env.get("app.name")).url(appURL).suffix(". All Rights Reserved").and()
                .build();

        message.setContent(htmlTextEmail.getHtml(), "text/html; charset=utf-8");
    }
}
