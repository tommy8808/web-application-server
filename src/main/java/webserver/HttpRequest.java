package webserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.HttpMethod;
import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

	private Map<String, String> headers = new HashMap<String, String>();
	private Map<String, String> params = new HashMap<String, String>();
	private RequestLine requestLine;
	
	public HttpRequest(InputStream in) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = br.readLine();
			if(line == null) {
				return;
			}
			
			requestLine = new RequestLine(line);
			
			line = br.readLine();
			while(!line.equals("")) {
				log.debug("header : {}", line);
				String[] tokens = line.split(":");
				headers.put(tokens[0].trim(), tokens[1].trim());
				line = br.readLine();
			}
			
			if(getMethod().isPost()){
				String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
				params = HttpRequestUtils.parseQueryString(body);
			} else {
				params = requestLine.getParams();
			}
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	public String getParameter(String name) {
		return params.get(name);
	}

	public HttpMethod getMethod() {
		return requestLine.getMethod();
	}

	public String getPath() {
		return requestLine.getPath();
	}
	
	public boolean isLogin() {
		getHeader("Cookie");
		String[] tokens = getHeader("Cookie").split("=");
		if(tokens == null){
			return false;
		}else if(tokens[1].equals(true)){
			return Boolean.parseBoolean(tokens[1]);
		}
		return Boolean.parseBoolean(tokens[1]);
	}

}
