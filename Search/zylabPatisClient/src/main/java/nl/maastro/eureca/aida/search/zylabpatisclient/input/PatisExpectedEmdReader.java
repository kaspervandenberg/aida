// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.input;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.NonNull;
import checkers.nullness.quals.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;

/**
 * Query the EMD for the patient's expected metastasis.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class PatisExpectedEmdReader implements PatisCsvReader.Classifier {
	private static class DBConnection {
		private static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
		private static final String DB_URL = "jdbc:mysql://as-emd-01-s";
		private static final String QUERY =
			"SELECT patient.patisnummer, " +
				"t.t, " +
				"n.n, " +
				"m.m, " +
				"icd_primaire_tumor_hoofdgroep.omschrijving, " +
				"icd_primaire_tumor_hoofdgroep.hoofdgroep_code, " +
				"icd_primaire_tumor_subgroep.diagnose, " +
				"icd_primaire_tumor_subgroep.diag_code " +
			"FROM	 (	(	(	(	(	web_emd.tumoren_prim tumoren_prim " +
												"LEFT OUTER JOIN " +
													"web_emd_keuzelijsten.m m " +
												"ON (tumoren_prim.m_klinisch = m.rec_id)) " +
										"RIGHT OUTER JOIN " +
											"web_emd.patient patient " +
										"ON (patient.rec_id = tumoren_prim.patient_rec_id)) " +
									"LEFT OUTER JOIN " +
										"web_emd_keuzelijsten.t t " +
									"ON (tumoren_prim.t_klinisch = t.rec_id)) " +
								"LEFT OUTER JOIN " +
									"web_emd_keuzelijsten.n n " +
								"ON (tumoren_prim.n_klinisch = n.rec_id)) " +
						"LEFT OUTER JOIN " +
							"web_emd_keuzelijsten.icd_primaire_tumor_hoofdgroep icd_primaire_tumor_hoofdgroep " +
						"ON (tumoren_prim.icd_primair_hoofdgroep = " +
									"icd_primaire_tumor_hoofdgroep.rec_id)) " +
					"LEFT OUTER JOIN " +
						"web_emd_keuzelijsten.icd_primaire_tumor_subgroep icd_primaire_tumor_subgroep " +
					"ON (tumoren_prim.icd_primair_subgroep = " +
							"icd_primaire_tumor_subgroep.rec_id) " +
			"WHERE (patient.patisnummer IN (?) " +
			")";

		private static @Nullable DBConnection singleton = null;

		private final String username;
		private final String password;
		private @Nullable Connection connection = null;
		private @Nullable PreparedStatement statement = null;
		
		private DBConnection() {
			this.username = getUsername();
			this.password = getPassword();
		}

		@EnsuresNonNull({"singleton", "singleton.connection", "singleton.statement"})
		public static DBConnection instance() {
			if(singleton == null) {
				singleton = new DBConnection();
			}
			singleton.init();
			return singleton;
		}
		
		public void close() {
			try {
				if (this.statement != null) {
					this.statement.closeOnCompletion();
					this.statement = null;
				}
				if (this.connection != null) {
					this.connection.close();
					this.connection = null;
				}
			} catch (SQLException ex) {
				throw new Error(ex);
			}
		}

		public ResultSet query(PatisNumber patient) {
			init();
			synchronized (statement) {
				try {
					statement.setString(1, patient.getValue());
					return statement.executeQuery();
				} catch (SQLException ex) {
					throw new Error(ex);
				}
			}
		}

		@SuppressWarnings("nullness")	// Suppressing "postcondition not satisfied"-warning as per http://stackoverflow.com/questions/21308736/ensuresnonnullif-annotation-gives-conditional-postcondition-not-satisfied-war
		@EnsuresNonNull({"connection", "statement"})
		private void init() {
			init_impl();
		}

		private void init_impl() {
			try {
				if (connection == null || connection.isClosed()) {
					this.connection = initConnection(username, password);
				}
				@NonNull Connection initialisedConnection = connection;
				if(statement == null || statement.isClosed()) {
					this.statement = initPreparedStatement(initialisedConnection);
				}
			} catch (SQLException ex) {
				throw new Error(ex);
			}
		}

		private static void checkDriver() {
			try {
				Class.forName(DRIVER_CLASS);
			} catch (ClassNotFoundException ex) {
				throw new Error(ex);
			}
		}

		private static Connection initConnection(String username, String password) {
			try {
				return DriverManager.getConnection(DB_URL, username, password);
			} catch (SQLException ex) {
				throw new Error(ex);
			}
		}

		private static PreparedStatement initPreparedStatement(Connection conn) {
			try {
				return conn.prepareStatement(QUERY);
			} catch (SQLException ex) {
				throw new Error(ex);
			}
		}

		private static String getUsername() {
			String prompt = String.format("Username for (%s): ", DB_URL);
			return askUser(prompt);
		}

		private static String getPassword() {
			String prompt = String.format("Username for (%s): ", DB_URL);
			return askUser(prompt);
		}
		
		private static String askUser(String prompt) {
			if(java.awt.GraphicsEnvironment.isHeadless()) {
				try {
					System.out.print(prompt);
					BufferedReader userinput = new BufferedReader(new InputStreamReader(System.in));
					String response = userinput.readLine();
					if (response != null) {
						return response;
					} else {
						throw new Error("User provided no answer");
					}
				} catch (IOException ex) {
					throw new Error(ex);
				}
			} else {
				return javax.swing.JOptionPane.showInputDialog(prompt);
			}
		}
	}

	public ConceptFoundStatus getExpectedMetastasis(PatisNumber patient) {
		try (ResultSet results = DBConnection.instance().query(patient)) {
			while(results.next()) {
				if(results.getBoolean("m")) {
					return ConceptFoundStatus.FOUND;
				}
			}
			return ConceptFoundStatus.NOT_FOUND;
		} catch (SQLException ex) {
			throw new Error(ex);
		}
	}
	
	public Map<PatisNumber, ConceptFoundStatus> getExpectedMetastasis(Iterable<PatisNumber> patients) {
		Map<PatisNumber, ConceptFoundStatus> result = new LinkedHashMap<>();
		for (PatisNumber patient : patients) {
			result.put(patient, getExpectedMetastasis(patient));
		}
		return result;
	}

	@Override
	public ConceptFoundStatus expectedClassification(
			PatisNumber patient, String[] textFields) {
		return getExpectedMetastasis(patient);
	}
}
