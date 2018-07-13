package ve.auros.trelloproject.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

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
 * Servlet implementation class CardsServlet
 */
@WebServlet("/Main/Data/CardsServlet/*")
@MultipartConfig
public class CardsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CardsServlet() {
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
		
		
		System.out.println("Column ID for cards: "+request.getPathInfo().substring(1, request.getPathInfo().length()));
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		if (dbc.execute(pr.getValue("getcards"))) {
			
			System.out.println("Cards requested.");
			System.out.println(dbc.getTable());
			json.put("status", 200)
				.put("msg", "Cards returned successfully.")
				.put("cards", dbc.getTable());
			out.print(json.toString());
			System.out.println("Columns sent.");
		}
		else {
			json.put("status", 500)
				.put("msg", "Could not return cards.");
			out.print(json.toString());
			System.out.println("Couldn't send cards.");
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
		DBConnection dbc;
		PropertiesReader pr = PropertiesReader.getInstance();
		ArrayList<Object> myVars = new ArrayList<Object>();
		HttpSession session = request.getSession();
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		
		myVars.add(Integer.parseInt(request.getParameter("column_id")));
		myVars.add(Integer.parseInt((String) session.getAttribute("user_id")));
		myVars.add(request.getParameter("card_name"));
		myVars.add(request.getParameter("card_description"));
		
		System.out.println(myVars);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		if (dbc.execute(pr.getValue("createcard"), myVars.toArray())) {
			json.put("status", 200)
				.put("msg", "Card created successfully")
				.put("redirect", "/TrelloProject/Main/");
			out.print(json.toString());
				
			
		}
		else {
			json.put("status",  500)
				.put("msg", "Error creating a new card");
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
		
		int card_id = Integer.parseInt(request.getPathInfo().substring(1, request.getPathInfo().length()));
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		System.out.println("Card to be deleted, ID: "+card_id);
		if (request.getPathInfo() == null) {
			json.put("status", 406)
				.put("msg", "No card_id was supplied with the request");
			out.print(json.toString());
		}
		else {
			if (dbc.execute(pr.getValue("deletecard"), card_id)) {
				json.put("status", 200)
					.put("msg", "Card deleted succesfully")
					.put("redirect", "/TrelloProject/Main/");
				out.print(json.toString());
			}
			else {
				json.put("status", 500)
					.put("msg", "Error deleting card")
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
		int card_id = Integer.parseInt((String) paramArray[0]);
		String card_name = (String) paramArray[1];
		String card_description = (String) paramArray[2];
		card_name = card_name.replace("%20", " ");
		card_description = card_description.replace("%20", " ");
		
		System.out.println("Params are "+card_id+" and "+card_name+" and "+card_description);
		
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
			if (dbc.execute(pr.getValue("editcard"), card_name, card_description, card_id)) {
				json.put("status", 200)
					.put("msg", "Card edited succesfully")
					.put("redirect", "/TrelloProject/Main/");
				out.print(json.toString());
				
			} 
			else {
				json.put("status", 500)
					.put("msg", "Error editing card")
					/*.put("redirect", "/TrelloProject/Main/")*/; //Maybe I shouldn't do this...
				out.print(json.toString());
			}
		}
		
	}
	
		

}
