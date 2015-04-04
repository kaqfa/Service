package servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import restws.GeneralService;
import restws.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import connector.MONGODB;
import exception.ExceptionValidation;
import exception.Validator;

@WebServlet("/rest/su/createtask")
@MultipartConfig(maxFileSize = 1024*1024*5)
public class CreateTask extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public CreateTask() {
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
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collFile = db.getCollection("file");
			
			String appKey = request.getParameter("appkey");
			String token = request.getParameter("token");
			
			GeneralService.AppkeyCheck(appKey, collApp);
			
			String student = request.getParameter("student");
			String supervisor = GeneralService.TokenCheck(token, collToken);
			String name = request.getParameter("name");
			String description = request.getParameter("description");
			String stringDuration = request.getParameter("duration");
			
			Validator.isParameterEmpty(student);
			Validator.isParameterEmpty(name);
			
			Validator.isParameterWrong(student, Validator.USERNAME);
			Validator.isParameterWrong(name, Validator.TASK_TEMPLATE_NAME);
			Validator.isParameterWrong(description, Validator.TASK_TEMPLATE_DESCRIPTION);
			Validator.isParameterWrong(stringDuration, Validator.TASK_DURATION);

			int duration = GetDuration(stringDuration);
			
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", student);
			queryObject.put("supervisor", supervisor);
			DBObject studentObject = collStudent.findOne(queryObject);
			Validator.isExist(studentObject, Validator.GENERAL);
			
			int status = (int) studentObject.get("status");
			Validator.isStudentStatus(status, Validator.STUDENT_STATUS_ACTIVE);
			
			JSONArray files = UploadFile(request.getParts(), collFile, supervisor);
			UpdateStudent(collStudent, queryObject, student, name, description, duration, files);

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

		outputResponse.write(outputJson.toString());
	}

	private int GetDuration(String stringDuration) {
		if (stringDuration == null || stringDuration.isEmpty())
			return -1;
		else
			return Integer.parseInt(stringDuration);
	}
	
	private void UpdateStudent(DBCollection collStudent, DBObject queryObject, String student, String name,
			String description, int duration, JSONArray files) {
		DBObject taskObject = new BasicDBObject();
		taskObject.put("name", name);
		taskObject.put("description", description);
		taskObject.put("id_task", GeneralService.GetTaskID(collStudent, student));
		taskObject.put("status", 0);
		taskObject.put("duration", duration);
		taskObject.put("created_date", new Date());
		taskObject.put("end_date", null);
		taskObject.put("file", files);
		taskObject.put("comment", new JSONArray());
		
		BasicDBObject updateObject = new BasicDBObject();
		updateObject.put("$push", new BasicDBObject("task", taskObject));
		
		collStudent.update(queryObject, updateObject);	
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray UploadFile(Collection<Part> parts, DBCollection collFile, String supervisor) throws IOException {
		JSONArray files = new JSONArray();
		for (Part part : parts) {
			if(part.getName().startsWith("file")) {
				try (InputStream input = part.getInputStream()) {
					String dirPath = getServletContext().getRealPath(Service.dirAttachment);
					String fileName = part.getSubmittedFileName();
					String fileID = GeneralService.GetFileID(fileName, collFile, dirPath);
					
					DBObject fileObject = new BasicDBObject();
					fileObject.put("fileid", fileID);
					fileObject.put("filename", fileName);
					fileObject.put("by", supervisor);
					fileObject.put("upload_date", Service.today);
					files.add(fileObject);
					
					File targetFile = new File(dirPath, fileID);
					Files.copy(input, targetFile.toPath());
				}
			}
		}
		return files;
	}
}
