package dto.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.Dto;
import model.File;
import model.User;
import org.hibernate.LazyInitializationException;

import java.util.List;
import java.util.stream.Collectors;

public class FileDto implements Dto<File> {

    @Override
    public File get(File file) {
        return transferFile(file);
    }

    @Override
    public List<File> getAll(List<File> list) {
        return list.stream()
                    .map(this::transferFile)
                    .collect(Collectors.toList());
    }

    private File transferFile(File file) {
        if (file == null) {
            return null;
        }
        File transferredFile = new File();
        transferredFile.setFileName(file.getFileName());
        transferredFile.setFileType(file.getFileType());
        transferredFile.setId(file.getId());
        transferredFile.setStatus(file.getStatus());
        transferredFile.setSize(file.getSize());

        try {
            User user = new User();
            user.setId(file.getFileUser().getId());
            user.setName(file.getFileUser().getName());
            user.setSurname(file.getFileUser().getSurname());
            transferredFile.setFileUser(user);
        } catch (LazyInitializationException e) {
            //TODO
        }
        return transferredFile;
    }
}
