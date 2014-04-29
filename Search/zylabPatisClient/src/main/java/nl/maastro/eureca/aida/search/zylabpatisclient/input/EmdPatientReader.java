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
public class EmdPatientReader extends DbPatientReader implements PatientProvider {
	private static final String RECENT_PATIENTS_SQL_FILE = "/sql/selectPatients-emd.sql";
	private static final String PATIS_FIELD = "patisnummer";
	private static final int DEFAULT_PERIOD_DAYS = 7;
	
	private static transient Cache<DbPatientReader.QueryProvider> query = 
			new Cache<DbPatientReader.QueryProvider>() {
		@Override
		protected DbPatientReader.QueryProvider calc() {
			try
			{
				return new DbPatientReader.ResourceQueryProvider(
						RECENT_PATIENTS_SQL_FILE);
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

	private static transient DbPatientReader.PatisMapper mapper = 
			new DbPatientReader.PatisMapper(
					RECENT_PATIENTS_SQL_FILE, PATIS_FIELD);
	
	private java.util.Date selection_start_date;


	public EmdPatientReader()
	{
		this(DEFAULT_PERIOD_DAYS);
	}


	public EmdPatientReader(int selectionPeriodInDays_)
	{
		super("emd");
		this.selection_start_date = getDate(-1 * selectionPeriodInDays_);
	}
	

	@Resource(name="emd.datasource")
	public void setEmd(DataSource emd_)
	{
		setDatasource(emd_);
	}


	public void setPeriod(int nDaysAgo_)
	{
		this.selection_start_date = getDate(-1 * nDaysAgo_);
	}

	
	protected QueryProvider getQueryProvider()
	{
		return query.get();
	}


	protected DbPatientReader.PatisMapper getPatisMapper()
	{
		return mapper;
	}


	protected Object[] getQueryParameters()
	{
		return new Object[] {
				selection_start_date, selection_start_date };
	}
}

/* vim: set tabstop=4 shiftwidth=4 autoindent fo=cqwa2 : */

