package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import restws.Service;
import restws.Student;

import restws.GeneralService;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import connector.MONGODB;

@WebServlet("/rest/su/claim")
public class ValidationFinalReport extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ValidationFinalReport() {
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
	}
	
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter outputResponse = response.getWriter();
		JSONObject outputJson = new JSONObject();
		DB db = null;
		try 
		{
			db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collSupervisor = db.getCollection("supervisor");
			DBCollection collFile = db.getCollection("file");

			String appKey = request.getParameter("appkey");
			String token = request.getParameter("token");
			
			GeneralService.AppkeyCheck(appKey, collApp);
			
			// Initiate Parameter
			String supervisor = GeneralService.TokenCheck(token, collToken);
			String student = request.getParameter("student");
			int code = Integer.parseInt(request.getParameter("code"));
			
			if(code == 1) {
				UpdateGraduatedStudent(collStudent, collSupervisor, student, supervisor);
				UpdateGraduateSupervisor(collSupervisor, student, supervisor);
				MoveFile(collFile, collStudent, student);
				
				outputJson.put("code", 1);
				outputJson.put("message", "Success");
			}
			else if (code == 0) {
				RejectGraduate(collStudent, collSupervisor, student, supervisor);
				
				outputJson.put("code", 1);
				outputJson.put("message", "Success");
			}
			else {
				outputJson.put("code", 0);
				outputJson.put("message", "Code Error");
				
			}
		} 
		catch (Exception e) 
		{
			outputJson.put("code", -1);
			outputJson.put("message",e.toString());
		}

		outputResponse.write(outputJson.toString());
	}

	private void UpdateGraduatedStudent(DBCollection collStudent, DBCollection collSupervisor, String student,
			String supervisor) throws Exception {
		DBObject objectFind = new BasicDBObject();
		objectFind.put("_id", supervisor);
		objectFind.put("claim.username", student);
		
		DBObject findOne = collSupervisor.findOne(objectFind, new BasicDBObject("claim.$", 1));
		JSONObject claimObject = (JSONObject) ((JSONArray) JSONValue.parse(findOne.get("claim").toString())).get(0);
		
		DBObject objectId = new BasicDBObject();
		objectId.put("_id", student);

		DBObject objectFinal = new BasicDBObject();
		objectFinal.put("filename", claimObject.get("filename"));
		objectFinal.put("fileid", claimObject.get("fileid"));
		objectFinal.put("upload_date", (Date) JSON.parse(claimObject.get("upload_date").toString()));
		objectFinal.put("accept_date", new Date());
		
		DBObject objectToSet = new BasicDBObject();
		objectToSet.put("status", Student.STATUS_GRADUATE);
		objectToSet.put("final", objectFinal);

		DBObject objectUpdate = new BasicDBObject();
		objectUpdate.put("$set", objectToSet);

		collStudent.update(objectId, objectUpdate);
	}

	private void UpdateGraduateSupervisor(DBCollection collSupervisor, String student, String supervisor) {

		DBObject objectId = new BasicDBObject();
		objectId.put("_id", supervisor);

		DBObject objectToPull = new BasicDBObject();
		objectToPull.put("student", student);
		objectToPull.put("claim", new BasicDBObject("username",student));

		DBObject objectToPush = new BasicDBObject();
		objectToPush.put("graduate", student);

		DBObject objectUpdate = new BasicDBObject();
		objectUpdate.put("$push", objectToPush);
		objectUpdate.put("$pull", objectToPull);
		
		collSupervisor.update(objectId, objectUpdate);
	}

	private void RejectGraduate(DBCollection collStudent, DBCollection collSupervisor, String student,
			String supervisor) {
		DBObject objectId = new BasicDBObject();
		objectId.put("_id", supervisor);

		DBObject objectToPull = new BasicDBObject();
		objectToPull.put("claim", new BasicDBObject("username",student));

		DBObject objectUpdate = new BasicDBObject();
		objectUpdate.put("$pull", objectToPull);
		
		collSupervisor.update(objectId, objectUpdate);
		
		objectId = new BasicDBObject();
		objectId.put("_id", student);

		DBObject objectToSet = new BasicDBObject();
		objectToSet.put("status", Student.STATUS_ACTIVE);

		objectUpdate = new BasicDBObject();
		objectUpdate.put("$set", objectToSet);
		
		collStudent.update(objectId, objectUpdate);
	}
	
	private void MoveFile(DBCollection collFile, DBCollection collStudent, String student) throws IOException {
		DBObject objectFind = new BasicDBObject();
		objectFind.put("_id", student);
		
		DBObject findOne = collStudent.findOne(objectFind);
		String filename = findOne.get("nim").toString();
		String fileId = ((JSONObject) JSONValue.parse(findOne.get("final").toString())).get("fileid").toString();
		
		objectFind = new BasicDBObject();
		objectFind.put("_id", fileId);
		
		findOne = collFile.findOne(objectFind);
		String source = findOne.get("fullpath").toString();
		String dirPath = getServletContext().getRealPath(Service.dirFinalWork);
		
		File sourcePath = new File(source);
		File dirTarget = new File(dirPath);
		File targetPath = new File(dirTarget, filename);
		Files.move(sourcePath.toPath(), targetPath.toPath());
		
		collFile.remove(objectFind);
	}
}
