package servlets;

import model.Account;
import model.User;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import service.impl.AccountServiceImpl;
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

@WebServlet(urlPatterns = "/accounts/*")
public class AccountsServlet extends HttpServlet {

    private AccountServiceImpl accountServiceImpl;
    private List resultList;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (ServletUtils.isIdSpecified(req, RequestIdentifierName.ACCOUNT_ID)) {
            resultList.add(accountServiceImpl.get(ServletUtils.getSpecifiedID(req, RequestIdentifierName.ACCOUNT_ID)));
        } else {
            resultList.addAll(accountServiceImpl.getAll());
        }
        ServletUtils.printContentInResponse(resp, resultList, accountServiceImpl);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        String event = "user%20created";
        String body;
        HttpPost post;
        PrintWriter pw = resp.getWriter();

        User createdUser = ServletUtils.createAndSaveNewUser(req);
        if (createdUser != null) {
            body = "Saved successfully!";
        } else {
            body = "Error during saving, check parameters!\n" +
                    "Required parameters to create user:\n" +
                    "1. login\n" +
                    "2. username\n" +
                    "3. surname\n" +
                    "4. age";
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
        resp.setContentType("text/html");
        String event = "account%20updated";
        String body;
        HttpPost post;
        Account updatedAccount = null;
        PrintWriter pw = resp.getWriter();

        String login = req.getParameter("login");
        String status = req.getParameter("status");
        String id = req.getParameter(RequestIdentifierName.ACCOUNT_ID.getKeyName());
        if (id != null) {
            Account account = new Account();
            account.setId(Long.parseLong(id));
            if (status != null) account.setStatus(Account.AccountStatus.valueOf(status));
            if (login != null) account.setAccountName(login);
            updatedAccount = accountServiceImpl.put(account);
        }

        if (updatedAccount != null) {
            body = "Account updated successfully!";
        } else {
            body = "Error during updating account!";
        }

        pw.println("<!DOCTYPE html>");
        pw.println("<html>\n" + "<head><title>Updating report</title></head>" +
                "<body>" + body + "</body>");
        pw.flush();
        pw.close();

        if (updatedAccount != null && (post = ServletUtils.createPost(event, updatedAccount.getUser().getId())) != null) {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            httpClient.execute(post);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter pw = resp.getWriter();
        String body;
        String userId = req.getParameter(RequestIdentifierName.ACCOUNT_ID.getKeyName());
        if (userId != null) {
            Long id = Long.parseLong(userId);
            try {
                accountServiceImpl.delete(id);
                body = "Account was deleted successfully!";
            } catch (Exception e) {
                body = "Error! Cause: " + e.getMessage();
            }
        } else {
            body = "Error! Account id must be specified!";
        }
        pw.println("<!DOCTYPE html>");
        pw.println("<html>\n" + "<head><title>Deleting report</title></head>" +
                "<body>" + body + "</body>");
        pw.flush();
        pw.close();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        accountServiceImpl = new AccountServiceImpl();
        resultList = new ArrayList();
        super.init(config);
    }

}
