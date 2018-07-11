package ve.auros.trelloproject.filters;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

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

import org.json.JSONObject;

import ve.auros.trelloproject.utilities.DBConnection;
import ve.auros.trelloproject.utilities.PropertiesReader;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter("/RegisterServlet")
@MultipartConfig
public class RegisterFilter implements Filter {

    /**
     * Default constructor. 
     */
    public RegisterFilter() {
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
		Enumeration<String> parameters = req.getParameterNames();
		String param = null;
		String[] paramVals = null;
		Boolean flag = false;
		Boolean flag_empty = false;
		DBConnection dbc;
		PropertiesReader pr = PropertiesReader.getInstance();
		JSONObject json = new JSONObject();
		
		//Check parameters sent.
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();

		while (parameters.hasMoreElements()) {
			param = null;
			paramVals = null;
			param = (String) parameters.nextElement();
			paramVals = request.getParameterValues(param);
			System.out.println(paramVals[0]);
			if (paramVals[0].equals("")) {
				flag_empty = true;
			}
		}
		System.out.println("Is empty? "+flag_empty);
		
		System.out.println(request.getParameterValues("user_username")[0]);
		
		//Check for user_username. Test.
		if (dbc.execute(pr.getValue("checkuser"), request.getParameterValues("user_username")[0])) {
			System.out.println(dbc.getTable());
			 if (dbc.getTable().length() >= 1) {
				 flag = true;
			 }
		}
		
		if (flag) { 
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			json.put("status", 406)
				.put("msg", "This username already exists.")
				.put("redirect", "/TrelloProject");
			out.print(json.toString());
			System.out.println("This username already exists.");
			return;
		}
		
		if (flag_empty) {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			json.put("status", 406)
				.put("msg", "Check for empty fields and try again.")
				/*.put("redirect", "/TrelloProject")*/;
			out.print(json.toString());
			System.out.println("Check for empty fields and try again.");
			return;
		}
		
		chain.doFilter(req, res);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
