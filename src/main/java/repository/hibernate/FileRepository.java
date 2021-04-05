package repository.hibernate;

import model.File;

import java.util.List;

public interface FileRepository extends Repository<File, Long> {
    List<File> getAllFilesByUserId(Long userId);

    File getConcreteFileByUserId(Long fileId, Long userId);

}
