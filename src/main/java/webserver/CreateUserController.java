package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class CreateUserController extends AbstractController {
	private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);

	@Override
	public void doPost(HttpRequest request, HttpResponse response) {
		User user = new User(request.getParameter("userId"), request.getParameter("password"), request.getParameter("name"), request.getParameter("email"));
		log.info("try login - user : {}", user.toString());
		DataBase.addUser(user);
		response.sendRedirect("/index.html");
	}

}
