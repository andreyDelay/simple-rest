package service.impl;

import dto.impl.FileDto;
import model.File;
import repository.hibernate.impl.FileRepositoryImpl;
import service.FileService;

import java.util.List;

public class FileServiceImpl implements FileService {

    private final FileRepositoryImpl repository = new FileRepositoryImpl();
    private final FileDto fileDto = new FileDto();

    @Override
    public File post(File entity) {
        return repository.save(entity);
    }

    @Override
    public File put(File entity) {
        return repository.update(entity);
    }

    @Override
    public void delete(Long id) {
        repository.delete(id);
    }

    @Override
    public File get(Long id) {
        File file = repository.find(id);
        return fileDto.get(file);
    }

    @Override
    public List<File> getAll() {
        List<File> files = repository.findAll();
        return fileDto.getAll(files);
    }

    @Override
    public String getJson(List<File> list) {
        return fileDto.getJson(list);
    }

    @Override
    public List<File> getAllFilesByUserId(Long userId) {
        List<File> files = repository.getAllFilesByUserId(userId);
        return fileDto.getAll(files);
    }

    @Override
    public File getConcreteFileByUserId(Long fileId, Long userId) {
        File file = repository.getConcreteFileByUserId(fileId, userId);
        return fileDto.get(file);
    }
}
