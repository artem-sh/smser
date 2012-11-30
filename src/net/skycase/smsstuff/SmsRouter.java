package net.skycase.smsstuff;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmsRouter {
	//db settings:
	public static final String JDBC_URL = "jdbc:mysql:///test";
	public static final String JDBC_USER = "root";
	public static final String JDBC_PASS = "myDB";

	//phone number constants:
	public static final int MAX_RAW_NUMBER_LENGTH = 12; //because of possible leading '+'
	public static final int DEF_PREF_LENGTH = 3; //Length of DEF (non-geographic) code, like 905, 921, 911
	public static final String RU_PREF1 = "+7";
	public static final String RU_PREF2 = "8";
	private PreparedStatement selectBotReq = null;

	public SmsRouter() {
		Connection conn;
		try {
			//commented cause using jdbc 4.0 style (see file META-INF\services\java.sql.Driver )
			//Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
			selectBotReq = conn.prepareStatement("SELECT bot FROM `dialplan` WHERE def = ? AND firstnumber <= ? AND ? <= lastnumber");
		} catch (SQLException e) {
			System.err.println("Exception in SmsRouter " + e.getMessage());
		}

	}

	public void route(String rawNumber) throws SmsException {
		String number = null;
		int rawNumberLength = rawNumber.length();

		if (rawNumberLength == MAX_RAW_NUMBER_LENGTH && rawNumber.startsWith(RU_PREF1)) {
			number = rawNumber.substring(RU_PREF1.length(), rawNumberLength);
		} else if (rawNumberLength == MAX_RAW_NUMBER_LENGTH - 1 && rawNumber.startsWith(RU_PREF2)) {
			number = rawNumber.substring(RU_PREF2.length(), rawNumberLength);
		}

		if (number == null) {
			throw new SmsException("Incorrect phone number format in given " + rawNumber);
		}

		try {
			selectBotReq.setString(1, number.substring(0, DEF_PREF_LENGTH));
			selectBotReq.setString(2, number);
			selectBotReq.setString(3, number);
			
			ResultSet rs = selectBotReq.executeQuery();
			rs.getMetaData();
			
			System.out.println(rs.getString("bot"));
			//Bot bot = (Bot) Class.forName(rs.getString("bot")).newInstance();
			//bot.run();
		} catch (SQLException ex) {
			throw new SmsException("Problems with DB" + ex.getMessage());
		} catch (Exception e) {
			System.err.println("Exception in SmsRouter.route() " + e.getMessage());
		}

	}

	public static void main(String[] args) {
		try {
			SmsRouter smsRouter = new SmsRouter();
			smsRouter.route("89052764799");
		} catch (SmsException ex) {
			Logger.getLogger(SmsRouter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}