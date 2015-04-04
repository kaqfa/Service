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

import org.json.simple.JSONObject;

import restws.GeneralService;
import restws.Service;
import restws.Student;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import connector.MONGODB;
import exception.ExceptionValidation;
import exception.Validator;

@WebServlet("/rest/s/claim")
@MultipartConfig(maxFileSize = 1024*1024*10, maxRequestSize = 1024*1024*10)
public class ClaimGraduation extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ClaimGraduation() {
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
			DBCollection collFile = db.getCollection("file");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			String appKey = request.getParameter("appkey").toString();
			String token = request.getParameter("token").toString();
			GeneralService.AppkeyCheck(appKey, collApp);
			
			String student = GeneralService.TokenCheck(token, collToken);
			Part file = request.getPart("file");
			
			Validator.isParameterEmpty(file);
			Validator.isParameterWrong(file, Validator.FILE_EXTENSION);
			
			DBObject studentObject = GeneralService.GetDBObjectFromId(collStudent, student);
			String supervisor = studentObject.get("supervisor").toString();
			int status = (int) studentObject.get("status");
			DBObject supervisorObject = GeneralService.GetDBObjectFromId(collSupervisor, supervisor);
			
			Validator.isExist(supervisorObject, Validator.GENERAL);
			Validator.isStudentStatus(status, Validator.STUDENT_STATUS_ACTIVE);
			
			UploadFile(request.getParts(), collSupervisor, collStudent, collFile, student, supervisor);
			
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

	@SuppressWarnings("unchecked")
	private void AddClaimInfo(DBCollection collSupervisor, String student, String supervisor, String fileName,
			String fileID) {
		JSONObject claimObject = new JSONObject();
		claimObject.put("username", student);
		claimObject.put("filename", fileName);
		claimObject.put("fileid", fileID);
		claimObject.put("upload_date", Service.today);
		
		DBObject queryObject = new BasicDBObject("_id", supervisor);
		DBObject objectToPush = new BasicDBObject("claim", claimObject);
		DBObject updateObject = new BasicDBObject("$push", objectToPush);
		collSupervisor.update(queryObject, updateObject);
	}
	
	private void ChangeStatus(DBCollection collStudent, String student) {
		DBObject queryObject = new BasicDBObject("_id", student);
		DBObject objectToSet = new BasicDBObject("status", Student.STATUS_CLAIM);
		DBObject updateObject = new BasicDBObject("$set", objectToSet);
		collStudent.update(queryObject, updateObject);
	}
	
	private void UploadFile(Collection<Part> parts, DBCollection collSupervisor, DBCollection collStudent,
			DBCollection collFile, String student, String supervisor) throws IOException {
		for (Part part : parts) {
			if(part.getName().startsWith("file")){
				try (InputStream input = part.getInputStream()) {
					String dirPath = getServletContext().getRealPath(Service.dirAttachment);
					String fileName = part.getSubmittedFileName();
					String fileID = GeneralService.GetFileID(fileName, collFile, dirPath);
					
					AddClaimInfo(collSupervisor, student, supervisor, fileName, fileID);
					ChangeStatus(collStudent, student);
					
					File targetFile = new File(dirPath, fileID);
					Files.copy(input, targetFile.toPath());
				}
			}
		}
	}
}
