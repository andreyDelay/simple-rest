package servlets;

import model.File;
import model.User;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import service.impl.FileServiceImpl;
import service.impl.UserServiceImpl;
import util.RequestIdentifierName;
import util.ServletUtils;

import javax.persistence.PersistenceException;
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
import java.util.Iterator;
import java.util.List;

@WebServlet(urlPatterns = "/files/*")
public class FilesServlet extends HttpServlet {

    private FileServiceImpl fileServiceImpl;
    private List resultList;
    private final String fileRepositoryPath = "src/main/resources/upload/";
    static final int fileMaxSize = 1000 * 1024;
    static final int memMaxSize = 1000 * 1024;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (ServletUtils.isIdSpecified(req, RequestIdentifierName.USER_ID)) {
            getResultForSpecifiedParent(req);
        } else if (req.getAttribute("parent") != null) {
            UserServiceImpl userService = new UserServiceImpl();
            if (ServletUtils.isIdSpecified(req, RequestIdentifierName.FILE_ID)) {
                Long fileId = ServletUtils.getSpecifiedID(req, RequestIdentifierName.FILE_ID);
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
        Long userId = ServletUtils.getSpecifiedID(request, RequestIdentifierName.USER_ID);
        if (ServletUtils.isIdSpecified(request, RequestIdentifierName.FILE_ID)) {
            Long eventId = ServletUtils.getSpecifiedID(request, RequestIdentifierName.FILE_ID);
            resultList.add(fileServiceImpl.getConcreteFileByUserId(eventId, userId));
        } else {
            resultList.addAll(fileServiceImpl.getAllFilesByUserId(userId));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = req.getParameter(RequestIdentifierName.USER_ID.getKeyName());
        String event = "";
        HttpPost post;
        File createdFile = null;
        resp.setContentType("text/html");
        String body = "";
        PrintWriter pw = resp.getWriter();

        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        diskFileItemFactory.setRepository(new java.io.File(fileRepositoryPath));
        diskFileItemFactory.setSizeThreshold(memMaxSize);

        ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
        upload.setSizeMax(fileMaxSize);
        if (userId != null) {
            try {
                List files = upload.parseRequest(req);
                Iterator iterator = files.iterator();

                while (iterator.hasNext()) {
                    FileItem fileItem = (FileItem) iterator.next();
                    if (!fileItem.isFormField()) {
                        String filename = fileItem.getName().substring(0, fileItem.getName().indexOf("."));
                        Long size = fileItem.getSize();
                        String extension = FilenameUtils.getExtension(fileItem.getName());
                        File newFile = new File();

                        newFile.setStatus(File.FileStatus.ACTIVE);
                        newFile.setFileName(filename);
                        newFile.setSize(size);
                        newFile.setFileType(extension);
                        User user = new User();
                        user.setId(Long.parseLong(userId));
                        newFile.setFileUser(user);

                        createdFile = fileServiceImpl.post(newFile);
                        if (createdFile != null) {
                            body = "File successfully uploaded!";
                            event = "file%20" + fileItem.getName()
                                                .replaceAll(" ", "%20") +
                                                "%20was%20uploaded%20to%20server";
                            java.io.File file =
                                    new java.io.File(fileRepositoryPath +
                                            fileItem.getName());
                            fileItem.write(file);
                        } else {
                            body = "Error. File wasn't uploaded!";
                        }
                    }
                }
            } catch (FileUploadException e) {
                body = "Couldn't save file on server!";
            } catch (PersistenceException e) {
                body = "Error during uploading file, probably mentioned user id doesn't exist! " +
                        "Reason: " + e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            body = "File wasn't uploaded! User id is not specified!";
        }


        pw.println("<!DOCTYPE html>");
        pw.println("<html>\n" + "<head><title>Persistence report</title></head>" +
                "<body>" + body + "</body>");
        pw.flush();
        pw.close();

        if (createdFile != null && (post = ServletUtils.createPost(req, event, createdFile.getFileUser().getId())) !=  null) {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            httpClient.execute(post);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fileId = req.getParameter(RequestIdentifierName.FILE_ID.getKeyName());
        String userId = req.getParameter(RequestIdentifierName.USER_ID.getKeyName());
        HttpPost post;
        File updatedFile = null;
        resp.setContentType("text/html");
        String body = "";
        PrintWriter pw = resp.getWriter();

        if (fileId != null && userId != null) {
            File oldValue = fileServiceImpl.get(Long.parseLong(fileId));
            String newFileName = req.getParameter("filename");
            String fileStatus = req.getParameter("filestatus");
            if (newFileName != null) {
                String fileType = "." + oldValue.getFileType();
                String oldFileName = oldValue.getFileName();
                renameFile(oldFileName, newFileName, fileType);
                oldValue.setFileName(newFileName);
            }
            if (fileStatus != null) oldValue.setStatus(File.FileStatus.valueOf(fileStatus));
            User user = new UserServiceImpl().get(Long.parseLong(userId));
            oldValue.setFileUser(user);
            updatedFile = fileServiceImpl.put(oldValue);
            if (updatedFile != null) body = "file was successfully updated!";
        } else {
            body = "Error! File id and user id must be specified!";
        }

        pw.println("<!DOCTYPE html>");
        pw.println("<html>\n" + "<head><title>Updating report</title></head>" +
                "<body>" + body + "</body>");
        pw.flush();
        pw.close();

        if (updatedFile != null) {
            String event = "file%20" + updatedFile.getFileName()
                    .replaceAll(" ", "%20") + "."
                    + updatedFile.getFileType()
                    + "%20was%20updated!";
            post = ServletUtils.createPost(req, event, updatedFile.getFileUser().getId());
            if (post != null) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                httpClient.execute(post);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        String body;
        HttpPost post;
        String event = "file%20was%20deleted!";
        PrintWriter pw = resp.getWriter();
        String fileId = req.getParameter(RequestIdentifierName.FILE_ID.getKeyName());
        String strUserId = req.getParameter(RequestIdentifierName.USER_ID.getKeyName());
        Long id = null;
        if (fileId != null) {
            id = Long.parseLong(fileId);
            try {
                File file = fileServiceImpl.get(id);
                file.setStatus(File.FileStatus.DELETED);
                User user = new User();
                user.setId(Long.parseLong(strUserId));
                file.setFileUser(user);
                fileServiceImpl.put(file);
                body = "File was deleted!";
            } catch (Exception e) {
                body = "Error! Cause: " + e.getMessage();
            }
        } else {
            body = "Error! File id must be specified!";
        }
        pw.println("<!DOCTYPE html>");
        pw.println("<html>\n" + "<head><title>Deleting report</title></head>" +
                "<body>" + body + "</body>");
        pw.flush();
        pw.close();

        if (id != null && !body.contains("Error")) {
            Long userId = Long.parseLong(strUserId);
            post = ServletUtils.createPost(req, event, userId);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            httpClient.execute(post);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        fileServiceImpl = new FileServiceImpl();
        resultList = new ArrayList<>();
        super.init(config);
    }

    private void renameFile(String oldFilename, String newFileName, String fileType) throws IOException {
        if (!oldFilename.equals(newFileName)) {
            Path repo = Paths.get(fileRepositoryPath + oldFilename + fileType);
            Files.move(repo, repo.resolveSibling(newFileName + fileType));
        }
    }

}
