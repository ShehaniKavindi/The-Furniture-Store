package lk.thefurniturestore.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Context;
import lk.thefurniturestore.dto.UserDTO;
import lk.thefurniturestore.entity.Address;
import lk.thefurniturestore.entity.Status;
import lk.thefurniturestore.entity.User;
import lk.thefurniturestore.mail.VerificationMail;
import lk.thefurniturestore.provider.MailServiceProvider;
import lk.thefurniturestore.util.AppUtil;
import lk.thefurniturestore.util.HibernateUtil;
import lk.thefurniturestore.validation.Validator;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class UserService {

    public String userLogin(UserDTO userDTO, @Context HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        //   login handling code / user authentication port-start
        if(userDTO.getEmail() == null){
            message = "Email is required!";
        }else if(userDTO.getEmail().isBlank()) {
            message = "Email cannot be empty!";
        }else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)){
            message = "Please enter a valid email!";
        }else if (userDTO.getPassword() == null) {
            message = "Password is required!";
        } else if (userDTO.getPassword().isBlank()) {
            message = "Password cannot be empty!";
        } else{
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            User singleUser = hibernateSession.createNamedQuery("User.getByEmail", User.class)
                    .setParameter("email", userDTO.getEmail())
                    .getSingleResultOrNull();
            if (singleUser == null){
                message = "User not found! Please register first!";
            }else {
                if (!singleUser.getPassword().equals(userDTO.getPassword())) {
                    message = "Something went wrong. Please check your login credentials!";
                }else {
                    Status verifiedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                            .setParameter("value", String.valueOf(Status.Type.VERIFIED))
                            .getSingleResult();
                    if (!singleUser.getStatus().equals(verifiedStatus)) {
                        message = "Your account is not verified. Please verify first!";
                    }else {
                        HttpSession httpSession = request.getSession();
                        httpSession.setAttribute("user", singleUser);
                        status = true;
                        message = "Login Successful!";
                    }
                }
            }
            hibernateSession.close();
        }

        //   login handling code / user authentication port-end

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String verifyUserAccount(UserDTO userDTO){
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        // logic handling part
        if(userDTO.getEmail() == null){
            message = "Email is required!";
        }else if(userDTO.getEmail().isBlank()) {
            message = "Email cannot be empty!";
        }else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)){
            message = "Please enter a valid email!";

        }else if (userDTO.getVerificationCode() == null){
            message = "Verification code is required";
        }else if (userDTO.getVerificationCode().isBlank()){
            message = "Verification code cannot be empty!";
        }else if(!userDTO.getVerificationCode().matches(Validator.VERIFICATION_CODE_VALIDATION)) {
            message = "Please provide a valid verification code! Verification code must have 6 digits";
        }else{
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            User user = hibernateSession.createQuery(
                            "FROM User u WHERE u.email = :email AND u.verificationCode = :verificationCode",
                            User.class)
                    .setParameter("email", userDTO.getEmail())
                    .setParameter("verificationCode", userDTO.getVerificationCode())
                    .getSingleResultOrNull();
            if (user == null) {
                message = "Account not found! Please register first!";
            }else{

                Status verifiedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", String.valueOf(Status.Type.VERIFIED))
                        .getSingleResult();

                if (user.getStatus().equals(verifiedStatus)){
                    message = "Account already verified!";
                } else {
                    user.setStatus(verifiedStatus);
                    user.setVerificationCode("");
                    Transaction transaction = hibernateSession.beginTransaction();
                    try{
                        hibernateSession.merge(user);
                        transaction.commit();
                        status = true;
                        message = "Account successfully verified!";
                    }catch (HibernateException e) {
                        transaction.rollback();
                        message = "Something went wrong. Verification process failed!";
                    }
                }

            }
            hibernateSession.close();
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String addNewUser(UserDTO userDTO){
        JsonObject responseObject = new JsonObject();

        boolean status = false;
        String message;

        if(userDTO.getFname() == null){
            message = "First name is required!";
        }else if(userDTO.getFname().isBlank()){
            message = "First name cannot be empty or blank!";
        }

        else if(userDTO.getLname() == null){
            message = "Last name is required!";
        }else if(userDTO.getLname().isBlank()){
            message = "Last name cannot be empty or blank!";
        }

        else if(userDTO.getEmail() == null){
            message = "Email is required!";
        }else if(userDTO.getEmail().isBlank()) {
            message = "Email cannot be empty!";
        }else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)){
            message = "Please enter a valid email!";
        }

        else if (userDTO.getPassword() == null) {
            message = "Password is required!";
        } else if (userDTO.getPassword().isBlank()) {
            message = "Password cannot be empty!";
        } else if (!userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "Please provide a valid password!  \n " +
                    "The password must be at least 10 characters long and include at least one uppercase letter, " +
                    "one lowercase letter, one digit and one special character";
        }else {
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            User singleUser = hibernateSession.createNamedQuery("User.getByEmail", User.class)
                    .setParameter("email", userDTO.getEmail())
                    .getSingleResultOrNull();

            if (singleUser != null){
                message = "This email already exists! Please use another email";
            }else {
                User u = new User();
                u.setFname(userDTO.getFname());
                u.setLname(userDTO.getLname());
                u.setEmail(userDTO.getEmail());
                u.setPassword(userDTO.getPassword());

                String verificationCode = AppUtil.generateCode();

                u.setVerificationCode(verificationCode);

                Status pendingStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", String.valueOf(Status.Type.PENDING)).getSingleResult();

                u.setStatus(pendingStatus);

                Transaction transaction = hibernateSession.beginTransaction();

                try{
                    hibernateSession.persist(u);
                    transaction.commit();

                    //   verification-mail-sending-start
                    VerificationMail verificationMail = new VerificationMail(u.getEmail(), verificationCode);
                    MailServiceProvider.getInstance().sendMail(verificationMail);
                    //   verification-mail-sending-end

                    status = true;
                    message = "Account created successfully! " +
                            "Verification code has been sent to your email. " +
                            "Please verify it for activate your account!";

                    // verification mail sending algorithm here
                }catch (HibernateException e){
                    transaction.rollback();
                    message = "Account creation failed! PLease Try Again!";
                }

            }
            hibernateSession.close();
        }
        responseObject.addProperty("status",status);
        responseObject.addProperty("message",message);
        return AppUtil.GSON.toJson(responseObject);
    }
}