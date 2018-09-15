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

import model.User;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private User user = null;
    Map<String, String> tmpVo = new HashMap<>();
    
    BufferedReader br = null;

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
            String url = separateUrlAndParameters(firstHeaderLine);
            
           /* while((line = br.readLine()) != null){
            	if(line != null){
            		System.out.println(line);
            	}
            }*/
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String separateUrlAndParameters(String line) {
		String url = "";
		StringTokenizer st = new StringTokenizer(line, " ");
		while(st.hasMoreTokens()){
			if((url = st.nextToken()).startsWith("/")){
				break;
			}
		}
		if(url.contains("?")){
			String pureUrl = url.substring(0, url.indexOf("?"));
			setParamsFromUrl(url);
			
			
			url = pureUrl;
		}
		return url;
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

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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
