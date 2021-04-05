package repository.hibernate;

public interface ProjectRepositoryFactory {
    AccountRepository getAccountRepository();

    UserRepository getUserRepository();

    FileRepository getFileRepository();

    EventRepository getEventRepository();
}
