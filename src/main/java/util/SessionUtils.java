package util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SessionUtils {
    private final static SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();

    public static Session getSession() {
        return sessionFactory.openSession();
    }

    public static void commitAndClose(Session session) {
        if (session.isOpen()) {
            session.getTransaction().commit();
            session.close();
        }
    }

}
