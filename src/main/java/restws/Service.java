package restws;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

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

@Path("f")
public class Service {
	public static final int COMMENT_TYPE_SUPERVISOR_INSTRUCT = 11;
	public static final int COMMENT_TYPE_SUPERVISOR_COMMENT = 12;
	public static final int COMMENT_TYPE_SUPERVISOR_CLARIFY = 13;
	public static final int COMMENT_TYPE_STUDENT_COMMENT = 21;
	public static final int COMMENT_TYPE_STUDENT_ASKING = 22;

	public static final String dirAttachment = "/files";
	public static final String dirFinalWork = "/final";
	public static final String today = new Date().toString();
	public static final int tokenLength = 1;
	
	@POST
	@Path("/auth")
	@SuppressWarnings("unchecked")
	public String Authentication(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collSupervisor = db.getCollection("supervisor");

			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			String username = inputJson.get("username").toString();
			String password = inputJson.get("password").toString();

			Validator.isParameterEmpty(username);
			Validator.isParameterEmpty(password);
			Validator.isParameterWrong(username, Validator.USERNAME);
			Validator.isParameterWrong(password, Validator.PASSWORD);
			
			password = GeneralService.md5(password);
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", username);
			queryObject.put("password", password);
			
			DBObject studentObject = collStudent.findOne(queryObject);
			DBObject supervisorObject = collSupervisor.findOne(queryObject);
			Validator.isExist(studentObject, supervisorObject);
			
			if (studentObject != null) {
				outputJson.put("code", 1);
				outputJson.put("message", "Login as Student");
				outputJson.put("token", GetToken(collToken, username));
				outputJson.put("username", username);
			}
			else if (supervisorObject != null) {
				outputJson.put("code", 2);
				outputJson.put("message", "Login as Supervisor");
				outputJson.put("token", GetToken(collToken, username));
				outputJson.put("username", username);
			}
		}
		catch (ExceptionValidation e) {
			outputJson.put("code", 0);
			outputJson.put("message", e.toString());
			outputJson.put("username", null);
		}
		catch (Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
			outputJson.put("username", null);	
		}
		
