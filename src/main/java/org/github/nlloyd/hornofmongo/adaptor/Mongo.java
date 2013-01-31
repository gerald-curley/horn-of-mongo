package org.github.nlloyd.hornofmongo.adaptor;

import java.net.UnknownHostException;

import org.github.nlloyd.hornofmongo.util.BSONizer;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;

import com.mongodb.DBCollection;

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
		return "Mongo";
	}
	
	// --- Mongo JavaScript function implementation ---
	
//if ( ! Mongo.prototype.find )
//    Mongo.prototype.find = function( ns , query , fields , limit , skip , batchSize , options ){ throw "find not implemented"; }
//if ( ! Mongo.prototype.insert )
//    Mongo.prototype.insert = function( ns , obj ){ throw "insert not implemented"; }
//if ( ! Mongo.prototype.remove )
//    Mongo.prototype.remove = function( ns , pattern ){ throw "remove not implemented;" }
//if ( ! Mongo.prototype.update )
//    Mongo.prototype.update = function( ns , query , obj , upsert ){ throw "update not implemented;" }
	
	@JSFunction
	public Object find(final String ns , final Object query , final Object fields , int limit , int skip , int batchSize , int options) {
		System.out.printf("find(%s, %s, %s, %d, %d, %d, %d)\n", ns, query, fields, limit, skip, batchSize, options);

        BSONizer.convertJStoBSON(((Scriptable)query));
		String[] nsBits = ns.split(".");
		// TODO some sort of assertion that nsBits.length == 2?
		com.mongodb.DB db = innerMongo.getDB(nsBits[0]);
		DBCollection collection = db.getCollection(nsBits[1]);
		
		return null;
	}
	
	@JSFunction
	public void insert(final String ns, Object query) {
		String str = query.toString();
		System.out.printf("insert(%s, %s)\n", ns, str);
		BSONizer.convertJStoBSON(((Scriptable)query));
	}
	
	@JSFunction
	public void remove(final String ns, Object pattern) {
		System.out.printf("remove(%s, %s)\n", ns, pattern);
	}
	
	@JSFunction
	public void update(final String ns, Object query, Object obj, boolean upsert) {
		System.out.printf("update(%s, %s, %s, %b)\n", ns, query, obj, upsert);
	}
	
}