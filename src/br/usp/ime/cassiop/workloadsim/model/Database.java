package br.usp.ime.cassiop.workloadsim.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {

	Connection con = null;

	final Logger logger = LoggerFactory.getLogger(Database.class);

	public synchronized void connect() {

		if (con == null) {

			String dbUrl = "jdbc:mysql://localhost/workload";
			String dbUser = "root";
			String dbPass = "";
			String dbClass = "com.mysql.jdbc.Driver";

			try {

				Class.forName(dbClass);
				con = DriverManager.getConnection(dbUrl, dbUser, dbPass);

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Connection getConnection() {
		return con;
	}

	public void closeConnection() {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
