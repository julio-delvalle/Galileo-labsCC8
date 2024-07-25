import java.sql.*;

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


	public boolean insertMailToDB(String fromParam, String toParamStr, String subjectParam, String bodyParam){
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
			" DATE      DATETIME  default current_timestamp )"; 
			stmt.executeUpdate(sql);

			String mailData = "Subject: "+subjectParam+"\nFrom: "+fromParam+"\nTo: "+toParamStr+"\n\n"+bodyParam;

			sql = "INSERT INTO SMTP_DB (MAIL_FROM, RCPT_TO, DATA) " + "VALUES (\""+fromParam+"\", \""+toParamStr+"\", \""+mailData+"\");";
			
			System.out.println("QUERY: "+sql);
			
			stmt.executeUpdate(sql);

			sql = "SELECT IDmail FROM SMTP_DB ORDER BY IDmail DESC LIMIT 1;";
			
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
			return false;
		}
		System.out.println("Table created successfully");
		return true;
	}
}