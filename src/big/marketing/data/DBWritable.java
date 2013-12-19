package big.marketing.data;

import com.mongodb.DBObject;

/**
 * This interface provides a method for conversation ob the object to an object
 * than can be written to the database.
 * 
 * @author oschwede
 * 
 */
public interface DBWritable {
	/**
	 * @return current object converted to DBObject
	 */
	public DBObject asDBObject();
}
