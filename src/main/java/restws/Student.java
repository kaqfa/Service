package restws;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import connector.MONGODB;
import exception.ExceptionValidation;
import exception.Validator;

@Path("s")
public class Student {
	public static final int STATUS_IDLE = -1;
	public static final int STATUS_PROPOSE = 0;
	public static final int STATUS_ASSIGN = 1;
	public static final int STATUS_ACTIVE = 2;
	public static final int STATUS_CLAIM = 3;
	public static final int STATUS_GRADUATE = 4;
	
	@POST
	@Path("/register")
	@SuppressWarnings("unchecked")
	public String RegisterStudent(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collStudent = db.getCollection("student");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			String username = inputJson.get("username").toString();
			String password = inputJson.get("password").toString();
			String nim = inputJson.get("nim").toString();
			String name = inputJson.get("name").toString();
			String address = inputJson.get("address").toString();
			String handphone = inputJson.get("handphone").toString();
			String email = inputJson.get("email").toString();
			
			Validator.isParameterEmpty(username);
			Validator.isParameterEmpty(password);
			Validator.isParameterEmpty(nim);
			Validator.isParameterEmpty(name);
			
			Validator.isParameterWrong(username, Validator.USERNAME);
			Validator.isParameterWrong(password, Validator.PASSWORD);
			Validator.isParameterWrong(nim.toUpperCase(), Validator.NIM);
			Validator.isParameterWrong(name, Validator.NAME_PERSON);
			Validator.isParameterWrong(username, Validator.ADDRESS);
			Validator.isParameterWrong(handphone, Validator.PHONE_NUMBER);
			Validator.isParameterWrong(email, Validator.EMAIL);
			
			DBObject insertObject = new BasicDBObject();
			DBObject objectInsideObject = new BasicDBObject();
			insertObject.put("_id", username);
			insertObject.put("password", GeneralService.md5(password));
			insertObject.put("nim", nim);
			insertObject.put("name", name);
			insertObject.put("address", address);
			insertObject.put("handphone", handphone);
			insertObject.put("email", email);
			
			insertObject.put("status", Student.STATUS_IDLE);
			insertObject.put("supervisor", null);
			
			objectInsideObject.put("topic", null);
			objectInsideObject.put("title", null);
			objectInsideObject.put("description", null);
			objectInsideObject.put("field", new JSONArray());
			
			insertObject.put("thesis", objectInsideObject);
			insertObject.put("task", new JSONArray());
			
			objectInsideObject = new BasicDBObject();
			objectInsideObject.put("filename", null);
			objectInsideObject.put("fileid", null);
			objectInsideObject.put("upload_date", null);
			objectInsideObject.put("accept_date", null);
			
			insertObject.put("final", objectInsideObject);
			insertObject.put("activity", new JSONArray());
			insertObject.put("history", new JSONArray());
			insertObject.put("notification", new JSONArray());
			
			collStudent.insert(insertObject);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Registrasi sukses");
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
	public String GetStudent(@PathParam("username") String username, @PathParam("appkey") String appkey,
			@PathParam("token") String token) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			
			GeneralService.AppkeyCheck(appkey, collApp);
			GeneralService.TokenCheck(token, collToken);
			
			Validator.isParameterEmpty(username);
			Validator.isParameterWrong(username, Validator.USERNAME);
			
			DBObject queryObject = new BasicDBObject("_id", username);
			DBObject studentObject = collStudent.findOne(queryObject);
			
			Validator.isExist(studentObject, Validator.GENERAL);
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
			outputJson.put("data", studentObject);
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
	@Path("/getall/{appkey}/{token}")
	@SuppressWarnings("unchecked")
	public String GetAllStudent(@PathParam("appkey") String appkey, @PathParam("token") String token) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			
			GeneralService.AppkeyCheck(appkey, collApp);
			GeneralService.TokenCheck(token, collToken);

			JSONArray studentArray = new JSONArray();
			DBCursor studentList = collStudent.find();
			Validator.isExist(studentList, Validator.GENERAL);
			
			while (studentList.hasNext()) {
				studentArray.add(studentList.next());
			}
			studentList.close();
			
			outputJson.put("code", 1);
			outputJson.put("message", "Success");
			outputJson.put("data", studentArray);
		}
		catch (ExceptionValidation e) {
			outputJson.put("code", 0);
			outputJson.put("message", "Not Found");
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
	@Path("/savethesis")
	@SuppressWarnings("unchecked")
	public String SaveThesis(String jsonString) {
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collField = db.getCollection("field");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			String username = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken); 
			String topic = inputJson.get("topic").toString();
			String title = inputJson.get("title").toString();
			String description = inputJson.get("description").toString();
			JSONArray field = (JSONArray) JSONValue.parse(inputJson.get("field").toString());

			Validator.isParameterEmpty(topic);
			Validator.isParameterEmpty(title);
			Validator.isParameterEmpty(description);
			Validator.isParameterEmpty(field);

			Validator.isParameterWrong(topic, Validator.THESIS_TOPIC);
			Validator.isParameterWrong(title, Validator.THESIS_TITLE);
			Validator.isParameterWrong(description, Validator.THESIS_DESCRIPTION);
			Validator.isParameterWrong(collField, field);
			
			DBObject queryObject = new BasicDBObject("_id", username);
			DBObject thesisObject = new BasicDBObject();
			thesisObject.put("topic", topic);
			thesisObject.put("title", title);
			thesisObject.put("description", description);
			thesisObject.put("field", field);
			DBObject studentObject = collStudent.findOne(queryObject);
			
			Validator.isExist(studentObject, Validator.GENERAL);
			
			UpdateThesis(collStudent, queryObject, thesisObject);
			AddToHistory(collStudent, queryObject, thesisObject);
			
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
	@Path("/resetthesis")
	@Deprecated
	@SuppressWarnings("unchecked")
	public String ResetThesis(String jsonString) {		
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collStudent = db.getCollection("student");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			// Initiate Parameter
			String username = inputJson.get("username").toString();
			
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", username);
			
			DBObject objectToSet = new BasicDBObject(); 
			objectToSet.put("thesis.topic", "");
			objectToSet.put("thesis.title", "");
			objectToSet.put("thesis.description", "");
			objectToSet.put("thesis.field", new JSONArray());
			
			DBObject updateObject = new BasicDBObject();
			queryObject.put("$set", objectToSet);
			
			collStudent.update(queryObject, updateObject);
			
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
	@Path("/propose")
	@SuppressWarnings("unchecked")
	public String ProposeSupervisor(String jsonString) {		
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
			String student = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String supervisor = inputJson.get("supervisor").toString();
			
			Validator.isParameterEmpty(supervisor);
			Validator.isParameterWrong(supervisor, Validator.USERNAME);
			
			DBObject supervisorObject = GeneralService.GetDBObjectFromId(collSupervisor, supervisor);
			Validator.isExist(supervisorObject, Validator.GENERAL);
			
			DBObject studentObject = GeneralService.GetDBObjectFromId(collStudent, student);
			int status = (int) studentObject.get("status");
			Validator.isStudentStatus(status, Validator.STUDENT_STATUS_IDLE);
			
			UpdateProposeStudent(collStudent, student, supervisor);
			UpdateProposeSupervisor(collSupervisor, student, supervisor, studentObject);
			
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
	@Path("/inputcode")
	@SuppressWarnings("unchecked")
	public String InputCode(String jsonString) {		
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
			String student = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String code = inputJson.get("code").toString();
			
			Validator.isParameterEmpty(code);
			Validator.isParameterWrong(code, Validator.TASK_TEMPLATE_ID);
			
			DBObject studentObject = GeneralService.GetDBObjectFromId(collStudent, student);
			String supervisor = studentObject.get("supervisor").toString();
			int status = (int) studentObject.get("status");
			
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", supervisor);
			queryObject.put("template.code", code);
			DBObject supervisorObject = collSupervisor.findOne(queryObject);
			
			Validator.isExist(supervisorObject, Validator.TEMPLATE_ID);
			Validator.isStudentStatus(status, Validator.STUDENT_STATUS_ASSIGNED);
			
			ChangeStudentStatus(collStudent, student);
			CopyTask(collStudent, collSupervisor, supervisor, student, code);
			
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
	@Path("/editprofile")
	@SuppressWarnings("unchecked")
	public String EditStudentProfile(String jsonString) {		
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			
			JSONObject inputJson = (JSONObject) JSONValue.parse(jsonString);
			GeneralService.AppkeyCheck(inputJson.get("appkey").toString(), collApp);
			
			String username = GeneralService.TokenCheck(inputJson.get("token").toString(), collToken);
			String address = inputJson.get("address").toString();
			String handphone = inputJson.get("handphone").toString();
			String email = inputJson.get("email").toString();
			
			Validator.isParameterWrong(address, Validator.ADDRESS);
			Validator.isParameterWrong(handphone, Validator.PHONE_NUMBER);
			Validator.isParameterWrong(email, Validator.EMAIL);
			
			DBObject queryObject = new BasicDBObject("_id", username);
			DBObject objectToSet = new BasicDBObject();
			objectToSet.put("address", address);
			objectToSet.put("handphone", handphone);
			objectToSet.put("email", email);

			DBObject updateObject = new BasicDBObject("$set", objectToSet);
			collStudent.update(queryObject, updateObject);
			
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

	private void AddToHistory(DBCollection collStudent, DBObject queryObject, DBObject thesisObject) {
		thesisObject.put("save_date", Service.today);
		DBObject objectToPush = new BasicDBObject("history", thesisObject);
		DBObject updateObject = new BasicDBObject("$push", objectToPush);
		collStudent.update(queryObject, updateObject);
	}

	private void ChangeStudentStatus(DBCollection collStudent, String student) {
		DBObject queryObject = new BasicDBObject();
		queryObject.put("_id", student);
		
		DBObject objectToSet = new BasicDBObject();
		objectToSet.put("status", STATUS_ACTIVE);
		
		DBObject updateObject = new BasicDBObject();
		updateObject.put("$set", objectToSet);
		
		collStudent.update(queryObject, updateObject);
	}

	@SuppressWarnings("unchecked")
	private void CopyTask(DBCollection collStudent, DBCollection collSupervisor, String supervisor,
			String  student, String code) throws ExceptionValidation {
		JSONArray taskArray = GetTaskArray(collSupervisor, supervisor, code);
		for (int i = 0; i < taskArray.size(); i++) {
			JSONObject taskObject = (JSONObject) taskArray.get(i);
			int duration = (int) taskObject.get("duration");
			JSONArray fileArray = (JSONArray) taskObject.get("file");
			JSONArray files = new JSONArray();
			
			for (int y = 0; y < fileArray.size(); y++) {
				JSONObject fileObject = (JSONObject) fileArray.get(y);
				fileObject.put("by", supervisor);
				fileObject.replace("upload_date", (Date) JSON.parse(fileObject.get("upload_date").toString()));
				files.add(fileObject);
			}
			
			taskObject.put("id_task", GeneralService.GetTaskID(collStudent, student));
			taskObject.put("status", 0);
			taskObject.replace("duration", duration);
			taskObject.put("created_date", Service.today);
			taskObject.put("end_date", null);
			taskObject.replace("file", files);
			taskObject.put("comment", new JSONArray());
			
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", student);
			
			DBObject objectToPush = new BasicDBObject();
			objectToPush.put("task", taskObject);
			
			DBObject updateObject = new BasicDBObject();
			updateObject.put("$push", objectToPush);
			
			collStudent.update(queryObject, updateObject);
		}
	}
	
	private JSONArray GetTaskArray(DBCollection collSupervisor, String supervisor, String code)
			throws ExceptionValidation {
		DBObject queryObject = new BasicDBObject();
		queryObject.put("_id", supervisor);
		queryObject.put("template.code", code);
		
		DBObject projectionObject = new BasicDBObject();
		projectionObject.put("template.$", 1);
		
		DBObject supervisorObject = collSupervisor.findOne(queryObject, projectionObject);
		isExist(supervisorObject);
		
		JSONArray templateArray = (JSONArray) JSONValue.parse(supervisorObject.get("template").toString());
		JSONObject templateObject = (JSONObject) templateArray.get(0);
		
		return (JSONArray) templateObject.get("task");
	}

	private void UpdateProposeStudent(DBCollection collStudent, String student, String supervisor) {
		DBObject queryObject = new BasicDBObject("_id", student);
		
		DBObject objectToSet = new BasicDBObject(); 
		objectToSet.put("status", STATUS_PROPOSE);
		objectToSet.put("supervisor", supervisor);
		
		DBObject updateObject = new BasicDBObject("$set", objectToSet);
		collStudent.update(queryObject, updateObject);
	}

	private void UpdateProposeSupervisor(DBCollection collSupervisor, String student, String supervisor,
			DBObject studentObject) {
		DBObject queryObject = new BasicDBObject("_id", supervisor);
		
		DBObject proposalObject = new BasicDBObject();
		DBObject thesisObject = (DBObject) studentObject.get("thesis");
		proposalObject.put("username", student);
		proposalObject.put("nim", studentObject.get("nim"));
		proposalObject.put("topic", thesisObject.get("topic"));
		proposalObject.put("description", thesisObject.get("description"));
		
		DBObject objectToPush = new BasicDBObject("proposal", proposalObject);
		DBObject updateObject = new BasicDBObject("$push", objectToPush);
		collSupervisor.update(queryObject, updateObject);
	}

	private void UpdateThesis(DBCollection collStudent, DBObject queryObject, DBObject thesisObject) {
		DBObject objectToSet = new BasicDBObject("thesis", thesisObject);
		DBObject updateObject = new BasicDBObject("$set", objectToSet);
		collStudent.update(queryObject, updateObject);
	}

	private void isExist(DBObject object0) throws ExceptionValidation {
		if (object0 == null)
			throw new ExceptionValidation(ExceptionValidation.NOT_EXIST);
	}
}
