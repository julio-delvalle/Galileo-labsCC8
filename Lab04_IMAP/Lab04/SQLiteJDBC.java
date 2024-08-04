import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteJDBC {

	public static void main( String args[] ) {
		Connection c = null;
		Statement stmt = null;

		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:SMTP_SERVER.db");
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS SMTP_DB ( " +
			" IDmail    INTEGER PRIMARY KEY AUTOINCREMENT," +
			" MAIL_FROM TEXT    NOT NULL, " + 
			" RCPT_TO   TEXT    NOT NULL, " + 
			" DATA      TEXT, " + 
			" READ      INTEGER DEFAULT 0, " + 
			" DELETED      INTEGER DEFAULT 0, " + 
			" DATE      DATETIME  default current_timestamp )"; 
			stmt.executeUpdate(sql);

			sql = "INSERT INTO SMTP_DB (MAIL_FROM, RCPT_TO, DATA) " +
			"VALUES (\"MAIL_FROM@lab03.com\", \"RCPT_TO@gmail.com\", \"HOLA MUNDO!\");";

			stmt.executeUpdate(sql);

			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		System.out.println("Table created successfully");
	}


	public int insertMailToDBSimple(String fromParam, String toParamStr, String subjectParam, String bodyParam){
		Connection c = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:SMTP_SERVER.db");
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS SMTP_DB ( " +
			" IDmail    INTEGER PRIMARY KEY AUTOINCREMENT," +
			" MAIL_FROM TEXT    NOT NULL, " + 
			" RCPT_TO   TEXT    NOT NULL, " + 
			" DATA      TEXT, " + 
			" READ      INTEGER DEFAULT 0, " + 
			" DELETED      INTEGER DEFAULT 0, " + 
			" DATE      DATETIME  default current_timestamp )"; 
			stmt.executeUpdate(sql);

			sql = "INSERT INTO SMTP_DB (MAIL_FROM, RCPT_TO, DATA) " + "VALUES (\""+fromParam+"\", \""+toParamStr+"\", \""+bodyParam+"\");";
			
			System.out.println("QUERY: "+sql);
			
			stmt.executeUpdate(sql);
			stmt.close();


			Statement stmt2 = c.createStatement();
			
			sql = "SELECT IDmail FROM SMTP_DB ORDER BY IDmail DESC LIMIT 1";
            rs = stmt2.executeQuery(sql);
			
			int rowID = -1;
			rowID = rs.getInt("IDMail");
			System.out.println("\n\n\nLast ID: " + rowID);

			if (rs.next()) {
                rowID = rs.getInt("IDMail");
                System.out.println("Last ID: " + rowID);
            }


			
			stmt2.close();
			c.close();

			return rowID;

		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
			return 0;
		}
	}

	public int insertMailToDBbuildBody(String fromParam, String toParamStr, String subjectParam, String bodyParam){
		Connection c = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:SMTP_SERVER.db");
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS SMTP_DB ( " +
			" IDmail    INTEGER PRIMARY KEY AUTOINCREMENT," +
			" MAIL_FROM TEXT    NOT NULL, " + 
			" RCPT_TO   TEXT    NOT NULL, " + 
			" DATA      TEXT, " + 
			" READ      INTEGER DEFAULT 0, " + 
			" DELETED      INTEGER DEFAULT 0, " + 
			" DATE      DATETIME  default current_timestamp )"; 
			stmt.executeUpdate(sql);

			String mailData = "Subject: "+subjectParam+"\nFrom: "+fromParam+"\nTo: "+toParamStr+"\n\n"+bodyParam;

			sql = "INSERT INTO SMTP_DB (MAIL_FROM, RCPT_TO, DATA) " + "VALUES (\""+fromParam+"\", \""+toParamStr+"\", \""+mailData+"\");";
			
			//System.out.println("QUERY: "+sql);
			
			stmt.executeUpdate(sql);
			stmt.close();


			Statement stmt2 = c.createStatement();
			
			sql = "SELECT IDmail FROM SMTP_DB ORDER BY IDmail DESC LIMIT 1";
            rs = stmt2.executeQuery(sql);
			
			int rowID = -1;
			rowID = rs.getInt("\n\n\nIDMail");
			System.out.println("Last ID: " + rowID);

			if (rs.next()) {
                rowID = rs.getInt("IDMail");
                System.out.println("Last ID: " + rowID);
            }


			
			stmt2.close();
			c.close();

			return rowID;
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
			return 0;
		}
	}


	public HashMap<String,String> getInboxInfo(String user){
		HashMap<String, String> inboxInfo = new HashMap<>();
		Connection c = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			int exists = -1;
			int recent = -1;
			int unseen = -1;
			int firstunseen = -1;
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:SMTP_SERVER.db");
			//System.out.println("Opened database successfully");

			try {

				//número de EXISTS
				String sql = "select count(*) as cuenta from SMTP_DB where rcpt_to = ?"; 
				var pstmt = c.prepareStatement(sql);
				pstmt.setString(1, user);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					exists = rs.getInt("cuenta"); 
				}
				pstmt.close();



				//número de RECENT
				sql = "select count(*) as cuenta from SMTP_DB where rcpt_to = ? and date > datetime('now', '-1 day')"; 
				pstmt = c.prepareStatement(sql);
				pstmt.setString(1, user);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					recent = rs.getInt("cuenta"); 
				}
				pstmt.close();
				
				
				
				
				//número de UNSEEN
				sql = "select count(*) as cuenta from SMTP_DB where rcpt_to = ? and read = 0"; 
				pstmt = c.prepareStatement(sql);
				pstmt.setString(1, user);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					unseen = rs.getInt("cuenta"); 
				}
				pstmt.close();



				//ID del primer unseen
				sql = """
					select IDmail as first from SMTP_DB where RCPT_TO = ? and read = 0
					order by first
					limit 1"""; 
				pstmt = c.prepareStatement(sql);
				pstmt.setString(1, user);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					firstunseen = rs.getInt("first"); 
				}
				pstmt.close();


			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}
			inboxInfo.put("exists", Integer.toString(exists));
			inboxInfo.put("recent", Integer.toString(recent));
			inboxInfo.put("unseen", Integer.toString(unseen));
			inboxInfo.put("firstunseen", Integer.toString(firstunseen));
			
			return inboxInfo;
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
			return inboxInfo;
		}
	}




	public List<Map<String,String>> getUserAllMailIDs(String user){
		List<Map<String, String>> listOfMails = new ArrayList<Map<String, String>>();
		Connection c = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			int mailID = -1;
			int recent = -1;
			int seen = -1;
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:SMTP_SERVER.db");
			//System.out.println("Opened database successfully");

			try {
				//Obtener ID, recent y Seen de todos los correos del usuario:
				String sql = """
					select IDmail as id,
					read,
					case when date > datetime('now', '-1 day') then 1 else 0 end as recent
					from SMTP_DB where RCPT_TO = ?"""; 
				var pstmt = c.prepareStatement(sql);
				pstmt.setString(1, user);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					Map<String, String> mail = new HashMap<>();	

					mailID = rs.getInt("id"); 
					recent = rs.getInt("recent"); 
					seen = rs.getInt("read"); 

					mail.put("id", Integer.toString(mailID));
					mail.put("recent", Integer.toString(recent));
					mail.put("seen", Integer.toString(seen));

					listOfMails.add(mail);	//Guarda un mail con ID, recent y seen.

					mailID = -1;
					recent = -1;
					seen = -1;
				}
				pstmt.close();

			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}
			return listOfMails;
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
			return listOfMails;
		}
	}




	public List<Map<String,String>> getUserMailsRange(String user, int firstMail, int lastMail){
		List<Map<String, String>> listOfMails = new ArrayList<Map<String, String>>();
		Connection c = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			int mailID = -1;
			int recent = -1;
			int seen = -1;
			String body = "";
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:SMTP_SERVER.db");
			//System.out.println("Opened database successfully");

			try {
				//Obtener ID, recent y Seen de todos los correos del usuario:
				String sql = """
					select IDmail as id,
					read,
					case when date > datetime('now', '-1 day') then 1 else 0 end as recent,
					data as body
					from SMTP_DB 
					where RCPT_TO = ?
						and IDmail >= ?
						and IDmail <= ?"""; 
				var pstmt = c.prepareStatement(sql);
				pstmt.setString(1, user);
				pstmt.setInt(2, firstMail);
				pstmt.setInt(3, lastMail);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					Map<String, String> mail = new HashMap<>();	

					mailID = rs.getInt("id"); 
					recent = rs.getInt("recent"); 
					seen = rs.getInt("read"); 
					body = rs.getString("body"); 

					mail.put("id", Integer.toString(mailID));
					mail.put("recent", Integer.toString(recent));
					mail.put("seen", Integer.toString(seen));
					mail.put("body", body);

					listOfMails.add(mail);	//Guarda un mail con ID, recent y seen.

					//Reinicia las variables
					mailID = -1;
					recent = -1;
					seen = -1;
					body = "";
				}
				pstmt.close();

			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}
			return listOfMails;
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
			return listOfMails;
		}
	}
}