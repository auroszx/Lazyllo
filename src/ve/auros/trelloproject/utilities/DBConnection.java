package ve.auros.trelloproject.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

public class DBConnection {
	private static String pgurl;
	private static String pguser;
	private static String pgpass;
	private static String driver;
	private static Connection pgconn;
	private static PreparedStatement ps;
	private static ResultSet rs;
	private static ResultSetMetaData rsmd;
	
	
	public DBConnection(String pgurl, String pguser, String pgpass, String driver) {
		DBConnection.pgurl = pgurl;
		DBConnection.pguser = pguser;
		DBConnection.pgpass = pgpass;
		DBConnection.driver = driver;
	}
	
	public DBConnection() {
		DBConnection.pgurl = "jdbc:postgresql://localhost:5432/trello";
		DBConnection.pguser = "postgres";
		DBConnection.pgpass = "holaan";
		DBConnection.driver = "org.postgresql.Driver";
	}
	
	public Connection connect() {
		try {
			Class.forName(driver);
			pgconn = DriverManager.getConnection(pgurl, pguser, pgpass);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pgconn;
	}
	
	public void setParams(String url, String user, String pass, String driv) {
		pgurl = url;
		pguser = user;
		pgpass = pass;
		driver = driv;
	}
	
	public Boolean execute(String stmt) {
		Boolean executed = false;
		try {
			ps = pgconn.prepareStatement(stmt, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = ps.executeQuery();
			executed = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return executed;
	}
	
	public Boolean execute(String stmt, Object ... vars ) {
		Boolean executed = false;
		int counter = 1;
		try {
			ps = pgconn.prepareStatement(stmt, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			for (Object var: vars) {
				//System.out.println(counter+"Var: " + var);
				ps.setObject(counter, var);
				counter++;
			}
			if (stmt.startsWith("SELECT")) {
				rs = ps.executeQuery();
				executed = true;
			}
			else {
				ps.execute();
				executed = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return executed;
	}
	
	public JSONArray getTable() {
		JSONArray table = null;
		JSONObject row;
		try {
			rsmd = rs.getMetaData();
			rs.last();
			table = new JSONArray();
			rs.beforeFirst();
			while (rs.next()) {
				row = new JSONObject();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					System.out.println(rsmd.getColumnLabel(i) + ": "+rs.getObject(i));
					row.put(rsmd.getColumnLabel(i), rs.getObject(i));
				}
				table.put(row);
			}
		}
		catch (Exception e) {
			e.getStackTrace();
		}
		return table;
	}
	
	
	
	public void disconnect() {
		try {
			pgconn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
