package servlets;

import model.User;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import service.impl.UserServiceImpl;
import util.RequestIdentifierName;
import util.ServletUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = "/users/*")
public class UsersServlet extends HttpServlet {

    private UserServiceImpl userServiceImpl;
    private List resultList;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (ServletUtils.isUrlMatchToAnySubRequest(req.getRequestURI())) {
            req.setAttribute("parent","yes");
            goForward(req, resp);
            return;
        }

        if (ServletUtils.isIdSpecified(req, RequestIdentifierName.USER_ID)) {
            resultList.add(userServiceImpl.get(ServletUtils.getSpecifiedID(req, RequestIdentifierName.USER_ID)));
        } else {
            resultList.addAll(userServiceImpl.getAll());
        }
        ServletUtils.printContentInResponse(resp, resultList, userServiceImpl);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (ServletUtils.isUrlMatchToAnySubRequest(req.getRequestURI())) {
            goForward(req, resp);
            return;
        }
        resp.setContentType("text/html");
        String event = "user%20created";
        String body;
        HttpPost post;
        PrintWriter pw = resp.getWriter();

        User createdUser = ServletUtils.createAndSaveNewUser(req);
        if (createdUser != null) {
            body = "Saved successfully!";
        } else {
            body = "Error during saving, check parameters!";
        }

        pw.println("<!DOCTYPE html>");
        pw.println("<html>\n" + "<head><title>Persistence report</title></head>" +
                "<body>" + body + "</body>");
        pw.flush();
        pw.close();

        if (createdUser != null && (post = ServletUtils.createPost(event, createdUser.getId())) !=  null) {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            httpClient.execute(post);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (ServletUtils.isUrlMatchToAnySubRequest(req.getRequestURI())) {
            goForward(req, resp);
            return;
        }
        resp.setContentType("text/html");
        String event = "user%20personal%20info%20updated";
        String body;
        HttpPost post;
        User updatedUser = null;
        PrintWriter pw = resp.getWriter();

        String username = req.getParameter("username");
        String surname = req.getParameter("surname");
        String age = req.getParameter("age");
        String id = req.getParameter(RequestIdentifierName.USER_ID.getKeyName());
        if (id != null) {
            User user = new User();
            user.setId(Long.parseLong(id));
            if (username != null) user.setName(username);
            if (surname != null) user.setSurname(surname);
            if (age != null) user.setAge(Integer.parseInt(age));
            updatedUser = userServiceImpl.put(user);

            if (updatedUser != null) {
                body = "User updated successfully!";
            } else {
                body = "Error during updating user!";
            }
        } else {
            body = "Error! User id must be specified!";
        }

        pw.println("<!DOCTYPE html>");
        pw.println("<html>\n" + "<head><title>Updating report</title></head>" +
                "<body>" + body + "</body>");
        pw.flush();
        pw.close();

        if (updatedUser != null && (post = ServletUtils.createPost(event, updatedUser.getId())) != null) {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            httpClient.execute(post);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (ServletUtils.isUrlMatchToAnySubRequest(req.getRequestURI())) {
            goForward(req, resp);
            return;
        }
        resp.setContentType("text/html");
        PrintWriter pw = resp.getWriter();
        String body;
        String userId = req.getParameter(RequestIdentifierName.USER_ID.getKeyName());
        if (userId != null) {
            Long id = Long.parseLong(userId);
            try {
                userServiceImpl.delete(id);
                body = "User was deleted successfully!";
            } catch (Exception e) {
                body = "Error! Cause: " + e.getMessage();
            }
        } else {
            body = "Error! User id must be specified!";
        }
        pw.println("<!DOCTYPE html>");
        pw.println("<html>\n" + "<head><title>Deleting report</title></head>" +
                "<body>" + body + "</body>");
        pw.flush();
        pw.close();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        userServiceImpl = new UserServiceImpl();
        resultList = new ArrayList<>();
        super.init(config);
    }

    private void goForward(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = ServletUtils.getSubRequestUrl(req, RequestIdentifierName.USER_ID);
        req.setAttribute("parent","yes");
        req.getRequestDispatcher(url).forward(req, resp);
    }

}
