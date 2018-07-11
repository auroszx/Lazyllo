package ve.auros.trelloproject.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import ve.auros.trelloproject.utilities.DBConnection;
import ve.auros.trelloproject.utilities.MD5Hasher;
import ve.auros.trelloproject.utilities.PropertiesReader;

/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet("/RegisterServlet")
@MultipartConfig
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
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

		//doGet(request, response);
		
		LocalDateTime ts = LocalDateTime.now();
		Enumeration<String> parameters = request.getParameterNames();
		String param = null;
		DBConnection dbc;
		PropertiesReader pr = PropertiesReader.getInstance();
		ArrayList<Object> myVars = new ArrayList<Object>();
		MD5Hasher mh = null;
		PrintWriter out = response.getWriter();
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
					myVars.add(pass);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				myVars.add(request.getParameterValues(param)[0]);
			}
		}
		myVars.add(ts);
		
		System.out.println(myVars);
		dbc.execute(pr.getValue("adduser"), myVars.toArray());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		json.put("status", 200)
			.put("msg", "User created successfully")
			/*.put("redirect", "/TrelloProject/Main")*/;
		out.print(json.toString());
		//out.println("Registration successful");
	}

}
