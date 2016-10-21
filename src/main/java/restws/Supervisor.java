package restws;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import connector.MONGODB;
import exception.ExceptionValidation;
import exception.Validator;

@Path("su")
public class Supervisor {
	@POST
	@Path("/register")
	@SuppressWarnings("unchecked")
	public String RegisterSupervisor(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			String username = inputJson.get("username").toString();
			String password = inputJson.get("password").toString();
			String npp = inputJson.get("npp").toString();
			String name = inputJson.get("name").toString();
			String address = inputJson.get("address").toString();
			String handphone = inputJson.get("handphone").toString();
			String email = inputJson.get("email").toString();
			
			Validator.isParameterEmpty(username);
			Validator.isParameterEmpty(password);
			Validator.isParameterEmpty(npp);
			Validator.isParameterEmpty(name);

			Validator.isParameterWrong(username, Validator.USERNAME);
			Validator.isParameterWrong(password, Validator.PASSWORD);
			Validator.isParameterWrong(npp, Validator.NPP);
			Validator.isParameterWrong(name, Validator.NAME_PERSON);
			Validator.isParameterWrong(address, Validator.ADDRESS);
			Validator.isParameterWrong(handphone, Validator.PHONE_NUMBER);
			Validator.isParameterWrong(email, Validator.EMAIL);
			
			DBObject insertObject = new BasicDBObject();
			insertObject.put("_id", username);
			insertObject.put("password", GeneralService.md5(password));
			insertObject.put("npp", npp);
			insertObject.put("name", name);
			insertObject.put("address", address);
			insertObject.put("handphone", handphone);
			insertObject.put("email", email);
			
			insertObject.put("graduate", new JSONArray());
			insertObject.put("student", new JSONArray());
			insertObject.put("field", new JSONArray());
			insertObject.put("template", new JSONArray());
			insertObject.put("proposal", new JSONArray());
			insertObject.put("claim", new JSONArray());
			
			collSupervisor.insert(insertObject);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Registrasi Sukses");
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
	@Path("/get/{username}/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String GetSupervisor(@PathParam("username") String username, @PathParam("appkey") String appkey,
			@PathParam("token") String token) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			GeneralService.AppkeyCheck(appkey, collApp);
			GeneralService.TokenCheck(token, collToken);
			
			Validator.isParameterEmpty(username);
			Validator.isParameterWrong(username, Validator.USERNAME);
			
			DBObject queryObject = new BasicDBObject("_id", username);
			DBObject supervisorObject = collSupervisor.findOne(queryObject);
			
			Validator.isExist(supervisorObject, Validator.GENERAL);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
			outputJson.put("data", supervisorObject);
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
	@Path("/getall/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String GetAllSupervisor(@PathParam("appkey") String appkey, @PathParam("token") String token) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			GeneralService.AppkeyCheck(appkey, collApp);
			GeneralService.TokenCheck(token, collToken);
			
			JSONArray supervisorArray = new JSONArray();
			DBCursor supervisorList = collSupervisor.find();
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
	
	@POST
	@Path("/issupervisor")
	@SuppressWarnings("unchecked")
	public String IsSupervisor(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);

			String supervisor = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String student = inputJson.get("student").toString();
			
			Validator.isParameterEmpty(student);
			Validator.isParameterWrong(student, Validator.USERNAME);
			
			DBObject supervisorObject = GeneralService.GetDBObjectFromId(collSupervisor, supervisor);
			JSONArray studentArray = (JSONArray) JSONValue.parse(supervisorObject.get("student").toString());
			if (studentArray.contains(student)) {
				outputJson.put("code", 2);
				outputJson.put("message", "true");
			}
			else {
				outputJson.put("code", 1);
				outputJson.put("message", "false");
			}			
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
	
	@POST
	@Path("/response")
	@SuppressWarnings("unchecked")
	public String ResponseProposal(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			String student = inputJson.get("student").toString();
			String supervisor = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String stringCode = inputJson.get("code").toString();
			
			Validator.isParameterEmpty(student);
			Validator.isParameterEmpty(stringCode);
			
			int code = Integer.parseInt(stringCode);
			Validator.isParameterWrong(student, Validator.USERNAME);
			Validator.isParameterWrong(code, Validator.RESPONSE);
			
			DBObject studentObject = GeneralService.GetDBObjectFromId(collStudent, student);
			Validator.isExist(studentObject, Validator.GENERAL);
			int status = (int) studentObject.get("status");
			Validator.isStudentStatus(status, Validator.STUDENT_STATUS_PROPOSE);
			
			if(code == 1) {
				UpdateProposeStudent(collStudent, student, supervisor);
				UpdateProposeSupervisor(collSupervisor, student, supervisor);
			}
			else if(code == 0)
				RejectProposal(collStudent, collSupervisor, student, supervisor);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
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

	@POST
	@Path("/createtemplate")
	@SuppressWarnings("unchecked")
	public String CreateTemplate(String jsonString) {		
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			String username = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String name = inputJson.get("name").toString();
			String description = inputJson.get("description").toString();
			JSONArray task = new JSONArray();
			
			Validator.isParameterEmpty(name);
			Validator.isParameterWrong(name, Validator.TASK_TEMPLATE_NAME);
			Validator.isParameterWrong(description, Validator.TASK_TEMPLATE_DESCRIPTION);
			
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", username);
			
			DBObject templateObject = new BasicDBObject();
			templateObject.put("name", name);
			templateObject.put("code", GetTemplateCode(username, collSupervisor));
			templateObject.put("description", description);
			templateObject.put("task", task);
			
			DBObject objectToPush = new BasicDBObject();
			objectToPush.put("template", templateObject);
			
			DBObject updateObject = new BasicDBObject();
			updateObject.put("$push", objectToPush);
			
			collSupervisor.update(queryObject, updateObject);
				
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
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
	
	@POST
	@Path("/updatetemplate")
	@SuppressWarnings("unchecked")
	public String UpdateTemplate(String jsonString) {		
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			String username = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String code = inputJson.get("code").toString();
			String name = inputJson.get("name").toString();
			String description = inputJson.get("description").toString();
			
			Validator.isParameterEmpty(code);
			Validator.isParameterEmpty(name);
			
			Validator.isParameterWrong(code, Validator.TASK_TEMPLATE_ID);
			Validator.isParameterWrong(name, Validator.TASK_TEMPLATE_NAME);
			Validator.isParameterWrong(description, Validator.TASK_TEMPLATE_DESCRIPTION);
			
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", username);
			queryObject.put("template.code", code);
			
			DBObject supervisorObject = collSupervisor.findOne(queryObject);
			Validator.isExist(supervisorObject, Validator.TEMPLATE_ID);
			
			DBObject objectToSet = new BasicDBObject();
			objectToSet.put("template.$.name", name);
			objectToSet.put("template.$.description", description);
			
			DBObject updateObject = new BasicDBObject();
			updateObject.put("$set", objectToSet);
			
			collSupervisor.update(queryObject, updateObject);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
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
	
	@POST
	@Path("/deletetemplate")
	@SuppressWarnings("unchecked")
	public String DeleteTemplate(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			String username = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String code = inputJson.get("code").toString();
			
			Validator.isParameterEmpty(code);
			Validator.isParameterWrong(code, Validator.TASK_TEMPLATE_ID);
			
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", username);
			queryObject.put("template.code", code);
			
			DBObject supervisorObject = collSupervisor.findOne(queryObject);
			Validator.isExist(supervisorObject, Validator.TEMPLATE_ID);
			
			queryObject.removeField("template.code");
			
			DBObject objectToPull = new BasicDBObject();
			objectToPull.put("template", new BasicDBObject("code", code));
			
			DBObject updateObject = new BasicDBObject();
			updateObject.put("$pull", objectToPull);
			
			collSupervisor.update(queryObject, updateObject);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
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
	
	@POST
	@Path("/removetask")
	@SuppressWarnings("unchecked")
	public String TemplateRemoveTask(String jsonString) 
	{		
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			String username = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String templateCode = inputJson.get("template").toString();
			String taskName = inputJson.get("task").toString();
			
			DBObject objectId = new BasicDBObject("_id", username).append("template.code", templateCode);
			DBObject objectToRemove = new BasicDBObject("template.$.task", new BasicDBObject("name", taskName));
			DBObject queryRemove = new BasicDBObject("$pull", objectToRemove);
			collSupervisor.update(objectId, queryRemove);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
		} 
		catch (Exception e) 
		{
			outputJson.put("code", -1);
			outputJson.put("message", e.toString());
		}

		return outputJson.toString();
	}
	
	@POST
	@Path("/validation")
	@SuppressWarnings("unchecked")
	public String TaskValidation(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			String supervisor = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String student = inputJson.get("student").toString();
			String taskId = inputJson.get("id_task").toString();
			
			Validator.isParameterEmpty(student);
			Validator.isParameterEmpty(taskId);
			Validator.isParameterWrong(student, Validator.USERNAME);
			Validator.isParameterWrong(taskId, Validator.TASK_TEMPLATE_ID);
			
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", student);
			queryObject.put("supervisor", supervisor);
			queryObject.put("task.id_task", taskId);
			
			DBObject projectionObject = new BasicDBObject("task.$", 1);
			DBObject studentObject = collStudent.findOne(queryObject, projectionObject);
			Validator.isExist(studentObject, Validator.TASK_ID);

			JSONArray taskArray = (JSONArray) JSONValue.parse(studentObject.get("task").toString());
			JSONObject taskObject = (JSONObject) taskArray.get(0);
			int status = (int) (long) taskObject.get("status");
			UpdateTaskStatus(collStudent, student, taskId, status);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
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
	@Path("/getstudentprogress/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String GetAllProgress(@PathParam("appkey") String appkey, @PathParam("token") String token) {		
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			GeneralService.AppkeyCheck(appkey, collApp);
			
			String supervisor = GeneralService.TokenCheck(token, collToken);
			DBObject supervisorObject = GeneralService.GetDBObjectFromId(collSupervisor, supervisor);
			JSONArray student = (JSONArray) JSONValue.parse(supervisorObject.get("student").toString());
			JSONArray progressArray = new JSONArray();
			
			DBCursor studentList = collStudent.find(new BasicDBObject("_id", new BasicDBObject("$in", student)));
			Validator.isExist(studentList, Validator.GENERAL);
			
			while (studentList.hasNext()) {
				DBObject studentObject = studentList.next();
				
				DBObject objectProgress = new BasicDBObject();
				objectProgress.put("username", studentObject.get("_id").toString());
				objectProgress.put("name", studentObject.get("name").toString());
				objectProgress.put("nim", studentObject.get("nim").toString());
				objectProgress.put("thesis", studentObject.get("thesis"));
				objectProgress.put("progress",
						new BasicDBObject("undonetasks", CountTask(0, studentObject.get("_id").toString(), collStudent))
									.append("donetasks", CountTask(1, studentObject.get("_id").toString(), collStudent)));
				
				progressArray.add(objectProgress);
			}
			studentList.close();
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
			outputJson.put("data", progressArray);
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
	@Path("/editprofile")
	@SuppressWarnings("unchecked")
	public String EditSupervisorProfile(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collSupervisor = db.getCollection("supervisor");
			DBCollection collField = db.getCollection("field");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			String username = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String address = inputJson.get("address").toString();
			String handphone = inputJson.get("handphone").toString();
			String email = inputJson.get("email").toString();
			JSONArray field = (JSONArray) JSONValue.parse(inputJson.get("field").toString());

			Validator.isParameterEmpty(field);
			Validator.isParameterWrong(address, Validator.ADDRESS);
			Validator.isParameterWrong(handphone, Validator.PHONE_NUMBER);
			Validator.isParameterWrong(email, Validator.EMAIL);
			Validator.isParameterWrong(collField, field);
			
			DBObject queryObject = new BasicDBObject("_id", username);
			DBObject objectToSet = new BasicDBObject();
			objectToSet.put("address", address);
			objectToSet.put("handphone", handphone);
			objectToSet.put("email", email);
			objectToSet.put("field", field);
			
			DBObject updateObject = new BasicDBObject("$set", objectToSet);
			collSupervisor.update(queryObject, updateObject);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
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
	
	@POST
	@Path("/deletetask")
	@SuppressWarnings("unchecked")
	public String DeleteTask(String jsonString) 
	{		
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			String student = inputJson.get("student").toString();
			String supervisor = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String taskId = inputJson.get("id_task").toString();
			
			if(GeneralService.IsExistData("_id", supervisor, collSupervisor)) {
				DBObject query = new BasicDBObject();
				query.put("_id", supervisor);
				query.put("student", student);
				DBObject isStudent = collSupervisor.findOne(query);
				
				if(isStudent != null) {
					DBObject objectId = new BasicDBObject("_id", student);
					DBObject objectToSet = new BasicDBObject("task", new BasicDBObject("id_task", taskId));
					DBObject objectSet = new BasicDBObject("$pull", objectToSet);
					collStudent.update(objectId, objectSet);
					
					outputJson.put("code", 1);
					outputJson.put("message", "Success");
				}
				else {
					outputJson.put("code", 0);
					outputJson.put("message", student+" is not Student of "+supervisor);
				}
			}
			else {
				outputJson.put("code", -1);
				outputJson.put("message", supervisor + " is not Supervisor");
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
	@Path("/savefield")
	@SuppressWarnings("unchecked")
	public String SaveField(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collSupervisor = db.getCollection("supervisor");
			DBCollection collField = db.getCollection("field");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			String username = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String name = inputJson.get("name").toString();
			String description = inputJson.get("description").toString();
			
			Validator.isParameterEmpty(name);
			Validator.isParameterWrong(name, Validator.FIELD_NAME);
			Validator.isParameterWrong(description, Validator.FIELD_DESCRIPTION);
			
			DBObject supervisorObject = GeneralService.GetDBObjectFromId(collSupervisor, username);
			Validator.isExist(supervisorObject, Validator.USER_SUPERVISOR);
			
			if(!GeneralService.IsExistData("_id", name, collField)) {
				DBObject insertObject = new BasicDBObject();
				insertObject.put("_id", name);
				insertObject.put("description", description);
				
				collField.insert(insertObject);
			}
			else {
				DBObject queryObject = new BasicDBObject("_id", name);
				DBObject objectToSet = new BasicDBObject("description", description);
				DBObject updateObject = new BasicDBObject("$set", objectToSet);
				collField.update(queryObject, updateObject);
			}
				
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
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
	
	@POST
	@Path("/deletefield")
	@SuppressWarnings("unchecked")
	public String DeleteField(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collSupervisor = db.getCollection("supervisor");
			DBCollection collField = db.getCollection("field");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			String username = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String name = inputJson.get("name").toString();
			
			Validator.isParameterEmpty(name);
			Validator.isParameterWrong(name, Validator.FIELD_NAME);
			
			DBObject supervisorObject = GeneralService.GetDBObjectFromId(collSupervisor, username);
			DBObject fieldObject = GeneralService.GetDBObjectFromId(collField, name);
			Validator.isExist(supervisorObject, Validator.USER_SUPERVISOR);
			Validator.isExist(fieldObject, Validator.GENERAL);
			
			DBObject removeObject = new BasicDBObject("_id", name);
//			DBObject ObjectToBeSet = new BasicDBObject("field", name);
//			DBObject ObjectSet = new BasicDBObject("$pull", ObjectToBeSet);
			
			collField.remove(removeObject);
//			collSupervisor.update(ObjectToBeSet, ObjectSet);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
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
	
	@POST
	@Path("/updatereference")
	@SuppressWarnings("unchecked")
	public String UpdateReference(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collReference = db.getCollection("references");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			GeneralService.TokenCheck(inputJson.get("token").toString(), collToken); 
			String id = inputJson.get("id").toString();
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
			DBObject queryObject = new BasicDBObject("_id", id);
			DBObject objectToSet = new BasicDBObject();
			objectToSet.put("author", author);
			objectToSet.put("title", title);
			objectToSet.put("year", year);
			objectToSet.put("abstract", abstr);
			objectToSet.put("keywords", keywords);
			DBObject updateObject = new BasicDBObject("$set", objectToSet);
			collReference.update(queryObject, updateObject);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
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
	
	@POST
	@Path("/deletereference")
	@SuppressWarnings("unchecked")
	public String DeleteReference(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collSupervisor = db.getCollection("supervisor");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collReference = db.getCollection("references");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			String username = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String id = inputJson.get("id").toString();
			
			Validator.isParameterEmpty(id);
			
			DBObject supervisorObject = GeneralService.GetDBObjectFromId(collSupervisor, username);
			DBObject refObject = GeneralService.GetDBObjectFromId(collReference, id);
			Validator.isExist(supervisorObject, Validator.USER_SUPERVISOR);
			Validator.isExist(refObject, Validator.GENERAL);
			
			DBObject removeObject = new BasicDBObject("_id", id);
//			DBObject ObjectToBeSet = new BasicDBObject("field", name);
//			DBObject ObjectSet = new BasicDBObject("$pull", ObjectToBeSet);
			
			collReference.remove(removeObject);
			DBObject objectToPull = new BasicDBObject("references", id);
			DBObject updateObject = new BasicDBObject("$pull", objectToPull);
			collStudent.updateMulti(new BasicDBObject(), updateObject);
//			collSupervisor.update(ObjectToBeSet, ObjectSet);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
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

	private int CountTask(int status, String username, DBCollection coll) {
		List<DBObject> pipeline = new ArrayList<DBObject>();
		pipeline.add(new BasicDBObject("$unwind", "$task"));
		pipeline.add(new BasicDBObject("$match", new BasicDBObject("task.status", status)));
		pipeline.add(new BasicDBObject("$match", new BasicDBObject("_id", username)));
		
		AggregationOutput output = coll.aggregate(pipeline);
		int total = 0;
		for (@SuppressWarnings("unused") final DBObject result: output.results()){
			total++;
		}
		return total;
	}

	private String GetTemplateCode(String username, DBCollection collSupervisor) {
		DBObject supervisorObject = null;
		DBObject queryObject = null;
		String templateCode = "";
		do {
			templateCode = RandomStringUtils.randomAlphanumeric(5);
			
			queryObject = new BasicDBObject();
			queryObject.put("_id", username);
			queryObject.put("template.code", templateCode);
			
			supervisorObject = collSupervisor.findOne(queryObject);
		} while (supervisorObject != null);
		return templateCode;
	}

	private void RejectProposal(DBCollection collStudent, DBCollection collSupervisor, String student,
			String supervisor) {
		DBObject queryObject = new BasicDBObject("_id", supervisor);
		DBObject proposalObject = new BasicDBObject("username", student);
		DBObject objectToPull = new BasicDBObject("proposal", proposalObject);
		DBObject updateObject = new BasicDBObject("$pull", objectToPull);
		collSupervisor.update(queryObject, updateObject);
		
		queryObject = new BasicDBObject("_id", student);
		
		DBObject objectToSet = new BasicDBObject();
		objectToSet.put("status", Student.STATUS_IDLE);
		objectToSet.put("supervisor", null);
		
		updateObject = new BasicDBObject("$set", objectToSet);
		collStudent.update(queryObject, updateObject);
	}

	private void UpdateProposeStudent(DBCollection collStudent, String student, String supervisor) {
		DBObject queryObject = new BasicDBObject("_id", student);
		DBObject objectToSet = new BasicDBObject("status", Student.STATUS_ASSIGN);
		DBObject updateObject = new BasicDBObject("$set", objectToSet);
		collStudent.update(queryObject, updateObject);
	}

	private void UpdateProposeSupervisor(DBCollection collSupervisor, String student, String supervisor) {
		DBObject queryObject = new BasicDBObject("_id", supervisor);		
		DBObject proposalObject = new BasicDBObject("username", student);
		DBObject objectToPull = new BasicDBObject("proposal", proposalObject);
		DBObject updateObject = new BasicDBObject("$pull", objectToPull);
		collSupervisor.update(queryObject, updateObject);
		
		DBObject objectToPush = new BasicDBObject("student", student);
		updateObject = new BasicDBObject("$push", objectToPush);
		collSupervisor.update(queryObject, updateObject);
	}
	
	private void UpdateTaskStatus (DBCollection collStudent, String student, String taskId, int status) {
		DBObject queryObject = new BasicDBObject();
		queryObject.put("_id", student);
		queryObject.put("task.id_task", taskId);

		DBObject objectToSet = new BasicDBObject();
		if (status == 0) {
			objectToSet.put("task.$.status", 1);
			objectToSet.put("task.$.end_date", Service.today);
		}
		else if (status == 1) {
			objectToSet.put("task.$.status", 0);
			objectToSet.put("task.$.end_date", null);
		}
		
		DBObject updateObject = new BasicDBObject("$set", objectToSet);
		collStudent.update(queryObject, updateObject);
	}
}
