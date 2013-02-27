package org.github.nlloyd.hornofmongo.adaptor;

import java.net.UnknownHostException;

import org.github.nlloyd.hornofmongo.MongoRuntime;
import org.github.nlloyd.hornofmongo.action.NewInstanceAction;
import org.github.nlloyd.hornofmongo.util.BSONizer;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * JavaScript host Mongo object that acts as an adaptor between the
 * JavaScript Mongo API and the {@link com.mongodb.Mongo} Java driver class.
 * 
 * @author nlloyd
 *
 */
public class Mongo extends ScriptableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6810309240609504412L;
	
	protected com.mongodb.Mongo innerMongo;
	
	protected String host;

	@JSConstructor
	public Mongo() throws UnknownHostException {
		super();
		initMongo("localhost");
	}
	
	@JSConstructor
	public Mongo(final Object host) throws UnknownHostException {
		super();
		if(host instanceof Undefined)
		    initMongo("localhost");
		else
		    initMongo(host.toString());
	}
	
	private void initMongo(String host) throws UnknownHostException {
		this.host = host;
		this.innerMongo = new com.mongodb.Mongo(this.host);
	}

	/**
	 * @see org.mozilla.javascript.ScriptableObject#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}
	
	// --- Mongo JavaScript function implementation ---
	
	@JSFunction
	public Object find(final String ns , final Object query , final Object fields , int limit , int skip , int batchSize , int options) {
		System.out.printf("find(%s, %s, %s, %d, %d, %d, %d)\n", ns, query, fields, limit, skip, batchSize, options);
		Object result = null;
		
        Object rawQuery = BSONizer.convertJStoBSON(query);
        Object rawFields = BSONizer.convertJStoBSON(fields);
        // TODO assert that rawQuery and rawFields are DBObject instances?
        DBObject bsonQuery = null;
        DBObject bsonFields = null;
        if(rawQuery instanceof DBObject)
        	bsonQuery = (DBObject)rawQuery;
        if(rawFields instanceof DBObject)
        	bsonFields = (DBObject)rawFields;
		com.mongodb.DB db = innerMongo.getDB(ns.substring(0, ns.indexOf('.')));
		String collectionName = ns.substring(ns.indexOf('.') + 1);
		if("$cmd".equals(collectionName)) {
			CommandResult cmdResult = db.command(bsonQuery, options);
			Object jsCmdResult = BSONizer.convertBSONtoJS(cmdResult);
			result = MongoRuntime.call(new NewInstanceAction("InternalCursor", new Object[]{jsCmdResult}));
		} else {
			if("$cmd.sys.inprog".equals(collectionName))
				collectionName = "$cmd.sys.inprog";
			DBCollection collection = db.getCollection(collectionName);
			DBCursor cursor = collection.find(bsonQuery, bsonFields).skip(skip).limit(limit).batchSize(batchSize).addOption(options);
			InternalCursor jsCursor = (InternalCursor)MongoRuntime.call(new NewInstanceAction("InternalCursor", new Object[]{cursor}));
			result = jsCursor;
		}
		
		return result;
	}
	
	@JSFunction
	public void insert(final String ns, Object obj) {
		String str = obj.toString();
		System.out.printf("insert(%s, %s)\n", ns, str);
		
        Object rawObj = BSONizer.convertJStoBSON(obj);
        // TODO assert that rawQuery and rawFields are DBObject instances?
        DBObject bsonObj = null;
        if(rawObj instanceof DBObject)
        	bsonObj = (DBObject)rawObj;
		// TODO some sort of assertion that ns contains a '.'?
		com.mongodb.DB db = innerMongo.getDB(ns.substring(0, ns.indexOf('.')));
		DBCollection collection = db.getCollection(ns.substring(ns.lastIndexOf('.') + 1));
		collection.insert(bsonObj);
	}
	
	@JSFunction
	public void remove(final String ns, Object pattern) {
		String str = pattern.toString();
		System.out.printf("remove(%s, %s)\n", ns, str);
		
        Object rawPattern = BSONizer.convertJStoBSON(pattern);
        // TODO assert that rawQuery and rawFields are DBObject instances?
        DBObject bsonPattern = null;
        if(rawPattern instanceof DBObject)
        	bsonPattern = (DBObject)rawPattern;
		// TODO some sort of assertion that ns contains a '.'?
		com.mongodb.DB db = innerMongo.getDB(ns.substring(0, ns.indexOf('.')));
		DBCollection collection = db.getCollection(ns.substring(ns.lastIndexOf('.') + 1));
		collection.remove(bsonPattern);
	}
	
	@JSFunction
	public void update(final String ns, Object query, Object obj, boolean upsert) {
		System.out.printf("update(%s, %s, %s, %b)\n", ns, query, obj, upsert);

        Object rawQuery = BSONizer.convertJStoBSON(query);
        Object rawObj = BSONizer.convertJStoBSON(obj);
        // TODO assert that rawQuery and rawFields are DBObject instances?
        DBObject bsonQuery = null;
        DBObject bsonObj = null;
        if(rawQuery instanceof DBObject)
        	bsonQuery = (DBObject)rawQuery;
        if(rawObj instanceof DBObject)
        	bsonObj = (DBObject)rawObj;
		// TODO some sort of assertion that ns contains a '.'?
		com.mongodb.DB db = innerMongo.getDB(ns.substring(0, ns.indexOf('.')));
		DBCollection collection = db.getCollection(ns.substring(ns.lastIndexOf('.') + 1));
		collection.update(bsonQuery, bsonObj, upsert, false);
	}
	
}
