package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    
    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }
    
    HttpRequest request = null;
    HttpResponse response = null;

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	request = new HttpRequest(in);
        	response = new HttpResponse(out);
        	String path = getDefaultPath(request.getPath());
            
            log.debug("path : {}", path);
            dispatcher(path);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

	private String getDefaultPath(String path) {
		if (path.equals("/")){
			return "/index.html";
		}
		return path;
	}

	private void dispatcher(String path) throws IOException {
		if("/user/create".equals(path)){
			User user = new User(request.getParameter("userId"), request.getParameter("password"), request.getParameter("name"), request.getParameter("email"));
			log.info("try login - tmpUser : {}", user.toString());
			DataBase.addUser(user);
			response.sendRedirect("/index.html");
		}else if("/user/login".equals(path)){
			User tmpUser = DataBase.findUserById(request.getParameter("userId"));
			if(tmpUser != null)
			log.info("try login - tmpUser : {}", tmpUser.toString());
			if(tmpUser != null && tmpUser.getPassword().equals(request.getParameter("password"))){
				log.debug("로그인 성공.");
				response.addHeader("Set-Cookie", "logined=true");
				response.sendRedirect("/index.html");
			}else {
				log.debug("로그인 실패.");
				response.sendRedirect("/user/login_failed.html");
				return;
			}
			
		}else if("/user/list.html".equals(path)){
			if(!request.isLogin()){
				log.debug("login을 먼저 해주세요.");
				response.sendRedirect("/user/login.html");
				return;
			}
			Collection<User> users = DataBase.findAll();
			StringBuilder sb = new StringBuilder();
			sb.append("<table border='1'>");
			for(User user : users){
				sb.append("<tr>");
				sb.append("<td>" + user.getUserId() + "</td>");
				sb.append("<td>" + user.getName() + "</td>");
				sb.append("<td>" + user.getEmail() + "</td>");
				sb.append("<tr>");
			}
			sb.append("</table>");
			response.forwardBody(sb.toString());
		} else {
			response.forward(path);
		}
	}

}
