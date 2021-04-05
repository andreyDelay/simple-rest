package repository.hibernate.impl;

import model.User;
import org.hibernate.Session;
import repository.hibernate.UserRepository;
import util.SessionUtils;

import java.util.List;

public class UserRepositoryImpl implements UserRepository {
    @Override
    public User save(User object) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        session.persist(object);
        SessionUtils.commitAndClose(session);
        return object;
    }

    @Override
    public User update(User object) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        session.saveOrUpdate(object);
        SessionUtils.commitAndClose(session);
        return object;
    }

    @Override
    public void delete(Long id) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        User user = session.load(User.class, id);
        session.delete(user);
        SessionUtils.commitAndClose(session);
    }

    @Override
    public User find(Long id) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        User user = session.find(User.class, id);
        SessionUtils.commitAndClose(session);
        return user;
    }

    @Override
    public List<User> findAll() {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        List users = session.createQuery("FROM User", User.class).getResultList();
        SessionUtils.commitAndClose(session);
        return users;
    }

    @Override
    public List<User> getUsersWithEvents() {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        List users = session.createQuery("FROM User u LEFT JOIN FETCH u.events").list();
        SessionUtils.commitAndClose(session);
        return users;
    }

    @Override
    public List<User> getUsersWithFiles() {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        List users = session.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.files").list();
        SessionUtils.commitAndClose(session);
        return users;
    }

    @Override
    public List<User> getUsersWithConcreteEvent(Long eventId) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        List users = session.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.events events WHERE events.id =:id")
                .setParameter("id", eventId)
                .list();
        SessionUtils.commitAndClose(session);
        return users;
    }

    @Override
    public List<User> getUsersWithConcreteFile(Long filetId) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        List users = session.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.files files WHERE files.id =:id")
                .setParameter("id", filetId)
                .list();
        SessionUtils.commitAndClose(session);
        return users;
    }
}
