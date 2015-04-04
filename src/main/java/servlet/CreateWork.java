package servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Collection;

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

@WebServlet("/rest/f/creatework")
@MultipartConfig(maxFileSize = 1024*1024*5)
public class CreateWork extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public CreateWork() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter outputResponse = response.getWriter();
		JSONObject outputJson = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collFile = db.getCollection("file");
			DBCollection collStudent = db.getCollection("student");

			String appKey = request.getParameter("appkey");
			String token = request.getParameter("token");
			GeneralService.AppkeyCheck(appKey, collApp);
			
			// Initiate Parameter
			String username = GeneralService.TokenCheck(token, collToken);
			String student = request.getParameter("student");
			String taskId = request.getParameter("id_task");
			Part file = request.getPart("file");
			int status;
			JSONArray files = new JSONArray();
			
			Validator.isParameterEmpty(student);
			Validator.isParameterEmpty(taskId);
			Validator.isParameterEmpty(file);
			
			Validator.isParameterWrong(student, Validator.USERNAME);
			Validator.isParameterWrong(taskId, Validator.TASK_TEMPLATE_ID);
			
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", student);
			queryObject.put("task.id_task", taskId);
			DBObject studentObject = collStudent.findOne(queryObject);
			String supervisor = studentObject.get("supervisor").toString();
			Validator.isExist(studentObject, Validator.TASK_ID);
			
			if (username.equals(student)) {
				status = (int) studentObject.get("task.$.status");
				Validator.isParameterWrong(status, Validator.TASK_STATUS);
			}
			else if (username.equals(supervisor)) {
				status = (int) studentObject.get("status");
				Validator.isStudentStatus(status, Validator.STUDENT_STATUS_ACTIVE);
			}
			
			files = UploadFile(request.getParts(), collFile, username);
			UpdateStudent(collStudent, student, taskId, files);
			
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
	
	private void UpdateStudent(DBCollection collStudent, String student, String taskId, JSONArray files) {
		DBObject queryObject = new BasicDBObject();
		queryObject.put("_id", student);
		queryObject.put("task.id_task", taskId);
		
		DBObject objectToPush = new BasicDBObject("task.$.file", files);
		DBObject updateObject = new BasicDBObject("$push", objectToPush);
		collStudent.update(queryObject, updateObject);
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray UploadFile(Collection<Part> parts, DBCollection collFile, String username) throws IOException {
		JSONArray files = new JSONArray();
		for (Part part : parts) {
			if(part.getName().startsWith("file")){
					try (InputStream input = part.getInputStream()) {	   
						String dirPath = getServletContext().getRealPath(Service.dirAttachment);
						String fileName = part.getSubmittedFileName();
						String fileID = GeneralService.GetFileID(fileName, collFile, dirPath);
						
						JSONObject fileObject = new JSONObject();
						fileObject.put("fileid", fileID);
						fileObject.put("filename", fileName);
						fileObject.put("by", username);
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
