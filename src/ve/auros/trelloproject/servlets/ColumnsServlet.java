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

import org.json.JSONObject;

import ve.auros.trelloproject.utilities.DBConnection;
import ve.auros.trelloproject.utilities.PropertiesReader;

/**
 * Servlet implementation class ColumnsServlet
 */
@WebServlet("/Main/Data/ColumnsServlet/*")
@MultipartConfig
public class ColumnsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ColumnsServlet() {
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
		
		String board_id = request.getPathInfo().substring(1, request.getPathInfo().length());
		System.out.println("Board ID for columns: "+request.getPathInfo().substring(1, request.getPathInfo().length()));
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		if (dbc.execute(pr.getValue("getcolumns"), Integer.parseInt(board_id))) {
			
			System.out.println("Columns requested.");
			System.out.println(dbc.getTable());
			json.put("status", 200)
				.put("msg", "Columns returned successfully.")
				.put("columns", dbc.getTable());
			out.print(json.toString());
			System.out.println("Columns sent.");
		}
		else {
			json.put("status", 500)
				.put("msg", "Could not return columns.")
				.put("columns", dbc.getTable());
			out.print(json.toString());
			System.out.println("Couldn't send columns.");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;
		PrintWriter out = response.getWriter();
		JSONObject json = new JSONObject();
		//Enumeration<String> parameters = request.getParameterNames();
		//String param = null;
		DBConnection dbc;
		PropertiesReader pr = PropertiesReader.getInstance();
		ArrayList<Object> myVars = new ArrayList<Object>();
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		/*while (parameters.hasMoreElements()) {
			param = (String) parameters.nextElement();
			System.out.println(request.getParameterValues(param)[0]);
			System.out.println(param);
			myVars.add(request.getParameterValues(param)[0]);
		}*/
		myVars.add(Integer.parseInt(request.getParameter("board_id")));
		myVars.add(request.getParameter("column_name"));
		
		System.out.println(myVars);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		if (dbc.execute(pr.getValue("createcolumn"), myVars.toArray())) {
			json.put("status", 200)
				.put("msg", "Column created successfully")
				.put("redirect", "/TrelloProject/Main/");
			out.print(json.toString());
				
			
		}
		else {
			json.put("status",  500)
				.put("msg", "Error creating a new column");
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
		
		int column_id = Integer.parseInt(request.getPathInfo().substring(1, request.getPathInfo().length()));
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		System.out.println("Column to be deleted, ID: "+column_id);
		if (request.getPathInfo() == null) {
			json.put("status", 406)
				.put("msg", "No column_id was supplied with the request");
			out.print(json.toString());
		}
		else {
			if (dbc.execute(pr.getValue("deletecolumn"), column_id)) {
				json.put("status", 200)
					.put("msg", "Column deleted succesfully")
					.put("redirect", "/TrelloProject/Main/");
				out.print(json.toString());
			}
			else {
				json.put("status", 500)
					.put("msg", "Error deleting column")
					.put("redirect", "/TrelloProject/Main/");
				out.print(json.toString());
			}
				
		}
		
		
	}
	
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
		int column_id = Integer.parseInt((String) paramArray[0]);
		String column_name = (String) paramArray[1];
		column_name = column_name.replace("%20", " ");
		
		System.out.println("Params are "+column_id+" and "+column_name);
		
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
			if (dbc.execute(pr.getValue("editcolumn"), column_name, column_id)) {
				json.put("status", 200)
					.put("msg", "Column edited succesfully")
					.put("redirect", "/TrelloProject/Main/");
				out.print(json.toString());
				
			} 
			else {
				json.put("status", 500)
					.put("msg", "Error editing column")
					/*.put("redirect", "/TrelloProject/Main/")*/; //Maybe I shouldn't do this...
				out.print(json.toString());
			}
		}
		
	}

}
