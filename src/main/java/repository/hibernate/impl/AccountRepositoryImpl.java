package repository.hibernate.impl;

import model.Account;
import org.hibernate.Session;
import repository.hibernate.AccountRepository;
import util.SessionUtils;

import java.util.List;

public class AccountRepositoryImpl implements AccountRepository {
    @Override
    public Account save(Account object) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        session.persist(object);
        SessionUtils.commitAndClose(session);
        return object;
    }

    @Override
    public Account update(Account object) {
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
        Account account = session.load(Account.class, id);
        session.delete(account);
        SessionUtils.commitAndClose(session);
    }

    @Override
    public Account find(Long id) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        Account account = session.find(Account.class, id);
        SessionUtils.commitAndClose(session);
        return account;
    }

    @Override
    public List<Account> findAll() {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        List accounts = session.createQuery("FROM Account ").list();
        SessionUtils.commitAndClose(session);
        return accounts;
    }

}
