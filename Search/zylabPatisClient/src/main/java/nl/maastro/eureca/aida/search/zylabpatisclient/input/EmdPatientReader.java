// © Maastro, 2014
package nl.maastro.eureca.aida.search.zylabpatisclient.input;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.MonotonicNonNull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import javax.annotation.Resource;
import javax.sql.DataSource;
import net.kaspervandenberg.apps.common.util.cache.Cache;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatientProvider;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import org.springframework.stereotype.Repository;

/**
 * Query the EMD database to retrieve a list of patients.
 *
 * @author Kasper van den Berg <kasper@kaspervandenberg.net> <kasper.vandenberg@maastro.nl>
 */
@Repository
public class EmdPatientReader implements PatientProvider {
	private static class Query {
		private final String sql;

		public Query() throws FileNotFoundException, IOException
		{
			this.sql = readSql();
		}


		public String getSql() {
			return sql;
		}

		
		private static String readSql() throws FileNotFoundException, IOException
		{
			InputStream resourceAsStream = Query.class.getResourceAsStream(
					EmdPatientReader.RECENT_PATIENTS_SQL_FILE);
			if (resourceAsStream != null)
			{
				String result = IOUtils.toString(resourceAsStream, "UTF-8");
				return result;
			}
			else
			{
				throw new FileNotFoundException(String.format(
						"Resource %s does not exist",
						EmdPatientReader.RECENT_PATIENTS_SQL_FILE));
			}
		}
	}

	private static class PatisMapper implements RowMapper<PatisNumber> {
		@Override
		public PatisNumber mapRow(ResultSet rs, int rowNum) throws SQLException {
			String value = rs.getString(EmdPatientReader.PATIS_FIELD);
			if (value != null) {
				return PatisNumber.create(value);
			} else {
				throw new SQLDataException(new NullPointerException(String.format(
						"EMD query contains NULL value for patis number in row %d",
						rowNum)));
			}
		}
	}


	private static final String RECENT_PATIENTS_SQL_FILE = "/sql/selectPatients.sql";
	private static final String PATIS_FIELD = "patisnummer";
	private static final int DEFAULT_PERIOD_DAYS = 7;
	
	private static transient Cache<Query> query = new Cache<Query>() {
		@Override
		protected Query calc() {
			try
			{
				return new Query();
			}
			catch (IOException ex)
			{
				throw new Error(String.format(
						"Cannot read query sql resource %s",
						EmdPatientReader.RECENT_PATIENTS_SQL_FILE),
						ex);
			}
		}
	};

	private static transient PatisMapper mapper = new PatisMapper();
	
	private /*>>>@MonotonicNonNull*/ DataSource emd = null;
	private /*>>>@MonotonicNonNull*/ JdbcTemplate template = null;
	private java.util.Date selection_start_date;


	public EmdPatientReader()
	{
		this(DEFAULT_PERIOD_DAYS);
	}


	public EmdPatientReader(int selectionPeriodInDays_)
	{
		this.selection_start_date = calcSelectionDate(selectionPeriodInDays_);
	}
	

	/*>>>@EnsuresNonNull({"emd","template"})*/
	@Override
	public Collection<PatisNumber> getPatients() {
		if (emd != null && template != null)
		{
			return template.query(
					query.get().getSql(),
					new Object[]{selection_start_date, selection_start_date},
					mapper);
		}
		else
		{
			throw new IllegalStateException("Configure datasource emd via spring.");
		}
	}


	/*>>>@EnsuresNonNull({"emd","template"})*/
	@Resource(name="emd.datasource")
	public void setEmd(DataSource emd_)
	{
		this.emd = emd_;
		this.template = new JdbcTemplate(emd_);
	}


	public void setPeriod(int nDaysAgo_)
	{
		this.selection_start_date = calcSelectionDate(nDaysAgo_);
	}

	

	private static java.util.Date calcSelectionDate(int nDaysAgo_)
	{
		Calendar cal = Calendar.getInstance();
		java.util.Date now = new java.util.Date();
		cal.setTime(now);
		cal.add(Calendar.DAY_OF_MONTH, -1 * DEFAULT_PERIOD_DAYS);
		return cal.getTime();
	}
}

/* vim: set tabstop=4 shiftwidth=4 autoindent fo=cqwa2 : */

