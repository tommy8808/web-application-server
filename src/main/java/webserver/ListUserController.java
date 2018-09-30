package webserver;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class ListUserController extends AbstractController {
	private static final Logger log = LoggerFactory.getLogger(LoginController.class);

	@Override
	public void doGet(HttpRequest request, HttpResponse response) {
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
		log.debug(sb.toString());
		response.forwardBody(sb.toString());
	}

}