		return outputJson.toString();
	}

	@POST
	@Path("/isexist")
	@SuppressWarnings("unchecked")
	public String IsUsernameExist(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			String username = inputJson.get("username").toString();
			
			DBObject studentObject = GeneralService.GetDBObjectFromId(collStudent, username);
			DBObject supervisorObject = GeneralService.GetDBObjectFromId(collSupervisor, username);
			Validator.isNotExist(studentObject, supervisorObject);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Username Available");
		}
		catch (ExceptionValidation e) {
			outputJson.put("code", 0);
			outputJson.put("message", "Username Already Exist!");
		}
		catch (Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
		}

		return outputJson.toString();
	}
	
	@GET
	@Path("/getallfield/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String GetAllField(@PathParam("appkey") String appkey, @PathParam("token") String token) 
	{
		JSONObject outputJson = new JSONObject();
		JSONArray data_json = new JSONArray();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collField = db.getCollection("field");
			
			GeneralService.AppkeyCheck(appkey, collApp);
			GeneralService.TokenCheck(token, collToken);
			
			DBCursor cursor = collField.find();
			while (cursor.hasNext()) {
				data_json.add(cursor.next());
			}
			cursor.close();
			
			if (data_json.size() == 0)
			{
				outputJson.put("code", 0);
				outputJson.put("message", "Not Found");
				outputJson.put("data", null);
			}else
			{
				outputJson.put("code", 1);
				outputJson.put("message", "Success");
				outputJson.put("data", data_json);
			}
		} 
		catch (Exception e) 
		{
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
		}

		return outputJson.toString();
	}
	
	@GET
	@Path("/searchfield/{keysearch}/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String SearchField(@PathParam("keysearch") String keySearch, @PathParam("appkey") String appkey,
			@PathParam("token") String token) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collField = db.getCollection("field");
			
			GeneralService.AppkeyCheck(appkey, collApp);
			GeneralService.TokenCheck(token, collToken);

			//keySearch=URLDecoder.decode(keySearch, java.nio.charset.StandardCharsets.UTF_8.toString());
			Validator.isParameterEmpty(keySearch);
			Validator.isParameterWrong(keySearch, Validator.FIELD_NAME);
			
			JSONArray fieldArray = new JSONArray();
			DBObject queryObject = new BasicDBObject("_id",  Pattern.compile(keySearch, Pattern.CASE_INSENSITIVE));
			DBCursor fieldList = collField.find(queryObject);
			Validator.isExist(fieldList, Validator.GENERAL);
			
			while (fieldList.hasNext()) {
				fieldArray.add(fieldList.next());
			}
			fieldList.close();
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
			outputJson.put("data", fieldArray);
		} 
		catch (ExceptionValidation e) {
			outputJson.put("code", 0);
			outputJson.put("message", e.toString());
			outputJson.put("data", null);
		}
		catch (Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
			outputJson.put("data", null);
		}

		return outputJson.toString();
	}
	
	@GET
	@Path("/gettask/{student}/{id_task}/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String GetTask(@PathParam("student") String student, @PathParam("id_task") String taskId,
			@PathParam("appkey") String appKey, @PathParam("token") String token) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			
			GeneralService.AppkeyCheck(appKey, collApp);
			GeneralService.TokenCheck(token, collToken);
			
			Validator.isParameterEmpty(student);
			Validator.isParameterEmpty(taskId);
			Validator.isParameterWrong(student, Validator.USERNAME);
			Validator.isParameterWrong(taskId, Validator.TASK_TEMPLATE_ID);
			
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", student);
			queryObject.put("task.id_task", taskId);
			DBObject projectionObject = new BasicDBObject("task.$", 1);
			DBObject studentObject = collStudent.findOne(queryObject, projectionObject);
			
			Validator.isExist(studentObject, Validator.TASK_ID);
			
			JSONObject taskObject = (JSONObject) ((JSONArray) JSONValue.parse(studentObject.get("task").toString())).get(0);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
			outputJson.put("data", taskObject);
		}
		catch (ExceptionValidation e) {
			outputJson.put("code", 0);
			outputJson.put("message", e.toString());
			outputJson.put("data", null);
		}
		catch (Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
			outputJson.put("data", null);
		}

		return outputJson.toString();
	}
	
	@POST
	@Path("/editcomment")
	@Deprecated
	@SuppressWarnings("unchecked")
	public String EditComment(String jsonString) 
	{		
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collStudent = db.getCollection("student");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			int studentId = Integer.parseInt(inputJson.get("student").toString());
			int taskId = Integer.parseInt(inputJson.get("id_task").toString());
			int commentId = Integer.parseInt(inputJson.get("id_comment").toString());
			String type = inputJson.get("type").toString();
			String text = inputJson.get("text").toString();
			
			DBObject query = new BasicDBObject("_id", studentId)
				.append("task.id_task", taskId);
			DBObject findOne = collStudent.findOne(query);
			if(findOne != null){				
				DBObject queryObject	= new BasicDBObject("_id", studentId)
					.append("task.id_task", taskId)
					.append("task.comment.id_comment", commentId);
				DBCursor cursor = collStudent.find(queryObject);
				DBObject student = cursor.next();
				cursor.close();
				JSONArray tasks = (JSONArray) JSONValue.parse(student.get("task").toString());
				JSONArray comments = GetComment(tasks, taskId);
				int index = GetIndexComment(comments, commentId);
				if(index != -1){
					DBObject objectToSet= new BasicDBObject("task.$.comment."+index+".text", text)
													.append("task.$.comment."+index+".type", type);
					DBObject objectSet= new BasicDBObject("$set", objectToSet);
					collStudent.update(queryObject, objectSet, false, true);
				}else{
					outputJson.put("code", 0);
					outputJson.put("message", "Parameter value was fault");
				}
				
				outputJson.put("code", 1);
				outputJson.put("message", "Success");
			}else{
				outputJson.put("code", 0);
				outputJson.put("message", "Parameter value was fault");
			}
		} 
		catch (Exception e) 
		{
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
		}

		return outputJson.toString();
	}

	@POST
	@Path("/deletecomment")
	@SuppressWarnings("unchecked")
	public String DeleteComment(String jsonString) 
	{		
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collFile = db.getCollection("file");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			String username = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String student = inputJson.get("student").toString();
			String taskId = inputJson.get("id_task").toString();
			String commentId = inputJson.get("id_comment").toString();
			
			DBObject query = new BasicDBObject("_id", student)
				.append("task.id_task", taskId)
				.append("task.comment.id_comment", commentId);
			DBObject findOne = collStudent.findOne(query, new BasicDBObject("task.$", 1));
			
			JSONArray taskArray = (JSONArray) JSONValue.parse(findOne.get("task").toString());
			JSONObject taskObject = (JSONObject) taskArray.get(0);
			JSONArray commentArray = (JSONArray) taskObject.get("comment");
			JSONObject commentObject = (JSONObject) commentArray
					.get(GeneralService.GetIndexArray(commentArray, "id_comment", commentId));
			String commentedBy = commentObject.get("by").toString();
			JSONArray files = (JSONArray) commentObject.get("file");
			
			if(!findOne.equals(null)) {
				if(username.equals(commentedBy)) {
					for(int i = 0; i < files.size(); i++) {
						JSONObject file = (JSONObject) files.get(i);
						String fileId = file.get("fileid").toString();
						DBObject objectFind = new BasicDBObject("_id", fileId);
						DBObject findFile = GeneralService.GetDBObjectFromId(collFile, fileId);
						if(findFile.equals(null)) throw new Exception("File not found");
						String fullPath = findFile.get("fullpath").toString();
						
						File fileToRemove = new File(fullPath);
						Files.deleteIfExists(fileToRemove.toPath());
						collFile.remove(objectFind);
					}
					DBObject objectId = new BasicDBObject("_id", student)
						.append("task.id_task", taskId);
					DBObject objectToSet= new BasicDBObject("task.$.comment", new BasicDBObject("id_comment", commentId));
					DBObject objectSet= new BasicDBObject("$pull", objectToSet);
					collStudent.update(objectId, objectSet);
				
					outputJson.put("code", 1);
					outputJson.put("message", "Success");
				}
				else {
					outputJson.put("code", 0);
					outputJson.put("message", "False Comment");
				}
			}
			else {
				outputJson.put("code", 0);
				outputJson.put("message", "Cannot Find Comment");
			}
		} 
		catch (Exception e) 
		{
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
		}

		return outputJson.toString();
	}

	@POST
	@Path("/resetpassword")
	@SuppressWarnings("unchecked")
	public String ResetPassword(String jsonString) {
		JSONObject outputJson = new JSONObject();
		DB db = null;
		try {
			db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initial Parameter
			String username = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String user = GetUser(username, collStudent, collSupervisor);
			String oldPassword = inputJson.get("oldpassword").toString();
			String newPassword = inputJson.get("newpassword").toString();
			
			DBObject objectId = new BasicDBObject("_id", username);
			DBObject objectToBeSet = new BasicDBObject("password", GeneralService.md5(newPassword));
			DBObject objectSet = new BasicDBObject("$set", objectToBeSet);
			
			if(user.equals("student")) {
				DBObject studentObject = collStudent.findOne(objectId);
				if(studentObject.get("password").equals(GeneralService.md5(oldPassword))) {
					collStudent.update(objectId, objectSet);

					outputJson.put("code", 1);
					outputJson.put("message", "Success");
				}
				else {
					outputJson.put("code", 0);
					outputJson.put("message", "Wrong Password");
				}
			}
			else if(user.equals("supervisor")) {
				DBObject supervisorObject = collSupervisor.findOne(objectId);
				if(supervisorObject.get("password").equals(GeneralService.md5(oldPassword))) {
					collSupervisor.update(objectId, objectSet);

					outputJson.put("code", 1);
					outputJson.put("message", "Success");
				}
				else {
					outputJson.put("code", 0);
					outputJson.put("message", "Wrong Password");
				}
			}
			else {
				outputJson.put("code", 0);
				outputJson.put("message", "Cannot Find User");
			}
		} catch (Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
		}

		return outputJson.toString();
	}

	@GET
	@Path("/search/{keysearch}/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String SearchSupervisor(@PathParam("keysearch") String key, @PathParam("appkey") String appkey,
			@PathParam("token") String token) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			GeneralService.AppkeyCheck(appkey, collApp);
			GeneralService.TokenCheck(token, collToken);

			Validator.isParameterEmpty(key);
			Validator.isParameterWrong(key, Validator.FIELD_NAME);
			
			JSONArray supervisorArray = new JSONArray();
			DBObject queryObject = new BasicDBObject("field", Pattern.compile(key));
			DBCursor supervisorList = collSupervisor.find(queryObject);
			Validator.isExist(supervisorList, Validator.GENERAL);
			
			while (supervisorList.hasNext()) {
				supervisorArray.add(supervisorList.next());
			}
			supervisorList.close();
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
			outputJson.put("data", supervisorArray);
		}
		catch (ExceptionValidation e) {
			outputJson.put("code", 0);
			outputJson.put("message", e.toString());
			outputJson.put("data", null);
		}
		catch (Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
			outputJson.put("data", null);
		}

		return outputJson.toString();
	}

	@GET
	@Path("/getgraduated/{supervisor}/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String GetGraduateStudent(@PathParam("supervisor") String supervisor, @PathParam("appkey") String appkey,
			@PathParam("token") String token) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			GeneralService.AppkeyCheck(appkey, collApp);
			GeneralService.TokenCheck(token, collToken);
			
			Validator.isParameterEmpty(supervisor);
			Validator.isParameterWrong(supervisor, Validator.USERNAME);
			
			DBObject supervisorObject = GeneralService.GetDBObjectFromId(collSupervisor, supervisor);
			JSONArray studentGraduateArray = (JSONArray) JSONValue.parse(supervisorObject.get("graduate").toString());
			JSONArray graduateArray = new JSONArray();
			
			DBObject queryObject = new BasicDBObject("_id", new BasicDBObject("$in", studentGraduateArray));
			DBCursor studentList = collStudent.find(queryObject);
			Validator.isExist(studentList, Validator.GENERAL);
			
			while (studentList.hasNext()) {
				JSONObject studentObject = (JSONObject) JSONValue.parse(studentList.next().toString());
				graduateArray.add(studentObject);
			}
			studentList.close();
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
			outputJson.put("data", graduateArray);
		}
		catch (ExceptionValidation e) {
			outputJson.put("code", 0);
			outputJson.put("message", e.toString());
			outputJson.put("data", null);
		}
		catch (Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
			outputJson.put("data", null);
		}

		return outputJson.toString();
	}

	@GET
	@Path("/getungraduated/{supervisor}/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String GetUngraduateStudent(@PathParam("supervisor") String supervisor, @PathParam("appkey") String appkey,
			@PathParam("token") String token) {		
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			GeneralService.AppkeyCheck(appkey, collApp);
			GeneralService.TokenCheck(token, collToken);
			
			Validator.isParameterEmpty(supervisor);
			Validator.isParameterWrong(supervisor, Validator.USERNAME);
			
			DBObject supervisorObject = GeneralService.GetDBObjectFromId(collSupervisor, supervisor);
			JSONArray studentUngraduateArray = (JSONArray) JSONValue.parse(supervisorObject.get("student").toString());
			JSONArray ungraduateArray = new JSONArray();
			
			DBObject queryObject = new BasicDBObject("_id", new BasicDBObject("$in", studentUngraduateArray));
			DBCursor studentList = collStudent.find(queryObject);
			Validator.isExist(studentList, Validator.GENERAL);
			
			while (studentList.hasNext()) {
				JSONObject studentObject = (JSONObject) JSONValue.parse(studentList.next().toString());
				ungraduateArray.add(studentObject);
			}
			studentList.close();
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
			outputJson.put("data", ungraduateArray);
		}
		catch (ExceptionValidation e) {
			outputJson.put("code", 0);
			outputJson.put("message", e.toString());
			outputJson.put("data", null);
		}
		catch (Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
		}

		return outputJson.toString();
	}

	@GET
	@Path("/getusername/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String GetUsername(@PathParam("appkey") String appkey, @PathParam("token") String token) {
		JSONObject outputJson = new JSONObject();
		JSONObject outputData = new JSONObject();
		DB db = null;
		try {
			db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collSupervisor = db.getCollection("supervisor");

			GeneralService.AppkeyCheck(appkey, collApp);
			
			// Initial Parameter
			String username = GeneralService.TokenCheck(token, collToken);
			String user = GetUser(username, collStudent, collSupervisor);
			DBObject query = new BasicDBObject("_id", username);
			isUserExist(user);
			
			if(user.equals("student")) {
				DBObject studentObject = collStudent.findOne(query);
				outputData.put("username", username);
				outputData.put("status", "student");
				outputData.put("name", studentObject.get("name").toString());

				outputJson.put("code", 1);
				outputJson.put("message", "Success");
				outputJson.put("data", outputData);
			}
			else if(user.equals("supervisor")) {
				DBObject supervisorObject = collSupervisor.findOne(query);
				outputData.put("username", username);
				outputData.put("status", "supervisor");
				outputData.put("name", supervisorObject.get("name").toString());

				outputJson.put("code", 1);
				outputJson.put("message", "Success");
				outputJson.put("data", outputData);
			}
		}
		catch(ExceptionValidation e) {
			outputJson.put("code", 0);
			outputJson.put("message", e.toString());
		}
		catch(Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
		}
		return outputJson.toString();
	}
	
	@POST
	@Path("/createreference")
	@SuppressWarnings("unchecked")
	public String CreateReference(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collReference = db.getCollection("references");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			String addedby = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken); 
			String author = inputJson.get("author").toString();
			String title = inputJson.get("title").toString();
			String year = inputJson.get("year").toString();
			String abstr = inputJson.get("abstract").toString();
			JSONArray keywords = (JSONArray) JSONValue.parse(inputJson.get("keywords").toString());
			
			Validator.isParameterEmpty(author);
			Validator.isParameterEmpty(title);
			Validator.isParameterEmpty(year);
			Validator.isParameterEmpty(keywords);
			
			Validator.isParameterWrong(title, Validator.THESIS_TITLE);
			Validator.isParameterWrong(year, Validator.YEAR);
			
			DBObject insertObject = new BasicDBObject();
			String id=RandomStringUtils.randomAlphanumeric(20);
			insertObject.put("_id", id);
			insertObject.put("added_by", addedby);
			insertObject.put("author", author);
			insertObject.put("title", title);
			insertObject.put("year", year);
			insertObject.put("abstract", abstr);
			insertObject.put("keywords", keywords);
			collReference.insert(insertObject);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
			outputJson.put("data", id);
		}
		catch (ExceptionValidation e) {
			outputJson.put("code", 0);
			outputJson.put("message", e.toString());
		}
		catch (Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
		}

		return outputJson.toString();
	}
	
	@GET
	@Path("/getallreference/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String GetAllReference(@PathParam("appkey") String appkey, @PathParam("token") String token) 
	{
		JSONObject outputJson = new JSONObject();
		JSONArray data_json = new JSONArray();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collReference = db.getCollection("references");
			
			GeneralService.AppkeyCheck(appkey, collApp);
			String username = GeneralService.TokenCheck(token, collToken);
			
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", username);
			DBObject studentObject = collStudent.findOne(queryObject);
			DBCursor cursor=null;
			if(studentObject==null||studentObject.get("references")==null)
				cursor = collReference.find();
			else
				cursor = collReference.find(new BasicDBObject("_id",  new BasicDBObject("$nin", studentObject.get("references"))));
			DBObject reference=null;
			while (cursor.hasNext()) {
				reference=cursor.next();
				queryObject=new BasicDBObject("references", reference.get("_id"));
				reference.put("count", collStudent.count(queryObject));
				data_json.add(reference);
			}
			cursor.close();
			
			if (data_json.size() == 0)
			{
				outputJson.put("code", 0);
				outputJson.put("message", "Not Found");
				outputJson.put("data", null);
			}else
			{
				outputJson.put("code", 1);
				outputJson.put("message", "Success");
				outputJson.put("data", data_json);
			}
		} 
		catch (Exception e) 
		{
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
		}

		return outputJson.toString();
	}
	
	@GET
	@Path("/searchreference/{keysearch}/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String SearchReference(@PathParam("keysearch") String keySearch, @PathParam("appkey") String appkey,
			@PathParam("token") String token) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collReference = db.getCollection("references");
			
			GeneralService.AppkeyCheck(appkey, collApp);
			String username = GeneralService.TokenCheck(token, collToken);

			Validator.isParameterEmpty(keySearch);
			Validator.isParameterWrong(keySearch, Validator.FIELD_NAME);
			//keySearch=URLDecoder.decode(keySearch, java.nio.charset.StandardCharsets.UTF_8.toString());
			
			JSONArray referenceArray = new JSONArray();
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", username);
			DBObject studentObject = collStudent.findOne(queryObject);
			DBCursor referenceList=null;
			queryObject = new BasicDBObject("title",  Pattern.compile(Pattern.quote(keySearch), Pattern.CASE_INSENSITIVE));
			if(!(studentObject==null||studentObject.get("references")==null))
				queryObject.put("_id", new BasicDBObject("$nin", studentObject.get("references")));
			referenceList = collReference.find(queryObject);
			Validator.isExist(referenceList, Validator.GENERAL);
			DBObject reference=null;
			while (referenceList.hasNext()) {
				reference=referenceList.next();
				queryObject=new BasicDBObject("references", reference.get("_id"));
				reference.put("count", collStudent.count(queryObject));
				referenceArray.add(reference);
			}
			referenceList.close();
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
			outputJson.put("data", referenceArray);
		} 
		catch (ExceptionValidation e) {
			outputJson.put("code", 0);
			outputJson.put("message", e.toString());
			outputJson.put("data", null);
		}
		catch (Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
			outputJson.put("data", null);
		}

		return outputJson.toString();
	}
	
	@GET
	@Path("/getreference/{refid}/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String GetReference(@PathParam("refid") String refId, @PathParam("appkey") String appkey,
			@PathParam("token") String token) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collReference = db.getCollection("references");
			
			GeneralService.AppkeyCheck(appkey, collApp);
			GeneralService.TokenCheck(token, collToken);

			Validator.isParameterEmpty(refId);
			
			DBObject queryObject = new BasicDBObject("_id",  refId);
			DBObject reference = collReference.findOne(queryObject);
			Validator.isExist(reference, Validator.TASK_ID);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
			outputJson.put("data", reference);
		} 
		catch (ExceptionValidation e) {
			outputJson.put("code", 0);
			outputJson.put("message", e.toString());
			outputJson.put("data", null);
		}
		catch (Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
			outputJson.put("data", null);
		}

		return outputJson.toString();
	}
	
	@GET
	@Path("/download")
	@SuppressWarnings("unchecked")
	public Response DownloadFile(@Context HttpServletRequest request){
	StreamingOutput stream = null;
		File file = null;
		JSONObject outputJson = new JSONObject();
		try {
			String fileId = request.getParameter("id");
			
			DB db = MONGODB.GetMongoDB();
			DBCollection collFile = db.getCollection("file");
			DBObject fileObject = GeneralService.GetDBObjectFromId(collFile, fileId);
			String filename = fileObject.get("filename").toString();
			isDataExist(fileObject);
			String filePath = fileObject.get("fullpath").toString();
			
			file = new File(filePath);
			isFileExist(file);
			try {
				final InputStream in = new FileInputStream(file);
				stream = new StreamingOutput() {
					public void write(OutputStream out) throws IOException, WebApplicationException {
						try {
							int read = 0;
							byte[] bytes = new byte[1024];

							while ((read = in.read(bytes)) != -1) {
								out.write(bytes, 0, read);
							}
						}
						catch (Exception e) {
							throw new WebApplicationException(e);
						}
					}
				};
			}
			catch (FileNotFoundException e) {
				throw new Exception(e.toString());
			}
			return Response.ok(stream).header("content-disposition", "attachment; filename = "+filename).build();
		}
		catch (ExceptionValidation e) {
			outputJson.put("code", 0);
			outputJson.put("message", e.toString());
			return Response.ok(outputJson.toString()).build();
		}
		catch (Exception e) {
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
			return Response.ok(outputJson.toString()).build();
		}
	}

	private JSONArray GetComment(JSONArray tasks, int taskID) {
		JSONArray comments = null;
		int i = 0;
		Boolean find = false;
		while(i<tasks.size() && !find){
			JSONObject task = (JSONObject) JSONValue.parse(tasks.get(i).toString());
			if(Integer.parseInt(task.get("id_task").toString()) == taskID){
				find = true;
				comments = (JSONArray) JSONValue.parse(task.get("comment").toString());
			}else
				i++;
		}
		return comments;
	}
	
	private int GetIndexComment(JSONArray comment, int commentID) {
		int i = 0;
		Boolean find = false;
		while(i<comment.size() && !find){
			JSONObject task = (JSONObject) JSONValue.parse(comment.get(i).toString());
			if(Integer.parseInt(task.get("id_comment").toString()) == commentID){
				find = true;
			}else
				i++;
		}
		
		if(i == comment.size()) i = -1;
		return i;
	}
	
	private String GetToken(DBCollection collToken, String username) throws ParseException {
		String token = "";
		DBObject tokenObject = GeneralService.GetDBObjectFromId(collToken, username);
		String oneMonth = DateUtils.addMonths(GeneralService.StringtoDate(today), tokenLength).toString();
		if (tokenObject == null) {
			token = GeneralService.GetTokenID(collToken);
			
			DBObject insertObject = new BasicDBObject();
			insertObject.put("_id", username);
			insertObject.put("token", token);
			insertObject.put("valid_date", oneMonth);
			
			collToken.insert(insertObject);
		}
		else {
			Date validDate = GeneralService.StringtoDate((String) tokenObject.get("valid_date"));
			if (GeneralService.StringtoDate(today).before(validDate)) {
				token = tokenObject.get("token").toString();
				DBObject queryObject = new BasicDBObject("_id", username);
				DBObject objectToSet = new BasicDBObject("valid_date", oneMonth);
				DBObject updateObject = new BasicDBObject("$set", objectToSet);
				collToken.update(queryObject, updateObject);
			}
			else {
				token = GeneralService.GetTokenID(collToken);
				DBObject queryObject = new BasicDBObject("_id", username);
				
				DBObject objectToSet = new BasicDBObject();
				objectToSet.put("token", token);
				objectToSet.put("valid_date", oneMonth);
				
				DBObject updateObject = new BasicDBObject("$set", objectToSet);
				collToken.update(queryObject, updateObject);
			}
		}
		return token;
	}

	private String GetUser(String username, DBCollection collStudent, DBCollection collSupervisor) {
		DBObject objectFind = new BasicDBObject("_id", username);
		DBObject studentObject = collStudent.findOne(objectFind);
		DBObject supervisorObject = collSupervisor.findOne(objectFind);
		
		if (studentObject != null)
			return "student";
		else if (supervisorObject != null)
			return "supervisor";
		else
			return "";
	}
	
	private void isDataExist(DBObject data) throws ExceptionValidation {
		if (data == null)
			throw new ExceptionValidation(ExceptionValidation.NOT_EXIST);
	}
	
	private void isFileExist(File file) throws ExceptionValidation {
		if (Files.notExists(file.toPath()))
			throw new ExceptionValidation(ExceptionValidation.FILE_NOT_EXIST);
	}

	private void isUserExist(String user) throws ExceptionValidation {
		if (user == null)
			throw new ExceptionValidation(ExceptionValidation.USER_NOT_EXIST);
	}
}
