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

import restws.Service;

import restws.GeneralService;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import connector.MONGODB;
import exception.ExceptionValidation;
import exception.Validator;

@WebServlet("/rest/f/createcomment")
@MultipartConfig(maxFileSize = 1024*1024*5)
public class CreateComment extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public CreateComment() {
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
			
			String appKey = request.getParameter("appkey");
			String token = request.getParameter("token");
			GeneralService.AppkeyCheck(appKey, collApp);
			
			// Initiate Parameter
			String username = GeneralService.TokenCheck(token, collToken);
			String student = request.getParameter("student");
			String taskId = request.getParameter("id_task");
			String typeComment = request.getParameter("type");
			String text = request.getParameter("text");
			Part file = request.getPart("file");
			JSONArray files = new JSONArray();
			
			Validator.isParameterEmpty(student);
			Validator.isParameterEmpty(taskId);
			Validator.isParameterEmpty(typeComment);
			Validator.isParameterEmpty(text, file);

			int type = Integer.parseInt(typeComment);			
			Validator.isParameterWrong(student, Validator.USERNAME);
			Validator.isParameterWrong(taskId, Validator.TASK_TEMPLATE_ID);
			Validator.isParameterWrong(type, Validator.COMMENT_TYPE);
			Validator.isParameterWrong(text, Validator.COMMENT_TEXT);

			DBObject isStudent = GeneralService.GetDBObjectFromId(collStudent, username);
			DBObject isSupervisor = GeneralService.GetDBObjectFromId(collSupervisor, username);
			Validator.isExist(isStudent, isSupervisor);
			
			DBObject queryObject = new BasicDBObject();
			queryObject.put("_id", student);
			queryObject.put("task.id_task", taskId);
			isStudent = collStudent.findOne(queryObject);
			
			Validator.isExist(isStudent, Validator.TASK_ID);
			
			type = CommentType(isStudent, isSupervisor, type);
			files = UploadFile(request.getParts(), collFile);
			UpdateStudent(collStudent, student, taskId, username, type, text, files);
			
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
	
	private int CommentType(DBObject studentObject, DBObject supervisorObject, int type) throws ExceptionValidation {
		if(studentObject != null) {
			if(type == Service.COMMENT_TYPE_STUDENT_ASKING ||
					type == Service.COMMENT_TYPE_STUDENT_COMMENT)
				return type;
		}
		else if(supervisorObject != null) {
			if(type == Service.COMMENT_TYPE_SUPERVISOR_CLARIFY ||
					type == Service.COMMENT_TYPE_SUPERVISOR_COMMENT ||
					type == Service.COMMENT_TYPE_SUPERVISOR_INSTRUCT)
				return type;
		}
		else throw new ExceptionValidation(ExceptionValidation.WRONG_COMMENT_TYPE);
		return type;
	}
	
	@SuppressWarnings("unchecked")
	private void UpdateStudent(DBCollection collStudent, String student, String taskId, String username,
			int type, String text, JSONArray files) {
		JSONObject commentObject = new JSONObject();
		commentObject.put("id_comment", GeneralService.GetCommentID(collStudent, student, taskId));
		commentObject.put("by", username);
		commentObject.put("type", type);
		commentObject.put("text", text);
		commentObject.put("file", files);
		commentObject.put("post_date", Service.today);
		
		DBObject queryObject = new BasicDBObject();
		queryObject.put("_id", student);
		queryObject.put("task.id_task", taskId);
		
		DBObject objectToPush = new BasicDBObject("task.$.comment", commentObject);
		DBObject updateObject = new BasicDBObject("$push", objectToPush);
		collStudent.update(queryObject, updateObject);
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray UploadFile(Collection<Part> parts, DBCollection collFile) throws IOException {
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
					files.add(fileObject);
					
					File targetFile = new File(dirPath, fileID);
					Files.copy(input, targetFile.toPath());
				}
			}
		}
		return files;
	}
}
