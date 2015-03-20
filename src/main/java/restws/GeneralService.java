package restws;

import java.util.Date;

import javax.servlet.http.Part;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import connector.MONGODB;
import exception.ExceptionValidation;
import exception.Validator;

@Path("g")
public class GeneralService {
	@POST
	@Path("/app")
	@SuppressWarnings("unchecked")
	public String AppReg(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			String name = inputJson.get("AppName").toString();
			
			if(!IsExistData("_id", name, collApp)) {
				String key = GetKey(collApp);
				collApp.insert(new BasicDBObject("_id", name).append("key", key));
				outputJson.put("code", 1);
				outputJson.put("message", key);
			}
			else {
				outputJson.put("code", 0);
				outputJson.put("message", "Application name already taken. Try another name.");
			}
		}
		catch (Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
		}

		return outputJson.toString();
	}
	
	public static Boolean IsExistData(String key, String value, DBCollection coll) {
		DBObject whereQuery = new BasicDBObject(key, value);
		DBCursor cursor = coll.find(whereQuery);
		Boolean result = false;
		while(cursor.hasNext()) {
			cursor.next();
			result = true;
		}
		cursor.close();
		return result;
	}
	
	public static String getHash(String txt, String hashType) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance(hashType);
			byte[] array = md.digest(txt.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {

		}
		return null;
	}

	public static String md5(String txt) {
		return GeneralService.getHash(txt, "MD5");
	}

	public static void AppkeyCheck(String appKey, DBCollection coll) throws ExceptionValidation {
		if (!GeneralService.IsExistData("key", appKey, coll))
			throw new ExceptionValidation(ExceptionValidation.WRONG_APPKEY);
	}

	public static String TokenCheck(String token, DBCollection collToken) throws ExceptionValidation {
		String output = "";
		Validator.isParameterWrong(token, Validator.TOKEN);
		DBObject tokenObject = collToken.findOne(new BasicDBObject("token", token));
		if (tokenObject == null)
			throw new ExceptionValidation(ExceptionValidation.WRONG_TOKEN);
		else {
			Date valid_date = (Date) tokenObject.get("valid_date");
			if (Service.today.after(valid_date))
				throw new ExceptionValidation(ExceptionValidation.TOKEN_EXPIRED);
			else {
				DBObject queryObject = new BasicDBObject();
				queryObject.put("_id", tokenObject.get("_id"));
				
				DBObject objectToSet = new BasicDBObject();
				objectToSet.put("valid_date", DateUtils.addMonths(Service.today, Service.tokenLength));
				
				DBObject updateObject = new BasicDBObject();
				updateObject.put("$set", objectToSet);
				
				collToken.update(queryObject, updateObject);
				output = tokenObject.get("_id").toString();
			}
		}
		return output;
	}

	public static DBObject GetDBObjectFromId(DBCollection collection, String _id) {
		DBObject queryObject = new BasicDBObject("_id", _id);
		return collection.findOne(queryObject);
	}


	private String GetKey(DBCollection coll) {
		JSONObject objectDB = null;
		String tempKey = "";
		do{
			tempKey = RandomStringUtils.randomAlphanumeric(20);
			objectDB = (JSONObject) coll.findOne(new BasicDBObject("key", tempKey));
		}while(objectDB != null);
		return tempKey;
	}

	public static String GetTokenID(DBCollection coll) {
		JSONObject objectDB = null;
		String tempKey = "";
		do{
			tempKey = RandomStringUtils.randomAlphanumeric(20);
			objectDB = (JSONObject) coll.findOne(new BasicDBObject("token", tempKey));
		}while(objectDB != null);
		return tempKey;
	}

	public static String GetFileID(String fileName, DBCollection coll, String dirPath) {
		JSONObject objectDB = null;
		String tempKey = "";
		do{
			tempKey = RandomStringUtils.randomAlphanumeric(20);
			objectDB = (JSONObject) coll.findOne(new BasicDBObject("_id", tempKey));
		}while(objectDB != null);
		
		DBObject obj = new BasicDBObject()
			.append("_id", tempKey)
			.append("filename", fileName)
			.append("fullpath", dirPath+"/"+tempKey)
			.append("upload_date", Service.today);
		coll.insert(obj);
		return tempKey;
	}

	public static String GetTaskID(DBCollection coll, String username) {
		JSONObject objectDB = null;
		String tempKey = "";
		do{
			tempKey = RandomStringUtils.randomAlphanumeric(5);
			objectDB = (JSONObject) coll.findOne(new BasicDBObject("_id", username).append("task.id_task", tempKey));
		}while(objectDB != null);
		return tempKey;
	}

	public static String GetCommentID(DBCollection coll, String username, String task) {
		JSONObject objectDB = null;
		String tempKey = "";
		do{
			tempKey = RandomStringUtils.randomAlphanumeric(5);
			objectDB = (JSONObject) coll.findOne(new BasicDBObject("_id", username)
				.append("task.id_task", task)
				.append("task.comment.id_comment", tempKey)
				);
		}while(objectDB != null);
		return tempKey;
	}

	public static String GetFileName(Part part) {
		String partHeader = part.getHeader("content-disposition");
		for (String cd : partHeader.split(";")) {
			if (cd.trim().startsWith("filename")) {
				return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}

	public static int GetIndexArray(JSONArray listArray, String keyObject, Object findObject) {
		int index = 0;
		Boolean isFound = false;
		do {
			JSONObject object = (JSONObject) JSONValue.parse(listArray.get(index).toString());
			if(object.get(keyObject).equals(findObject)) isFound = true;
			else if(index == listArray.size()) index = -1;
			else index++;
		} while(index <= listArray.size() && !isFound);
		return index;
	}
}
