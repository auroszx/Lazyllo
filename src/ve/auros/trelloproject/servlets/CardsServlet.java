package ve.auros.trelloproject.servlets;

import java.io.IOException;
import java.io.PrintWriter;

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
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
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
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		String board_id = request.getParameterValues("board_id")[0];
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		if (dbc.execute(pr.getValue("getboarddata"), Integer.parseInt(board_id))) {
			json.put("status", 200)
				.put("msg", "Board data retrieved successfully")
				.put("boarddata", dbc.getTable());;
			out.print(json.toString());
		}
		else {
			json.put("status", 500)
				.put("msg", "There was a problem retrieving the board data");
			out.print(json.toString());
		}
	}
		
		
		
		
		

}
