package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;

@Entity
@Table(name = "files")
@JsonInclude(JsonInclude.Include.NON_NULL)
@SqlResultSetMapping(name = "getFilesByUserId", entities = {
        @EntityResult(entityClass = File.class, fields = {
                @FieldResult(name = "id", column = "file_id"),
                @FieldResult(name = "fileName", column = "filename"),
                @FieldResult(name = "fileType", column = "file_type"),
                @FieldResult(name = "size", column = "size"),
                @FieldResult(name = "status", column = "file_status"),
                @FieldResult(name = "fileUser", column = "user_id"),
                @FieldResult(name = "filePath", column = "file_path")
        })
},classes = {
        @ConstructorResult(
                targetClass = User.class,
                columns = {
                        @ColumnResult(name = "user_id", type = Long.class),
                        @ColumnResult(name = "name", type = String.class),
                        @ColumnResult(name = "surname", type = String.class),
                })
})
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @Column(name = "filename")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "size")
    private long size;

    @Column(name = "file_path")
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_status")
    private FileStatus status;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User fileUser;

    public File() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public FileStatus getStatus() {
        return status;
    }

    public void setStatus(FileStatus status) {
        this.status = status;
    }

    public User getFileUser() {
        return fileUser;
    }

    public void setFileUser(User fileUser) {
        this.fileUser = fileUser;
    }

    public enum FileStatus {
        ACTIVE("ACTIVE"),
        DELETED("DELETED");

        private String statusValue;

        FileStatus(String statusValue) {
            this.statusValue = statusValue;
        }

        public String getStatusValue() {
            return statusValue;
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
