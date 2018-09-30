package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class LoginController extends AbstractController {
	private static final Logger log = LoggerFactory.getLogger(LoginController.class);

	@Override
	public void doPost(HttpRequest request, HttpResponse response) {
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
	}

}
