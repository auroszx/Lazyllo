package ve.auros.trelloproject.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import ve.auros.trelloproject.utilities.DBConnection;
import ve.auros.trelloproject.utilities.MD5Hasher;
import ve.auros.trelloproject.utilities.PropertiesReader;

/**
 * Servlet implementation class SessionServlet
 */
@WebServlet("/LoginServlet")
@MultipartConfig
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		PrintWriter out = response.getWriter();
		String user = request.getParameterValues("user_username")[0];
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Enumeration<String> parameters = request.getParameterNames();
		String param = null;
		DBConnection dbc;
		PropertiesReader pr = PropertiesReader.getInstance();
		ArrayList<Object> myVars = new ArrayList<Object>();
		MD5Hasher mh = null;
		JSONObject json = new JSONObject();
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		while (parameters.hasMoreElements()) {
			param = (String) parameters.nextElement();
			System.out.println(request.getParameterValues(param)[0]);
			System.out.println(param);
			if (param.equals("user_password")) {
				try {
					System.out.println("Se viene el MD5.");
					mh = MD5Hasher.getInstance();
					String pass = mh.hashString(request.getParameterValues(param)[0]);
					System.out.println("Login pass: "+pass);
					myVars.add(pass);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (param.equals("user_username")) {
				myVars.add(request.getParameterValues("user_username")[0]);
			}
		}
		
		System.out.println("Signing in...");
		if (dbc.execute(pr.getValue("checklogin"), myVars.toArray())) {
			if (dbc.getTable().length() >= 1) {
				System.out.println(dbc.getTable().toString());
				if(session.isNew()) {
					json.put("status", 200)
						.put("msg", "Session saved.")
						.put("redirect", "/TrelloProject/Main");
					setValue("user", user, session);
					System.out.println("Session user id set: "+dbc.getTable().getJSONObject(0).getInt("user_id"));
					String user_id = ""+dbc.getTable().getJSONObject(0).getInt("user_id");
					String type_id = ""+dbc.getTable().getJSONObject(0).getInt("type_id");
					setValue("user_id", user_id, session);
					setValue("type_id", type_id, session);
				}
				else {
					json.put("status", 200)
						.put("msg", "Session changed.")
						.put("redirect", "/TrelloProject/Main");
					setValue("user", user, session);
					System.out.println("Session user id set: "+dbc.getTable().getJSONObject(0).getInt("user_id"));
					String user_id = ""+dbc.getTable().getJSONObject(0).getInt("user_id");
					String type_id = ""+dbc.getTable().getJSONObject(0).getInt("type_id");
					setValue("user_id", user_id, session);
					setValue("type_id", type_id, session);
				}
			 }
			else {
				System.out.println("Bad username/password combination.");
				json.put("status", 406)
					.put("msg", "Bad username/password combination.");
			}
			
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		out.print(json.toString());
		
	}
	
	private void setValue(String att, String value, HttpSession session) {
		if(value == null) {
			session.setAttribute(att, "");
		}
		else {
			session.setAttribute(att, value);
		}
	}

}
