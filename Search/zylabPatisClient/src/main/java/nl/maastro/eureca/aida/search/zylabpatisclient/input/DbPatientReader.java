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
 * Query a database to retrieve a list of patients.  EMD and SOARIAN are 
 * potential database sources of patients.
 *
 * @author Kasper van den Berg <kasper@kaspervandenberg.net> <kasper.vandenberg@maastro.nl>
 */
@Repository
public abstract class DbPatientReader implements PatientProvider {
	/**
	 * Provide the sql query to query for patients.
	 *
	 * Derived classes implement {@link #getQueryProvider} which returns a 
	 * {@link QueryProvider}
	 */
	protected interface QueryProvider {
		public String getSql();
	}

	/**
	 * Default {@link QueryProvider} implementation which reads a query from a 
	 * resource.
	 */
	protected static class ResourceQueryProvider implements QueryProvider {
		private final String sqlResource;
		private final String sql;

		public ResourceQueryProvider(String sqlResource_)
				throws FileNotFoundException, IOException
		{
			this.sqlResource = sqlResource_;
			this.sql = readSql(sqlResource);
		}


		public String getSql() {
			return sql;
		}

		
		private static String readSql(String sqlResource) 
				throws FileNotFoundException, IOException
		{
			InputStream resourceAsStream = 
					ResourceQueryProvider.class.getResourceAsStream(sqlResource);
			if (resourceAsStream != null)
			{
				String result = IOUtils.toString(resourceAsStream, "UTF-8");
				return result;
			}
			else
			{
				throw new FileNotFoundException(String.format(
						"Resource %s does not exist",
						sqlResource));
			}
		}
	}


	protected static class PatisMapper implements RowMapper<PatisNumber> {
		private final String datasourceName;
		private final String fieldName;

		public PatisMapper(String datasourceName_, String fieldName_)
		{
			this.datasourceName = datasourceName_;
			this.fieldName = fieldName_;
		}

		@Override
		public PatisNumber mapRow(ResultSet rs, int rowNum) throws SQLException {
			String value = rs.getString(fieldName);
			if (value != null) {
				return PatisNumber.create(value);
			} else {
				throw new SQLDataException(new NullPointerException(String.format(
						"%s query contains NULL value for patis number (field %s) in row %d",
						datasourceName, fieldName,
						rowNum)));
			}
		}
	}

	
	protected abstract QueryProvider getQueryProvider() throws IOException;
	protected abstract RowMapper<PatisNumber> getPatisMapper();
	protected abstract Object[] getQueryParameters();

	private final String dataSourceName;

	private /*>>>@MonotonicNonNull*/ DataSource datasource = null;
	private /*>>>@MonotonicNonNull*/ JdbcTemplate template = null;


	public DbPatientReader(String dataSourceName_)
	{
		this.dataSourceName = dataSourceName_;
	}


	/*>>>@EnsuresNonNull({"datasource","template"})*/
	@Override
	public Collection<PatisNumber> getPatients() {
		if (datasource != null && template != null)
		{
			try
			{
				return template.query(
						getQueryProvider().getSql(),
						getQueryParameters(),
						getPatisMapper());
			}
			catch(IOException ex)
			{
				throw new Error(String.format(
						"Error reading patients from %s.",
						dataSourceName));
			}
		}
		else
		{
			throw new IllegalStateException(String.format(
					"Configure datasource %s via spring.",
					dataSourceName));
		}
	}


	/*>>>@EnsuresNonNull({"datasource","template"})*/
	public void setDatasource(DataSource datasource_)
	{
		this.datasource = datasource_;
		this.template = new JdbcTemplate(datasource);
	}


	/**
	 * Calculate a {@link java.util.Date} {@code deltaDays} in the future or
	 * in the past.
	 *
	 * @param deltaDays	<ul><li>{@code > 0}, returned {@code Date} is in the
	 * 			future;</li>
	 *		<li>{@code < 0}, returned {@code Date} is in the past; or</li>
	 *		<li>{@code == 0}, return today's date.</li></ul>
	 */
	protected static java.util.Date getDate(int deltaDays)
	{
		Calendar cal = Calendar.getInstance();
		java.util.Date now = new java.util.Date();
		cal.setTime(now);
		cal.add(Calendar.DAY_OF_MONTH, deltaDays);
		return cal.getTime();
	}
}

/* vim: set tabstop=4 shiftwidth=4 autoindent fo=cqwa2 : */

