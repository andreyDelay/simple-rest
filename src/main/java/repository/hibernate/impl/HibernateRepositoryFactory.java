package repository.hibernate.impl;

import repository.hibernate.*;

public class HibernateRepositoryFactory implements ProjectRepositoryFactory {
    @Override
    public AccountRepository getAccountRepository() {
        return new AccountRepositoryImpl();
    }

    @Override
    public UserRepository getUserRepository() {
        return new UserRepositoryImpl();
    }

    @Override
    public FileRepository getFileRepository() {
        return new FileRepositoryImpl();
    }

    @Override
    public EventRepository getEventRepository() {
        return new EventRepositoryImpl();
    }
}
