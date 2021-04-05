package service;

import model.File;

import java.util.List;

public interface FileService extends Service<File> {
    List<File> getAllFilesByUserId(Long userId);

    File getConcreteFileByUserId(Long fileId, Long userId);
}
