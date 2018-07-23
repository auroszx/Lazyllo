package ve.auros.trelloproject.filters;

import java.io.IOException;
import java.io.PrintWriter;

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
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
	
		
		if(session.isNew()) {
			session.invalidate();
			
			json.put("status", 403)
				.put("msg", "Guests are not allowed to check boards")
				/*.put("redirect", "/TrelloProject")*/;
			out.print(json.toString());
			
		}
		else {
			if (request.getRequestURI().contains("Boards")) {
				String board_id = request.getPathInfo().substring(1, request.getPathInfo().length());
				String user_id = (String) session.getAttribute("user_id");
				
				System.out.println("Request URI: "+request.getRequestURI());
				
				if(dbc.execute(pr.getValue("getboardperm"), Integer.parseInt(board_id), Integer.parseInt(user_id))) {
					if (dbc.getTable().length() > 0) {
						
						chain.doFilter(req, res);
					}
					else {
						json.put("status", 403)
							.put("msg", "You don't have permission to see this board");
						out.print(json.toString());
						return;
					}
				}
				else {
					json.put("status", 500)
						.put("msg", "Error checking permissions");
					out.print(json.toString());
					return;
				}
			}
			else {
				System.out.println("Request URI: "+request.getRequestURI());
				chain.doFilter(req, res);
			}
			
		}
		
		
		//chain.doFilter(req, res);
	}
	
	

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
