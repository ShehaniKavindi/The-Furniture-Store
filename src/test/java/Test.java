import lk.thefurniturestore.entity.Address;
import lk.thefurniturestore.entity.Status;
import lk.thefurniturestore.entity.User;
import lk.thefurniturestore.mail.VerificationMail;
import lk.thefurniturestore.provider.MailServiceProvider;
import lk.thefurniturestore.util.AppUtil;
import lk.thefurniturestore.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        System.out.println(sessionFactory);

//        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
//            User user = s.createQuery("FROM user u WHERE u.id=:id", User.class)
//                   .setParameter("id", 1)
//                    .getSingleResult();
//
//
//            Status.Type[] values = Status.Type.values();
//            Transaction transaction = s.beginTransaction();
//            for (Status.Type t : values) {
//                Status status = new Status();
//                status.setValue(t.name());
//                s.persist(status);
//            }
//            transaction.commit();
//        }
    }
}
