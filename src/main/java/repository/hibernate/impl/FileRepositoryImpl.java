package repository.hibernate.impl;

import model.File;
import model.User;
import org.hibernate.Session;
import repository.hibernate.FileRepository;
import util.SessionUtils;

import javax.persistence.Query;
import java.util.List;
import java.util.stream.Collectors;

public class FileRepositoryImpl implements FileRepository {
    @Override
    public File save(File file) {
        Session session = SessionUtils.getSession();
        try {
            session.beginTransaction();
            User user = session.load(User.class, file.getFileUser().getId());
            file.setFileUser(user);
            session.persist(file);
            SessionUtils.commitAndClose(session);
        } catch (Exception e) {
            SessionUtils.commitAndClose(session);
            file = null;
        }
        return file;
    }

    @Override
    public File update(File object) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        session.merge(object);
        SessionUtils.commitAndClose(session);
        return object;
    }

    @Override
    public void delete(Long id) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        File file = session.load(File.class, id);
        session.delete(file);
        SessionUtils.commitAndClose(session);
    }

    @Override
    public File find(Long id) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        File file = session.find(File.class, id);
        SessionUtils.commitAndClose(session);
        return file;
    }

    @Override
    public List<File> findAll() {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        List files = session.createQuery("FROM File ").list();
        SessionUtils.commitAndClose(session);
        return files;
    }

    @Override
    public List<File> getAllFilesByUserId(Long userId) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        Query query = session.createNativeQuery("SELECT f.file_id, f.filename, f.file_type, f.size," +
                              "f.file_status, f.user_id, u.name, u.surname " +
                              "FROM files f INNER JOIN users u on f.user_id=? " +
                              "group by f.file_id;",
                        "getFilesByUserId");
        query.setParameter(1, userId);
        List<Object[]> resultList = query.getResultList();
        List<File> files = resultList.stream().map((arr) -> {
            File f = (File) arr[0];
            User u = (User) arr[1];
            f.setFileUser(u);
            return f;
        }).collect(Collectors.toList());
        SessionUtils.commitAndClose(session);
        return files;
    }

    @Override
    public File getConcreteFileByUserId(Long fileId, Long userId) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        Query query = session.createNativeQuery("SELECT f.file_id, f.filename, f.file_type, f.size," +
                        "f.file_status, f.user_id, u.name, u.surname " +
                        "FROM files f INNER JOIN users u on f.user_id=u.user_id WHERE f.file_id=? and u.user_id=?" +
                        " group by f.file_id;",
                "getFilesByUserId");
        query.setParameter(1, fileId);
        query.setParameter(2, userId);

        List<Object[]> resultList = query.getResultList();
        File file = resultList.stream().map((arr) -> {
            File f = (File) arr[0];
            User u = (User) arr[1];
            f.setFileUser(u);
            return f;
        }).findFirst().orElse(null);
        SessionUtils.commitAndClose(session);
        return file;
    }

}
