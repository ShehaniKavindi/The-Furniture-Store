package lk.thefurniturestore.util;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AvailableSettings;

public class HibernateUtil {
    private static final SessionFactory sessionFactory;
    static {
        try{
            Configuration configuration = new Configuration().configure();
            configuration.setProperty(AvailableSettings.JAKARTA_JDBC_DRIVER, "com.mysql.cj.jdbc.Driver");
            configuration.setProperty(AvailableSettings.JAKARTA_JDBC_URL, Env.require("db.url"));
            configuration.setProperty(AvailableSettings.JAKARTA_JDBC_USER, Env.require("db.username"));
            configuration.setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, Env.require("db.password"));
            configuration.setProperty(AvailableSettings.HBM2DDL_AUTO, Env.get("hibernate.ddl.auto"));
            sessionFactory = configuration.buildSessionFactory();
        }catch(HibernateException e){
            throw new  ExceptionInInitializerError("Session creation failed: " +  e.getMessage());
        }
    }
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    public static void shutdown(){
        sessionFactory.close();
    }
}
