package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
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

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	request = new HttpRequest(in);
        	String path = getDefaultPath(request.getPath());
            DataOutputStream dos = new DataOutputStream(out);
            
            log.debug("path : {}", path);
            dispatcher(dos, path);
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

	private void dispatcher(DataOutputStream dos, String path) throws IOException {
		if("/user/create".equals(path)){
			User user = new User(request.getParameter("userId"), request.getParameter("password"), request.getParameter("name"), request.getParameter("email"));
			log.info("try login - tmpUser : {}", user.toString());
			DataBase.addUser(user);
			response302Header(dos, "");
		}else if("/index.html".equals(path) || "/user/form.html".equals(path) || "/user/login.html".equals(path)){
			byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
			response200Header(dos, body.length, "text/html");
			responseBody(dos, body);
		}else if("/user/login".equals(path)){
			User tmpUser = DataBase.findUserById(request.getParameter("userId"));
			if(tmpUser != null)
			log.info("try login - tmpUser : {}", tmpUser.toString());
			if(tmpUser != null && tmpUser.getPassword().equals(request.getParameter("password"))){
				log.debug("로그인 성공.");
				response302Header(dos, request.getHeader("Cookie"));
			}else {
				log.debug("로그인 실패.");
				responseResource(dos, "/user/login_failed.html");
				return;
			}
			
		}else if(path.endsWith(".css")){
			byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
			response200Header(dos, body.length, "text/css");
			responseBody(dos, body);
			
		}else if("/user/list.html".equals(path)){
			if(!request.isLogin()){
				log.debug("login을 먼저 해주세요.");
				response302Header(dos, "");
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
			sb.append("</table");
			byte[] body = sb.toString().getBytes();
			response200Header(dos, body.length, "text/html");
			responseBody(dos, body);
		}
	}

	private void responseResource(DataOutputStream dos, String url) throws IOException {
		byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
		response200Header(dos, body.length, "text/html");
		responseBody(dos, body);
	}

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302Header(DataOutputStream dos, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            if(cookie != null && cookie != ""){
            	dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            }
            dos.writeBytes("Location: " + "/index.html" + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
