package lk.thefurniturestore.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Context;
import lk.thefurniturestore.dto.UserDTO;
import lk.thefurniturestore.entity.Address;
import lk.thefurniturestore.entity.City;
import lk.thefurniturestore.entity.District;
import lk.thefurniturestore.entity.Province;
import lk.thefurniturestore.entity.Status;
import lk.thefurniturestore.entity.User;
import lk.thefurniturestore.mail.ForgotPasswordMail;
import lk.thefurniturestore.mail.VerificationMail;
import lk.thefurniturestore.provider.MailServiceProvider;
import lk.thefurniturestore.util.AppUtil;
import lk.thefurniturestore.util.HibernateUtil;
import lk.thefurniturestore.util.PasswordUtil;
import lk.thefurniturestore.util.RememberMeUtil;
import lk.thefurniturestore.validation.Validator;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class UserService {

    public String userLogin(UserDTO userDTO, @Context HttpServletRequest request, HttpServletResponse response) {
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
                if (!PasswordUtil.matches(userDTO.getPassword(), singleUser.getPassword())) {
                    message = "Something went wrong. Please check your login credentials!";
                }else {
                    Status verifiedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                            .setParameter("value", String.valueOf(Status.Type.VERIFIED))
                            .getSingleResult();
                    if (!singleUser.getStatus().equals(verifiedStatus)) {
                        message = "Your account is not verified. Please verify first!";
                    }else {
                        if (PasswordUtil.needsUpgrade(singleUser.getPassword())) {
                            Transaction transaction = hibernateSession.beginTransaction();
                            singleUser.setPassword(PasswordUtil.hash(userDTO.getPassword()));
                            hibernateSession.merge(singleUser);
                            transaction.commit();
                        }
                        HttpSession httpSession = request.getSession(true);
                        request.changeSessionId();
                        httpSession.setAttribute("user", singleUser);
                        if (userDTO.isRememberMe()) {
                            RememberMeUtil.issue(request, response, singleUser);
                        }
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

    public String forgotPassword(UserDTO userDTO) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;

        if (userDTO == null || userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            message = "email is required";
        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please enter a valid email!";
        } else {
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            try {
                User user = hibernateSession.createNamedQuery("User.getByEmail", User.class)
                        .setParameter("email", userDTO.getEmail().trim())
                        .getSingleResultOrNull();

                if (user == null) {
                    message = "email is not found";
                } else {
                    String resetCode = AppUtil.generateCode();
                    Transaction transaction = hibernateSession.beginTransaction();
                    user.setVerificationCode(resetCode);
                    hibernateSession.merge(user);
                    transaction.commit();
                    ForgotPasswordMail forgotPasswordMail = new ForgotPasswordMail(user.getEmail(), resetCode);
                    MailServiceProvider.getInstance().sendMail(forgotPasswordMail);
                    status = true;
                    message = "A password reset code has been sent to your email.";
                }
            } catch (Exception e) {
                message = "Failed to send password email. Please try again.";
                e.printStackTrace();
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String resetPassword(UserDTO userDTO) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;

        if (userDTO == null || userDTO.getEmail() == null || !userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please enter a valid email address.";
        } else if (userDTO.getVerificationCode() == null || !userDTO.getVerificationCode().matches(Validator.VERIFICATION_CODE_VALIDATION)) {
            message = "Please enter the six-digit reset code.";
        } else if (userDTO.getPassword() == null || !userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "Password must be at least 10 characters and include uppercase, lowercase, digit and special character.";
        } else {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                User user = session.createQuery(
                                "FROM User u WHERE u.email = :email AND u.verificationCode = :code",
                                User.class)
                        .setParameter("email", userDTO.getEmail().trim())
                        .setParameter("code", userDTO.getVerificationCode())
                        .getSingleResultOrNull();

                if (user == null) {
                    message = "The reset code is invalid or has already been used.";
                } else {
                    Transaction transaction = session.beginTransaction();
                    user.setPassword(PasswordUtil.hash(userDTO.getPassword()));
                    user.setVerificationCode("");
                    session.merge(user);
                    transaction.commit();
                    status = true;
                    message = "Password reset successfully. You can now log in.";
                }
            } catch (Exception e) {
                message = "Unable to reset your password. Please try again.";
                e.printStackTrace();
            }
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
                u.setPassword(PasswordUtil.hash(userDTO.getPassword()));

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

    public String getUserProfile(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        User sessionUser = getSessionUser(request);

        if (sessionUser == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Please login to view your profile.");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        try {
            User user = hibernateSession.get(User.class, sessionUser.getId());
            if (user == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "User not found. Please login again.");
            } else {
                UserDTO userDTO = toUserProfileDTO(hibernateSession, user);
                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "Profile loaded successfully.");
                responseObject.add("data", AppUtil.GSON.toJsonTree(userDTO));
            }
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load profile details.");
            e.printStackTrace();
        } finally {
            hibernateSession.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String updateUserProfile(UserDTO userDTO, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        User sessionUser = getSessionUser(request);
        boolean status = false;
        String message;

        if (sessionUser == null) {
            message = "Please login to update your profile.";
        } else if (userDTO == null) {
            message = "Profile details are required.";
        } else if (userDTO.getFname() == null || userDTO.getFname().isBlank()) {
            message = "First name is required!";
        } else if (userDTO.getLname() == null || userDTO.getLname().isBlank()) {
            message = "Last name is required!";
        } else if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            message = "Email is required!";
        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please enter a valid email!";
        } else if (userDTO.getLine1() == null || userDTO.getLine1().isBlank()) {
            message = "Address line 01 is required!";
        } else if (userDTO.getMobile() == null || userDTO.getMobile().isBlank()) {
            message = "Contact number is required!";
        } else if (!userDTO.getMobile().matches(Validator.MOBILE_VALIDATION)) {
            message = "Please enter a valid contact number!";
        } else if (userDTO.getProvinceId() <= 0) {
            message = "Province is required!";
        } else if (userDTO.getDistrictId() <= 0) {
            message = "District is required!";
        } else if (userDTO.getCityId() <= 0) {
            message = "City is required!";
        } else if (userDTO.getPostalCode() != null && !userDTO.getPostalCode().isBlank()
                && !userDTO.getPostalCode().matches(Validator.POSTAL_CODE_VALIDATION)) {
            message = "Please enter a valid postal code!";
        } else {
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;
            try {
                User duplicateUser = hibernateSession.createQuery(
                                "FROM User u WHERE u.email = :email AND u.id <> :userId", User.class)
                        .setParameter("email", userDTO.getEmail())
                        .setParameter("userId", sessionUser.getId())
                        .getSingleResultOrNull();

                if (duplicateUser != null) {
                    message = "This email already exists! Please use another email.";
                } else {
                    City city = hibernateSession.createQuery(
                                    "FROM City c JOIN FETCH c.district d JOIN FETCH d.province WHERE c.id = :cityId AND d.id = :districtId AND d.province.id = :provinceId",
                                    City.class)
                            .setParameter("cityId", userDTO.getCityId())
                            .setParameter("districtId", userDTO.getDistrictId())
                            .setParameter("provinceId", userDTO.getProvinceId())
                            .getSingleResultOrNull();

                    if (city == null) {
                        message = "Please select a valid province, district and city.";
                        responseObject.addProperty("status", false);
                        responseObject.addProperty("message", message);
                        return AppUtil.GSON.toJson(responseObject);
                    }

                    transaction = hibernateSession.beginTransaction();

                    User user = hibernateSession.get(User.class, sessionUser.getId());
                    user.setFname(userDTO.getFname().trim());
                    user.setLname(userDTO.getLname().trim());
                    user.setEmail(userDTO.getEmail().trim());

                    Address address = getUserAddress(hibernateSession, user.getId());
                    if (address == null) {
                        address = new Address();
                        address.setUser(user);
                        address.setLine1(userDTO.getLine1().trim());
                        address.setLine2(clean(userDTO.getLine2()));
                        address.setPostalCode(clean(userDTO.getPostalCode()));
                        address.setMobile(userDTO.getMobile().trim());
                        address.setCity(city);
                        hibernateSession.persist(address);
                    } else {
                        address.setLine1(userDTO.getLine1().trim());
                        address.setLine2(clean(userDTO.getLine2()));
                        address.setPostalCode(clean(userDTO.getPostalCode()));
                        address.setMobile(userDTO.getMobile().trim());
                        address.setCity(city);
                        hibernateSession.merge(address);
                    }

                    hibernateSession.merge(user);
                    transaction.commit();

                    request.getSession().setAttribute("user", user);
                    status = true;
                    message = "Profile details updated successfully.";
                }
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                message = "Failed to update profile details.";
                e.printStackTrace();
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    private User getSessionUser(HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            return null;
        }
        return (User) httpSession.getAttribute("user");
    }

    private UserDTO toUserProfileDTO(Session hibernateSession, User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFname(user.getFname());
        userDTO.setLname(user.getLname());
        userDTO.setEmail(user.getEmail());

        Address address = getUserAddress(hibernateSession, user.getId());
        if (address != null) {
            userDTO.setLine1(address.getLine1());
            userDTO.setLine2(address.getLine2());
            userDTO.setPostalCode(address.getPostalCode());
            userDTO.setMobile(address.getMobile());

            if (address.getCity() != null) {
                userDTO.setCityId(address.getCity().getId());
                userDTO.setCityName(address.getCity().getName());

                if (address.getCity().getDistrict() != null) {
                    userDTO.setDistrictId(address.getCity().getDistrict().getId());
                    userDTO.setDistrictName(address.getCity().getDistrict().getName());

                    if (address.getCity().getDistrict().getProvince() != null) {
                        userDTO.setProvinceId(address.getCity().getDistrict().getProvince().getId());
                        userDTO.setProvinceName(address.getCity().getDistrict().getProvince().getName());
                    }
                }
            }
        }

        return userDTO;
    }

    private Address getUserAddress(Session hibernateSession, int userId) {
        return hibernateSession.createQuery(
                        "FROM Address a LEFT JOIN FETCH a.city c LEFT JOIN FETCH c.district d LEFT JOIN FETCH d.province WHERE a.user.id = :userId",
                        Address.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getSingleResultOrNull();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public String getProvinces() {
        JsonObject responseObject = new JsonObject();
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        try {
            List<Province> provinceList = hibernateSession.createQuery(
                    "FROM Province p ORDER BY p.name", Province.class).list();

            JsonArray provinceArray = new JsonArray();
            for (Province province : provinceList) {
                JsonObject provinceObject = new JsonObject();
                provinceObject.addProperty("id", province.getId());
                provinceObject.addProperty("name", province.getName());
                provinceArray.add(provinceObject);
            }

            responseObject.addProperty("status", true);
            responseObject.add("data", provinceArray);
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load provinces.");
            e.printStackTrace();
        } finally {
            hibernateSession.close();
        }
        return AppUtil.GSON.toJson(responseObject);
    }

    public String getDistrictsByProvince(int provinceId) {
        JsonObject responseObject = new JsonObject();
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        try {
            List<District> districtList = hibernateSession.createQuery(
                            "FROM District d WHERE d.province.id = :provinceId ORDER BY d.name",
                            District.class)
                    .setParameter("provinceId", provinceId)
                    .list();

            JsonArray districtArray = new JsonArray();
            for (District district : districtList) {
                JsonObject districtObject = new JsonObject();
                districtObject.addProperty("id", district.getId());
                districtObject.addProperty("name", district.getName());
                districtArray.add(districtObject);
            }

            responseObject.addProperty("status", true);
            responseObject.add("data", districtArray);
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load districts.");
            e.printStackTrace();
        } finally {
            hibernateSession.close();
        }
        return AppUtil.GSON.toJson(responseObject);
    }

    public String getCitiesByDistrict(int districtId) {
        JsonObject responseObject = new JsonObject();
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        try {
            List<City> cityList = hibernateSession.createQuery(
                            "FROM City c WHERE c.district.id = :districtId ORDER BY c.name",
                            City.class)
                    .setParameter("districtId", districtId)
                    .list();

            JsonArray cityArray = new JsonArray();
            for (City city : cityList) {
                JsonObject cityObject = new JsonObject();
                cityObject.addProperty("id", city.getId());
                cityObject.addProperty("name", city.getName());
                cityArray.add(cityObject);
            }

            responseObject.addProperty("status", true);
            responseObject.add("data", cityArray);
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load cities.");
            e.printStackTrace();
        } finally {
            hibernateSession.close();
        }
        return AppUtil.GSON.toJson(responseObject);
    }
}
