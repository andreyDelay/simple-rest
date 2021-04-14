package servlets;

import model.File;
import model.User;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import service.impl.FileServiceImpl;
import service.impl.UserServiceImpl;
import util.ServletEvents;
import util.IdentifierName;
import util.ServletUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@WebServlet(urlPatterns = "/files/*")
public class FilesServlet extends HttpServlet {

    private FileServiceImpl fileServiceImpl;
    private List resultList;
    private final String fileRepositoryPath = "src/main/resources/upload/";
    static final int fileMaxSize = 100 * 1024;
    static final int memMaxSize = 100 * 1024;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (ServletUtils.isIdSpecified(req, IdentifierName.USER_ID)) {
            getResultForSpecifiedParent(req);
        } else if (req.getAttribute("parent") != null) {
            UserServiceImpl userService = new UserServiceImpl();
            if (ServletUtils.isIdSpecified(req, IdentifierName.FILE_ID)) {
                Long fileId = ServletUtils.getSpecifiedID(req, IdentifierName.FILE_ID);
                resultList.addAll(userService.getUsersWithConcreteFile(fileId));
            } else {
                resultList.addAll(userService.getUsersWithFiles());
            }
            ServletUtils.printContentInResponse(resp, resultList, userService);
            return;
        } else {
            resultList.addAll(fileServiceImpl.getAll());
        }
        ServletUtils.printContentInResponse(resp, resultList, fileServiceImpl);
    }

    private void getResultForSpecifiedParent(HttpServletRequest request) {
        Long userId = ServletUtils.getSpecifiedID(request, IdentifierName.USER_ID);
        if (ServletUtils.isIdSpecified(request, IdentifierName.FILE_ID)) {
            Long eventId = ServletUtils.getSpecifiedID(request, IdentifierName.FILE_ID);
            resultList.add(fileServiceImpl.getConcreteFileByUserId(eventId, userId));
        } else {
            resultList.addAll(fileServiceImpl.getAllFilesByUserId(userId));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletEvents event = null;
        String errorText = "";
        File createdFile = null;

        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        diskFileItemFactory.setRepository(new java.io.File(fileRepositoryPath));
        diskFileItemFactory.setSizeThreshold(memMaxSize);
        ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
        upload.setSizeMax(fileMaxSize);

        String userId = req.getParameter(IdentifierName.USER_ID.getKeyName());
        if (ServletUtils.isIdentifierValid(userId)) {
            try {
                List files = upload.parseRequest(req);
                Iterator iterator = files.iterator();

                while (iterator.hasNext()) {
                    FileItem fileItem = (FileItem) iterator.next();
                    if (!fileItem.isFormField()) {
                        String filePath = fileRepositoryPath + fileItem.getName();
                        String filename = FilenameUtils.getBaseName(fileItem.getName());
                        String fileExtension = FilenameUtils.getExtension(fileItem.getName());
                        Long fileSize = fileItem.getSize();

                        File newFile = new File();
                        newFile.setStatus(File.FileStatus.ACTIVE);
                        newFile.setFileName(filename);
                        newFile.setSize(fileSize);
                        newFile.setFileType(fileExtension);
                        newFile.setFilePath(filePath);

                        User user = new User();
                        user.setId(Long.parseLong(userId));
                        newFile.setFileUser(user);
                        createdFile = fileServiceImpl.post(newFile);
                        if (createdFile != null) {
                            event = ServletEvents.FILE_UPLOADED;
                            java.io.File file = new java.io.File(filePath);
                            fileItem.write(file);
                        } else {
                            event = ServletEvents.FILE_UPLOAD_ERROR;
                        }
                    }
                }
            } catch (Exception e) {
                event = ServletEvents.FILE_UPLOAD_ERROR;
                errorText = e.getMessage();
            }
        } else {
            event = ServletEvents.ID_ERROR;
        }

        if (event == null) {
            event = ServletEvents.FILE_UPLOAD_ERROR;
        }

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/html");
        pw.println(ServletUtils.getResponseContent(event.toString() + errorText));
        pw.flush();
        pw.close();

        if (event.equals(ServletEvents.FILE_UPLOADED)) {
            postEvent(createdFile, req, event);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fileId = req.getParameter(IdentifierName.FILE_ID.getKeyName());
        String userId = req.getParameter(IdentifierName.USER_ID.getKeyName());
        ServletEvents event;
        File updatedFile = null;

        if (ServletUtils.isChildAndUserIdValid(fileId, userId)) {
            updatedFile = updateFile(req, fileId, userId);
            event = updatedFile == null ? ServletEvents.FILE_UPDATE_ERROR : ServletEvents.FILE_UPDATED;
        } else {
            event = ServletEvents.ID_ERROR;
        }

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/html");
        pw.println(ServletUtils.getResponseContent(event.toString()));
        pw.flush();
        pw.close();

        if (event.equals(ServletEvents.FILE_UPDATED)) {
            postEvent(updatedFile, req, event);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletEvents event;
        String fileId = req.getParameter(IdentifierName.FILE_ID.getKeyName());
        String userId = req.getParameter(IdentifierName.USER_ID.getKeyName());
        String errorText = "";
        File updatedFile = null;

        if (ServletUtils.isChildAndUserIdValid(fileId, userId)) {
            Long fId = Long.parseLong(fileId);
            Long uId = Long.parseLong(userId);
            try {
                File file = fileServiceImpl.get(fId);
                file.setStatus(File.FileStatus.DELETED);

                User user = new User();
                user.setId(uId);
                file.setFileUser(user);
                updatedFile = fileServiceImpl.put(file);
                if (isFileExistsInRepo(file)) {
                    deleteFileFromRepo(file);
                    event = ServletEvents.FILE_DELETED;
                } else {
                    event = ServletEvents.FILE_NOT_FOUND;
                }
            } catch (Exception e) {
                //TODO write error to a log
                event = ServletEvents.FILE_DELETE_ERROR;
                errorText = "Error! Cause: " + e.getMessage();
            }
        } else {
            event = ServletEvents.ID_ERROR;
        }

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/html");
        pw.println(ServletUtils.getResponseContent(event.toString() + errorText));
        pw.flush();
        pw.close();

        if (event.equals(ServletEvents.FILE_DELETED)) {
            postEvent(updatedFile, req, event);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        fileServiceImpl = new FileServiceImpl();
        resultList = new ArrayList<>();
        super.init(config);
    }

    private File updateFile(HttpServletRequest request, String fileId, String userId) {
        File updatableFile = fileServiceImpl.get(Long.parseLong(fileId));
        String newFileName = request.getParameter("filename");
        try {
            renameFileIfExists(updatableFile, newFileName);
        } catch (IOException e) {
            //TODO write an error to some log to be able to fix later
        }
        String fileStatus = request.getParameter("status");
        establishNewStatus(fileStatus, updatableFile);

        User user = new UserServiceImpl().get(Long.parseLong(userId));
        updatableFile.setFileUser(user);
        return fileServiceImpl.put(updatableFile);
    }

    private void renameFileIfExists(File oldFile, String newFileName) throws IOException {
        String oldFileName = oldFile.getFileName();
        if (newFileName !=null && !oldFileName.equals(newFileName)) {

            if (isFileExistsInRepo(oldFile)) {
                String fileType = "." + oldFile.getFileType();
                Path repo = Paths.get(fileRepositoryPath + oldFileName + fileType);
                Files.move(repo, repo.resolveSibling(newFileName + fileType));
                oldFile.setFilePath(fileRepositoryPath + newFileName + fileType);
                oldFile.setFileName(newFileName);
            }

        }
    }

    private boolean isFileExistsInRepo(File oldFile) {
        boolean result;
        try {
            result = Files.list(Path.of(fileRepositoryPath))
                    .anyMatch(path -> path.getFileName().equals(oldFile.getFileName() + "." + oldFile.getFileType()));
        } catch (IOException e) {
            result = false;
            //TODO print somewhere about something went wrong
        }
        return result;
    }

    private void establishNewStatus(String newStatus, File oldValue) {
        boolean isNewStatusValid =
                Arrays.stream(File.FileStatus.values())
                .map(File.FileStatus::getStatusValue)
                .anyMatch(status -> status.equals(newStatus));
        if (isNewStatusValid) {
            oldValue.setStatus(File.FileStatus.valueOf(newStatus));
        }
    }

    private void postEvent(File updatedFile, HttpServletRequest request,
                                             ServletEvents event) throws IOException {
        if (updatedFile != null) {
            String postEvent = event.toString();
            HttpPost post = ServletUtils.createPost(request, postEvent, updatedFile.getFileUser().getId());
            if (post != null) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                httpClient.execute(post);
            }
        }
    }

    private void deleteFileFromRepo(File file) {
        String filePath = file.getFilePath();
        try {
            Files.delete(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
