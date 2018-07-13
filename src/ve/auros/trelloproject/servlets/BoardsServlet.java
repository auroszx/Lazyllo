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
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import ve.auros.trelloproject.utilities.DBConnection;
import ve.auros.trelloproject.utilities.PropertiesReader;

/**
 * Servlet implementation class BoardsServlet
 */
@WebServlet("/BoardsServlet/*")
@MultipartConfig
public class BoardsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BoardsServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		DBConnection dbc;
		PropertiesReader pr = PropertiesReader.getInstance();
		JSONObject json = new JSONObject();
		
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		if (dbc.execute(pr.getValue("getboards"))) {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			System.out.println("Boards requested.");
			System.out.println(dbc.getTable());
			json.put("status", 200)
				.put("msg", "Boards returned successfully.")
				.put("boards", dbc.getTable());
			out.print(json.toString());
			System.out.println("Boards sent.");
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;
		PrintWriter out = response.getWriter();
		LocalDateTime ts = LocalDateTime.now();
		HttpSession session = request.getSession(false);
		JSONObject json = new JSONObject();
		Enumeration<String> parameters = request.getParameterNames();
		String param = null;
		DBConnection dbc;
		PropertiesReader pr = PropertiesReader.getInstance();
		ArrayList<Object> myVars = new ArrayList<Object>();
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		while (parameters.hasMoreElements()) {
			param = (String) parameters.nextElement();
			System.out.println(request.getParameterValues(param)[0]);
			System.out.println(param);
			myVars.add(request.getParameterValues(param)[0]);
		}
		System.out.println(session.getAttribute("user_id"));
		myVars.add(Integer.parseInt((String) session.getAttribute("user_id")));
		myVars.add(ts);
		
		System.out.println(myVars);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		if (dbc.execute(pr.getValue("createboard"), myVars.toArray())) {
			myVars.remove(1);
			if (dbc.execute(pr.getValue("getboardid"), myVars.toArray())) {
				myVars = new ArrayList<Object>();
				myVars.add(dbc.getTable().getJSONObject(0).getInt("board_id"));
				myVars.add(Integer.parseInt((String) session.getAttribute("user_id")));
				myVars.add(Integer.parseInt((String) session.getAttribute("type_id")));
				if (dbc.execute(pr.getValue("setboardperm"), myVars.toArray())) { 
					json.put("status", 200)
						.put("msg", "Board created successfully")
						/*.put("redirect", "/TrelloProject/Main/")*/;
					out.print(json.toString());
				}
				//Deletion should happen in case of failure?
				else {
					json.put("status", 500)
						.put("msg", "Error setting board permissions")
						/*.put("redirect", "/TrelloProject/Main/")*/;
					out.print(json.toString());
				}
			}
			else {
				//Deletion should happen in case of failure?
				json.put("status", 500)
					.put("msg", "Error getting board info for permissions")
					/*.put("redirect", "/TrelloProject/Main/")*/;
				out.print(json.toString());
			}
			
		}
		else {
			json.put("status",  500)
				.put("msg", "Error creating a new board");
			out.print(json.toString());
		}
		
		
		
	}
	
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;
		PrintWriter out = response.getWriter();
		JSONObject json = new JSONObject();
		DBConnection dbc;
		PropertiesReader pr = PropertiesReader.getInstance();
		
		int board_id = Integer.parseInt(request.getPathInfo().substring(1, request.getPathInfo().length()));
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		System.out.println("Board to be deleted, ID: "+board_id);
		if (request.getPathInfo() == null) {
			json.put("status", 406)
				.put("msg", "No board_id was supplied with the request");
			out.print(json.toString());
		}
		else {
			if (dbc.execute(pr.getValue("deleteuserboard"), board_id)) {
				if (dbc.execute(pr.getValue("deleteboard"), board_id)) {
					json.put("status", 200)
						.put("msg", "Board deleted succesfully")
						/*.put("redirect", "/TrelloProject/Main/")*/;
					out.print(json.toString());
				}
				else {
					json.put("status", 500)
						.put("msg", "Error deleting board")
						/*.put("redirect", "/TrelloProject/Main/")*/;
					out.print(json.toString());
				}
				
			} 
			else {
				json.put("status", 500)
					.put("msg", "Error deleting board permissions")
					/*.put("redirect", "/TrelloProject/Main/")*/;
				out.print(json.toString());
			}
		}
		
		
	}
	//Needs work.
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;
		PrintWriter out = response.getWriter();
		JSONObject json = new JSONObject();
		DBConnection dbc;
		PropertiesReader pr = PropertiesReader.getInstance();
		ArrayList<String> parameters = new ArrayList<String>();
		
		//Parsing a query the stupid way.
		String[] queriesFromString = request.getQueryString().split("&");
		for (String params: queriesFromString) {
			parameters.add(params.split("=")[1]);
		}
		Object[] paramArray = parameters.toArray();
		int board_id = Integer.parseInt((String) paramArray[0]);
		String board_name = (String) paramArray[1];
		board_name = board_name.replace("%20", " ");
		
		System.out.println("Params are "+board_id+" and "+board_name);
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		if (request.getQueryString() == null) {
			json.put("status", 406)
				.put("msg", "No parameters were supplied with the request");
			out.print(json.toString());
		}
		else {
			if (dbc.execute(pr.getValue("editboard"), board_name, board_id)) {
				json.put("status", 200)
					.put("msg", "Board edited succesfully")
					/*.put("redirect", "/TrelloProject/Main/")*/;
				out.print(json.toString());
				
			} 
			else {
				json.put("status", 500)
					.put("msg", "Error editing board")
					/*.put("redirect", "/TrelloProject/Main/")*/;
				out.print(json.toString());
			}
		}
		
	}
}
