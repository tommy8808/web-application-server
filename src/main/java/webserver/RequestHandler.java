package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private User user = null;
    Map<String, String> tmpVo = new HashMap<>();
    
    BufferedReader br = null;
    
    DataBase db = new DataBase();

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            DataOutputStream dos = new DataOutputStream(out);
            
            br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            
            String firstHeaderLine = br.readLine();
            if(firstHeaderLine == null){
            	return;
            }
            String url = HttpRequestUtils.separateUrlAndParameters(firstHeaderLine);
            
            if(url.contains("?")){
    			String pureUrl = url.substring(0, url.indexOf("?"));
    			setParamsFromUrl(url);
    			
    			url = pureUrl;
    		}
            //printHeader();
            log.debug("url : {}", url);
            
            dispatcher(dos, url);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

	private void dispatcher(DataOutputStream dos, String url) throws IOException {
		if("/user/create".equals(url)){
			response302Header(dos, url);
			db.addUser(user);
		}else if("/index.html".equals(url) || "/user/form.html".equals(url) || "/user/login.html".equals(url)){
			byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
			response200Header(dos, body.length, "text/html");
			responseBody(dos, body);
		}else if("/user/login".equals(url)){
			User tmpUser = db.findUserById(tmpVo.get("userId"));
			log.info(tmpUser.toString());
			if((tmpUser) != null && tmpUser.getPassword() == tmpVo.get("password")){
				log.debug("로그인 성공.");
			}else {
				log.debug("로그인 실패.");
			}
		}else if(url.startsWith("/css/")){
			byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
			response200Header(dos, body.length, "text/css");
			responseBody(dos, body);
			
		}
	}

	private void printHeader() throws IOException {
		String firstHeaderLine;
		while((firstHeaderLine = br.readLine()) != null){
			if(firstHeaderLine != null){
				log.debug("header : {}", firstHeaderLine);
			}
		}
	}

	private void setParamsFromUrl(String url) {
		StringTokenizer st;
		String params = url.substring(url.indexOf("?")+1);
		
		st = new StringTokenizer(params, "&");
		String[] separetedParams = null;
		String paramName = null;
		String paramValue = null;
		while(st.hasMoreTokens()){
			String tmpParam = st.nextToken();
			separetedParams = tmpParam.split("=");
			paramName = separetedParams[0];
			paramValue = separetedParams[1];
			tmpVo.put(paramName, paramValue);
		}
		
		if(isNotEmptyVo()){
			user = new User(tmpVo.get("userId"), tmpVo.get("password"), tmpVo.get("name"), tmpVo.get("email"));
		}
		
		log.info(user.toString());
	}

	private boolean isNotEmptyVo() {
		return tmpVo.get("userId") != null && tmpVo.get("password") != null && 
				tmpVo.get("name") != null && tmpVo.get("email") != null;
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
    
    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
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
