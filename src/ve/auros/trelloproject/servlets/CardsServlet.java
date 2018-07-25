package ve.auros.trelloproject.servlets;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

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
		
		if (request.getRequestURI().contains("getfilelist")) {
			ArrayList<String> parameters2 = new ArrayList<String>();
			
			String[] queriesFromString = request.getQueryString().split("&");
			for (String params: queriesFromString) {
				parameters2.add(params.split("=")[1]);
			}
			Object[] paramArray = parameters2.toArray();
			int card_id = Integer.parseInt((String) paramArray[0]);
			
			if (dbc.execute(pr.getValue("getfilesbycard"), card_id)) {
				
				System.out.println("Files requested.");
				System.out.println(dbc.getTable());
				json.put("status", 200)
					.put("msg", "Files returned successfully.")
					.put("files", dbc.getTable());
				out.print(json.toString());
				System.out.println("Files sent.");
			}
			else {
				json.put("status", 500)
					.put("msg", "Could not return files.");
				out.print(json.toString());
				System.out.println("Couldn't send files.");
			}
		}
		else if (request.getRequestURI().contains("getcommentlist")) {
			ArrayList<String> parameters2 = new ArrayList<String>();
			
			String[] queriesFromString = request.getQueryString().split("&");
			for (String params: queriesFromString) {
				parameters2.add(params.split("=")[1]);
			}
			Object[] paramArray = parameters2.toArray();
			int card_id = Integer.parseInt((String) paramArray[0]);
			
			if (dbc.execute(pr.getValue("getcommentsbycard"), card_id)) {
				
				System.out.println("Comments requested.");
				System.out.println(dbc.getTable());
				json.put("status", 200)
					.put("msg", "Comments returned successfully.")
					.put("comments", dbc.getTable());
				out.print(json.toString());
				System.out.println("Comments sent.");
			}
			else {
				json.put("status", 500)
					.put("msg", "Could not return comments.");
				out.print(json.toString());
				System.out.println("Couldn't send comments.");
			}
		}
		else {
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
		LocalDateTime ts = LocalDateTime.now();
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		if (request.getRequestURI().contains("addfiles")) {
			int card_id = Integer.parseInt(request.getParameter("card_id"));
			int user_id = Integer.parseInt((String) session.getAttribute("user_id"));
			
			
			Collection<Part> files = request.getParts();
			files.remove(files.iterator().next());
			InputStream filecontent = null;
			OutputStream os = null;
			boolean success = true;
			try {
				String baseDir = "/home/andres/Eclipse/TrelloProject/WebContent/UploadedFiles";
				for (Part file : files) {
					filecontent = file.getInputStream();
					os = new FileOutputStream(baseDir + "/" + this.getFileName(file));
					int read = 0;
					byte[] bytes = new byte[1024];
					while ((read = filecontent.read(bytes)) != -1) {
						os.write(bytes, 0, read);
					}
					if (filecontent != null) {
						filecontent.close();
					}
					if (os != null) {
						os.close();
					}
					String file_name = this.getFileName(file);
					String file_url = "UploadedFiles/"+this.getFileName(file);
					if (dbc.execute(pr.getValue("addfile"), card_id, user_id, file_url, ts, file_name)) {
						success = true;
					}
					else {
						success = false;
						break;
					}
					
				}
				if (success) {
					json.put("status", 200)
						.put("msg", "Files added successfully");
					out.print(json.toString());
				}
				else {
					json.put("status", 500)
						.put("msg", "There was a problem adding some files to the card");
					out.print(json.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
					
		}
		else if (request.getRequestURI().contains("addcomment")){
			myVars.add(Integer.parseInt(request.getParameter("card_id")));
			myVars.add(Integer.parseInt((String) session.getAttribute("user_id")));
			myVars.add(request.getParameter("comment_text"));
			myVars.add(ts);
			
			System.out.println(myVars);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			if (dbc.execute(pr.getValue("addcomment"), myVars.toArray())) {
				json.put("status", 200)
					.put("msg", "Comment added successfully");
				out.print(json.toString());
					
				
			}
			else {
				json.put("status",  500)
					.put("msg", "Error adding a comment");
				out.print(json.toString());
			}
		}
		else {
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
		
	}
	
	private String getFileName(Part part) {
		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename")) {
				return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}
		
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;
		PrintWriter out = response.getWriter();
		JSONObject json = new JSONObject();
		DBConnection dbc;
		PropertiesReader pr = PropertiesReader.getInstance();
		
		dbc = new DBConnection(pr.getValue("pgurl"), pr.getValue("pguser"), pr.getValue("pgpass"), pr.getValue("driver"));
		dbc.connect();
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		if (request.getRequestURI().contains("deletefile")) {
				
			ArrayList<String> parameters2 = new ArrayList<String>();
			
			String[] queriesFromString = request.getQueryString().split("&");
			for (String params: queriesFromString) {
				parameters2.add(params.split("=")[1]);
			}
			Object[] paramArray = parameters2.toArray();
			int card_id = Integer.parseInt((String) paramArray[0]);
			
			if (dbc.execute(pr.getValue("deletefile"), card_id)) {
				json.put("status", 200)
					.put("msg", "File deleted from card successfully");
				out.print(json.toString());
			}
			else {
				json.put("status", 500)
					.put("msg", "Error deleting file from card");
				out.print(json.toString());
			}
		}
		else if (request.getRequestURI().contains("deletecomment")) {
			ArrayList<String> parameters2 = new ArrayList<String>();
			
			String[] queriesFromString = request.getQueryString().split("&");
			for (String params: queriesFromString) {
				parameters2.add(params.split("=")[1]);
			}
			Object[] paramArray = parameters2.toArray();
			int comment_id = Integer.parseInt((String) paramArray[0]);
				
			if (dbc.execute(pr.getValue("deletecomment"), comment_id)) {
				json.put("status", 200)
					.put("msg", "Comment deleted succesfully");
				out.print(json.toString());
			}
			else {
				json.put("status", 500)
					.put("msg", "Error deleting comment");
				out.print(json.toString());
			}
		}
		else {
			
			int card_id = Integer.parseInt(request.getPathInfo().substring(1, request.getPathInfo().length()));
			
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
