package net.sourceforge.taverna.baclava;

import java.awt.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;

/**
 * This class is designed as a wrapper for inputmaps and output maps. Values
 * that are typically stored in these maps are usually stored as DataThings. In
 * order to make the code cleaner, this wrapper, insures that values inserted
 * into these maps are inserted as DataThings.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class DataThingAdapter {

	private Map map;

	public DataThingAdapter(Map map) {
		this.map = map;
	}

	/**
	 * This method gets a value out of the map and returns it as a string. It
	 * performs the DataThing conversion for you.
	 * 
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		DataThing val = (DataThing) map.get(key);
		if (val == null) {
			return null;
		}
		String strVal = (String) val.getDataObject();

		return strVal;
	}

	/**
	 * This method gets an array of objects as a string array.
	 * 
	 * @param key
	 * @return
	 */
	public String[] getStringArray(String key) {
		DataThing val = (DataThing) map.get(key);
		if (val == null) {
			return null;
		}
		ArrayList list = (ArrayList) val.getDataObject();
		String[] vals = new String[list.size()];
		return (String[]) list.toArray(vals);

		// return (String[])val.getDataObject();

	}

	/**
	 * This method wraps the string value in a DataThing before storing it in
	 * the hashmap.
	 * 
	 * @param key
	 *            The key for the value.
	 * @param value
	 *            The value to be stored.
	 */
	public void putString(String key, String value) {
		DataThing val = new DataThing(value);
		this.map.put(key, val);
	}

	/**
	 * This method puts a boolean value into the map.
	 * 
	 * @param key
	 * @param value
	 */
	public void putBoolean(String key, boolean value) {
		DataThing val = new DataThing(String.valueOf(value));
		this.map.put(key, val);
	}

	/**
	 * This method gets a boolean value from the hashmap
	 * 
	 * @param key
	 * @return False if null, otherwise returns the value indicated by the key.
	 */
	public boolean getBoolean(String key) {
		DataThing val = (DataThing) map.get(key);
		if (val == null) {
			return false;
		}
		String strVal = (String) val.getDataObject();

		return Boolean.valueOf(strVal).booleanValue();
	}

	/**
	 * This method puts a string array into the DataThing map.
	 * 
	 * @param key
	 *            The key for the value.
	 * @param value
	 *            The values to be stored.
	 */
	public void putStringArray(String key, String[] values) {
		// ArrayList valueArray = new ArrayList();
		// valueArray = (ArrayList)Arrays.asList(values);
		DataThing val = new DataThing(values);
		this.map.put(key, val);
	}

	/**
	 * This method gets an integer value from the DataThing map
	 * 
	 * @param key
	 *            The key for the value.
	 * @return
	 */
	public int getInt(String key) {
		String strVal = getString(key);
		return Integer.parseInt(strVal);
	}

	/**
	 * This method puts an integer value into the DataThing map
	 * 
	 * @param key
	 *            The key for the value.
	 * @param value
	 *            The values to be stored.
	 */
	public void putInt(String key, int value) {
		putString(key, String.valueOf(value));
	}

	/**
	 * This method gets a two-dimensional array out of the DataThing map.
	 * 
	 * @param key
	 * @return
	 */
	public String[][] getArrayArray(String key) {
		String[][] values = null;
		DataThing val = (DataThing) map.get(key);

		return values;
	}

	/**
	 * This method puts a two-dimensional array into the DataThing map.
	 * 
	 * @param key
	 * @param value
	 */
	public void putArrayArray(String key, String[][] value) {
		map.put(key, DataThingFactory.bake(value));
	}

	/**
	 * This metod gets an ArrayList from the DataThing map.
	 * 
	 * @param key
	 * @return
	 */
	public ArrayList getArrayList(String key) {
		DataThing val = (DataThing) map.get(key);
		return (ArrayList) val.getDataObject();
	}

	/**
	 * This method puts an ArrayList into the DataThing map
	 * 
	 * @param key
	 * @param list
	 */
	public void putArrayList(String key, ArrayList list) {
		DataThing val = new DataThing(list);
		map.put(key, val);
	}

	/**
	 * This method puts a Serializable object into the map.
	 * 
	 * @param key
	 * @param obj
	 */
	public void putSerializable(String key, Serializable obj) {
		DataThing val = new DataThing(obj);
		map.put(key, val);
	}

	/**
	 * This method gets a Serializable object from the map.
	 * 
	 * @param key
	 * @return
	 */
	public Serializable getSerializable(String key) {
		DataThing val = (DataThing) map.get(key);
		return (Serializable) val.getDataObject();
	}

	public Image getImage(String key) {
		DataThing val = (DataThing) map.get(key);
		return (Image) val.getDataObject();
	}

	public void putImage(String key, Image img) {
		DataThing val = new DataThing(img);
		map.put(key, val);
	}

	public void putDouble(String key, Double db) {
		DataThing val = new DataThing(db);
		map.put(key, val);
	}

	public Double getDouble(String key) {
		DataThing val = (DataThing) map.get(key);
		return (Double) val.getDataObject();
	}

	public double getDoubleVal(String key) {
		return getDouble(key).doubleValue();
	}

}
