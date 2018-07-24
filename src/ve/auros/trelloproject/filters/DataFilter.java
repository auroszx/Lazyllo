package ve.auros.trelloproject.filters;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import ve.auros.trelloproject.utilities.DBConnection;
import ve.auros.trelloproject.utilities.PropertiesReader;

/**
 * Servlet Filter implementation class SessionFilter
 */
@WebFilter("/Main/Data/*")
@MultipartConfig
public class DataFilter implements Filter {

    /**
     * Default constructor. 
     */
    public DataFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession(); //Careful here
		JSONObject json = new JSONObject();
		DBConnection dbc;
		PropertiesReader pr = PropertiesReader.getInstance();
		//ArrayList<Object> perms = new ArrayList<Object>();
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
	
		System.out.println("Request URI: "+request.getRequestURI());
		System.out.println("Request Method: "+request.getMethod());
		System.out.println("Session is new? "+session.isNew());
		
		//Guest check is broken sometimes I guess.
		if (session.isNew()) {
			session.invalidate();
			if(!request.getMethod().equalsIgnoreCase("GET")) {
				System.out.println("User is a guest...");
				json.put("status", 403)
					.put("msg", "Guests cannot create/change/delete columns, cards, files, comments or permissions");
				out.print(json.toString());
				
		    }
			else {
				chain.doFilter(req, res);
			}
		}
		else {
			if ((String) session.getAttribute("type_user") == "2") {
				//Board related permissions.
				if(request.getRequestURI().contains("Boards")) {
					if(request.getMethod().equalsIgnoreCase("PUT")) {
						
						ArrayList<String> parameters2 = new ArrayList<String>();
						
						String[] queriesFromString = request.getQueryString().split("&");
						for (String params: queriesFromString) {
							parameters2.add(params.split("=")[1]);
						}
						Object[] paramArray = parameters2.toArray();
						int board_id = Integer.parseInt((String) paramArray[0]);
						int user_id =  Integer.parseInt((String) session.getAttribute("user_id"));
						
						if(dbc.execute(pr.getValue("getboardperm"), board_id, user_id)) {
							JSONArray ja = dbc.getTable();
							if (ja.length() > 0) {
								if (ja.getJSONObject(0).getInt("type_board_user_id") == 1) {
									System.out.println("User can call this method");
									chain.doFilter(req, res);
								}
								else {
									System.out.println("User is not a board master");
									json.put("status", 403)
										.put("msg", "Only board masters for this board can perform this action");
									out.print(json.toString());
								}
							}
							else {
								System.out.println("User is not allowed");
								json.put("status", 403)
									.put("msg", "Your user can't perform this action");
								out.print(json.toString());
							}
						}
						else {
							json.put("status", 500)
								.put("msg", "Could not check permissions");
							out.print(json.toString());
						}
					}
					else if(request.getMethod().equalsIgnoreCase("DELETE") && !request.getRequestURI().contains("deleteperm")) {
						
						int board_id = Integer.parseInt(request.getPathInfo().substring(1, request.getPathInfo().length()));
						int user_id =  Integer.parseInt((String) session.getAttribute("user_id"));
						
						if(dbc.execute(pr.getValue("getboardperm"), board_id, user_id)) {
							JSONArray ja = dbc.getTable();
							if (ja.length() > 0) {
								if (ja.getJSONObject(0).getInt("type_board_user_id") == 1) {
									System.out.println("User can call this method");
									chain.doFilter(req, res);
								}
								else {
									System.out.println("User is not a board master");
									json.put("status", 403)
										.put("msg", "Only board masters for this board can perform this action");
									out.print(json.toString());
								}
							}
							else {
								System.out.println("User is not allowed");
								json.put("status", 403)
									.put("msg", "Your user can't perform this action");
								out.print(json.toString());
							}
						}
						else {
							json.put("status", 500)
								.put("msg", "Could not check permissions");
							out.print(json.toString());
						}
					}
					else if(request.getMethod().equalsIgnoreCase("DELETE") && request.getRequestURI().contains("deleteperm")) {
						
						ArrayList<Object> parameters2 = new ArrayList<Object>();
						
						String[] queriesFromString = request.getQueryString().split("&");
						for (String params: queriesFromString) {
							parameters2.add(params.split("=")[1]);
						}
						Object[] paramArray = parameters2.toArray();
						int board_id = Integer.parseInt((String) paramArray[1]);
						int user_id =  Integer.parseInt((String) session.getAttribute("user_id"));
						
						if(dbc.execute(pr.getValue("getboardperm"), board_id, user_id)) {
							JSONArray ja = dbc.getTable();
							if (ja.length() > 0) {
								if (ja.getJSONObject(0).getInt("type_board_user_id") == 1) {
									System.out.println("User can call this method");
									chain.doFilter(req, res);
								}
								else {
									System.out.println("User is not a board master");
									json.put("status", 403)
										.put("msg", "Only board masters for this board can perform this action");
									out.print(json.toString());
								}
							}
							else {
								System.out.println("User is not allowed");
								json.put("status", 403)
									.put("msg", "Your user can't perform this action");
								out.print(json.toString());
							}
						}
						else {
							json.put("status", 500)
								.put("msg", "Could not check permissions");
							out.print(json.toString());
						}
					}
					else if (request.getMethod().equalsIgnoreCase("POST") && request.getRequestURI().contains("setperm")) {
						int board_id = Integer.parseInt(request.getParameter("board_id"));
						int user_id =  Integer.parseInt((String) session.getAttribute("user_id"));
						
						if(dbc.execute(pr.getValue("getboardperm"), board_id, user_id)) {
							JSONArray ja = dbc.getTable();
							if (ja.length() > 0 && ja.getJSONObject(0).getInt("type_board_user_id") == 1) {
								chain.doFilter(req, res);
							}
							else {
								System.out.println("User is not allowed");
								json.put("status", 403)
									.put("msg", "Your user can't perform this action");
								out.print(json.toString());
							}
						}
						else {
							json.put("status", 500)
								.put("msg", "Could not check permissions");
							out.print(json.toString());
						}
					}
					//In case of GET or POST break the glass.
					else {
						chain.doFilter(req, res);
					}
				}
				//Column related permissions.
				else if(request.getRequestURI().contains("Columns")) {
					if(request.getMethod().equalsIgnoreCase("POST")) {
						int board_id = Integer.parseInt(request.getParameter("board_id"));
						int user_id =  Integer.parseInt((String) session.getAttribute("user_id"));
						
						if(dbc.execute(pr.getValue("getboardperm"), board_id, user_id)) {
							JSONArray ja = dbc.getTable();
							if (ja.length() > 0) {
								chain.doFilter(req, res);
							}
							else {
								System.out.println("User is not allowed");
								json.put("status", 403)
									.put("msg", "Your user can't perform this action");
								out.print(json.toString());
							}
						}
						else {
							json.put("status", 500)
								.put("msg", "Could not check permissions");
							out.print(json.toString());
						}
					}
					else if (request.getMethod().equalsIgnoreCase("PUT")) {
						
						ArrayList<String> parameters2 = new ArrayList<String>();
						
						String[] queriesFromString = request.getQueryString().split("&");
						for (String params: queriesFromString) {
							parameters2.add(params.split("=")[1]);
						}
						Object[] paramArray = parameters2.toArray();
						String column_id = (String) paramArray[0];
						
						dbc.execute(pr.getValue("getboardbycolumnid"), column_id);
						
						int board_id = dbc.getTable().getJSONObject(0).getInt("board_id");
						int user_id =  Integer.parseInt((String) session.getAttribute("user_id"));
						
						if(dbc.execute(pr.getValue("getboardperm"), board_id, user_id)) {
							JSONArray ja = dbc.getTable();
							if (ja.length() > 0) {
								chain.doFilter(req, res);
							}
							else {
								System.out.println("User is not allowed");
								json.put("status", 403)
									.put("msg", "Your user can't perform this action");
								out.print(json.toString());
							}
						}
						else {
							json.put("status", 500)
								.put("msg", "Could not check permissions");
							out.print(json.toString());
						}
					}
					else if (request.getMethod().equalsIgnoreCase("DELETE")) {
						
						int column_id = Integer.parseInt(request.getPathInfo().substring(1, request.getPathInfo().length()));
						
						dbc.execute(pr.getValue("getboardbycolumnid"), column_id);
						
						int board_id = dbc.getTable().getJSONObject(0).getInt("board_id");
						int user_id =  Integer.parseInt((String) session.getAttribute("user_id"));
						
						if(dbc.execute(pr.getValue("getboardperm"), board_id, user_id)) {
							JSONArray ja = dbc.getTable();
							if (ja.length() > 0) {
								chain.doFilter(req, res);
							}
							else {
								System.out.println("User is not allowed");
								json.put("status", 403)
									.put("msg", "Your user can't perform this action");
								out.print(json.toString());
							}
						}
						else {
							json.put("status", 500)
								.put("msg", "Could not check permissions");
							out.print(json.toString());
						}
					}
					//Any other case. Mostly GET.
					else {
						chain.doFilter(req, res);
					}
				}
				//Card related permissions.
				else if(request.getRequestURI().contains("Cards")) {
					if(request.getMethod().equalsIgnoreCase("POST")) {
						int board_id = Integer.parseInt(request.getParameter("board_id"));
						int user_id =  Integer.parseInt((String) session.getAttribute("user_id"));
						
						if(dbc.execute(pr.getValue("getboardperm"), board_id, user_id)) {
							JSONArray ja = dbc.getTable();
							if (ja.length() > 0) {
								chain.doFilter(req, res);
							}
							else {
								System.out.println("User is not allowed");
								json.put("status", 403)
									.put("msg", "Your user can't perform this action");
								out.print(json.toString());
							}
						}
						else {
							json.put("status", 500)
								.put("msg", "Could not check permissions");
							out.print(json.toString());
						}
					}
					else if (request.getMethod().equalsIgnoreCase("PUT")) {
						
						ArrayList<String> parameters2 = new ArrayList<String>();
						
						String[] queriesFromString = request.getQueryString().split("&");
						for (String params: queriesFromString) {
							parameters2.add(params.split("=")[1]);
						}
						Object[] paramArray = parameters2.toArray();
						int card_id = Integer.parseInt((String) paramArray[0]);
						
						dbc.execute(pr.getValue("getboardbycardid"), card_id);
						
						int board_id = dbc.getTable().getJSONObject(0).getInt("board_id");
						int user_id =  Integer.parseInt((String) session.getAttribute("user_id"));
						
						if(dbc.execute(pr.getValue("getboardperm"), board_id, user_id)) {
							JSONArray ja = dbc.getTable();
							if (ja.length() > 0) {
								chain.doFilter(req, res);
							}
							else {
								System.out.println("User is not allowed");
								json.put("status", 403)
									.put("msg", "Your user can't perform this action");
								out.print(json.toString());
							}
						}
						else {
							json.put("status", 500)
								.put("msg", "Could not check permissions");
							out.print(json.toString());
						}
					}
					else if (request.getMethod().equalsIgnoreCase("DELETE")) {
						
						int card_id = Integer.parseInt(request.getPathInfo().substring(1, request.getPathInfo().length()));
						
						dbc.execute(pr.getValue("getboardbycardid"), card_id);
						
						int board_id = dbc.getTable().getJSONObject(0).getInt("board_id");
						int user_id =  Integer.parseInt((String) session.getAttribute("user_id"));
						
						if(dbc.execute(pr.getValue("getboardperm"), board_id, user_id)) {
							JSONArray ja = dbc.getTable();
							if (ja.length() > 0) {
								dbc.execute(pr.getValue("getcarduser"), card_id);
								if (dbc.getTable().getJSONObject(0).getInt("user_id") == user_id || ja.getJSONObject(0).getInt("type_board_user_id") == 1) {
									chain.doFilter(req, res);
								}
								else {
									System.out.println("User is not allowed");
									json.put("status", 403)
										.put("msg", "You can't delete other users' cards");
									out.print(json.toString());
								}
							}
							else {
								System.out.println("User is not allowed");
								json.put("status", 403)
									.put("msg", "Your user can't perform this action");
								out.print(json.toString());
							}
						}
						else {
							json.put("status", 500)
								.put("msg", "Could not check permissions");
							out.print(json.toString());
						}
					}
					//Any other case. Mostly GET.
					else {
						chain.doFilter(req, res);
					}
				}
			}
			else {
				//Any other possible case? Maybe master user.
				chain.doFilter(req, res);
			}
		}
		
		
		
	}
	
	

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
