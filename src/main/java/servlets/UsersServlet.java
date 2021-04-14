package servlets;

import model.User;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import service.impl.UserServiceImpl;
import util.IdentifierName;
import util.ServletEvents;
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

@WebServlet(urlPatterns = "/users/*", loadOnStartup = 0)
public class UsersServlet extends HttpServlet {

    private UserServiceImpl userServiceImpl;
    private List resultList;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resultList = new ArrayList();
        if (ServletUtils.isUrlMatchToAnySubRequest(req.getRequestURI())) {
            req.setAttribute("parent","yes");
            goForward(req, resp);
            return;
        }

        if (ServletUtils.isIdSpecified(req, IdentifierName.USER_ID)) {
            resultList.add(userServiceImpl.get(ServletUtils.getSpecifiedID(req, IdentifierName.USER_ID)));
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
        ServletEvents event;
        User createdUser = ServletUtils.createAndSaveNewUser(req);
        if (createdUser != null) {
            event = ServletEvents.ACCOUNT_CREATED;
        } else {
            event = ServletEvents.ACCOUNT_USER_CREATION_ERROR;
        }

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/html");
        pw.println(ServletUtils.getResponseContent(event.toString()));
        pw.flush();
        pw.close();

        if (event.equals(ServletEvents.ACCOUNT_CREATED)) {
            postEvent(createdUser, req, event);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (ServletUtils.isUrlMatchToAnySubRequest(req.getRequestURI())) {
            goForward(req, resp);
            return;
        }
        ServletEvents event;
        User updatedUser = null;

        String userId = req.getParameter(IdentifierName.USER_ID.getKeyName());
        if (ServletUtils.isIdentifierValid(userId)) {
            User oldUser = userServiceImpl.get(Long.parseLong(userId));
            if (oldUser != null) {
                updatedUser = updateUser(req, oldUser);
                event = ServletEvents.USER_UPDATED;
            } else {
                event = ServletEvents.ACCOUNT_NOT_FOUND;
            }
        } else {
            event = ServletEvents.ID_ERROR;
        }

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/html");
        pw.println(ServletUtils.getResponseContent(event.toString()));
        pw.flush();
        pw.close();

        if (event.equals(ServletEvents.USER_UPDATED)) {
            postEvent(updatedUser, req, event);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (ServletUtils.isUrlMatchToAnySubRequest(req.getRequestURI())) {
            goForward(req, resp);
            return;
        }
        ServletEvents event;
        String errorText = "";
        String userId = req.getParameter(IdentifierName.USER_ID.getKeyName());
        if (ServletUtils.isIdentifierValid(userId)) {
            try {
                userServiceImpl.delete(Long.parseLong(userId));
                event = ServletEvents.ACCOUNT_DELETED;
            } catch (Exception e) {
                event = ServletEvents.ACCOUNT_DELETE_ERROR;
                errorText = e.getMessage();
            }
        } else {
            event = ServletEvents.ID_ERROR;
        }

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/html");
        pw.println(ServletUtils.getResponseContent(event.toString() + errorText));
        pw.flush();
        pw.close();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        userServiceImpl = new UserServiceImpl();
        super.init(config);
    }

    private void goForward(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = ServletUtils.getSubRequestUrl(req, IdentifierName.USER_ID);
        req.setAttribute("parent","yes");
        req.getRequestDispatcher(url).forward(req, resp);
    }

    private void postEvent(User user, HttpServletRequest request,
                           ServletEvents event) throws IOException {
        if (user != null) {
            String eventText = event.toString();
            HttpPost post = ServletUtils.createPost(request, eventText, user.getId());
            if (post != null) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                httpClient.execute(post);
            }
        }
    }

    private User updateUser(HttpServletRequest request, User user) {
        String username = request.getParameter("username");
        String surname = request.getParameter("surname");
        String age = request.getParameter("age");

        if (username != null) {
            user.setName(username);
        }
        if (surname != null){
            user.setSurname(surname);
        }
        if (age != null) {
            user.setAge(Integer.parseInt(age));
        }
        return user;
    }

}
