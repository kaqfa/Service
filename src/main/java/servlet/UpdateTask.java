package servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import restws.Service;

import restws.GeneralService;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import connector.MONGODB;

@WebServlet("/rest/su/updatetask")
public class UpdateTask extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public UpdateTask() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		JSONObject outputJsonObj = new JSONObject();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApplication = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collFile = db.getCollection("file");
			DBCollection collStudent = db.getCollection("student");
			
			String appKey = request.getParameter("appkey");
			String token = request.getParameter("token");
			
			String studentId = request.getParameter("student");
			String taskId = request.getParameter("id_task");
			
			String name = request.getParameter("name");
			String description = request.getParameter("description");
			int duration = Integer.parseInt(request.getParameter("duration"));
			
			String username = GeneralService.TokenCheck(token, collToken);		
			GeneralService.AppkeyCheck(appKey, collApplication);
	
			DBObject objectFind	= new BasicDBObject("_id",studentId)
				.append("task.id_task", taskId);
			
			UpdateStudentTask(collStudent, objectFind, name, description, duration);
			if(!request.getParameter("remove").isEmpty()) RemoveFileTask(request.getParameter("remove"), collStudent, objectFind);
			UploadFile(request.getParts(), collFile, collStudent, objectFind);
			
			outputJsonObj.put("code", 1);
		    outputJsonObj.put("message", "success");
		    outputJsonObj.put("username", username);
		    
		} catch (Exception e) {
			outputJsonObj.put("code", -1);
		    outputJsonObj.put("message", e.toString());
		}

		out.write(outputJsonObj.toString());
	}

	private void UploadFile(Collection<Part> parts,DBCollection collFile, DBCollection collStudent,
			DBObject objectFind) throws IOException {
		for (Part part : parts) {
			if(part.getName().startsWith("file")){
				try (InputStream input = part.getInputStream()) {	   
					String dirPath = getServletContext().getRealPath(Service.dirAttachment);
		        	String fileName = GeneralService.GetFileName(part);
		        	String fileID = GeneralService.GetFileID(fileName,collFile,dirPath);
			        
			        DBObject fileObj = new BasicDBObject();
			        fileObj.put("fileid", fileID);
					fileObj.put("filename", fileName);
					fileObj.put("upload_date", new Date());
					
					DBObject objectToSet= new BasicDBObject("task.$.file",fileObj);
					DBObject objectSet= new BasicDBObject("$push",objectToSet);
					collStudent.update(objectFind, objectSet);
					
		            File uploads = new File(dirPath);
			        File file = new File(uploads, fileID);
		            Files.copy(input, file.toPath());
		        }
			}
		}
	}

	private void RemoveFileTask(String filesToRemove, DBCollection collStudent, DBObject objectFind) {
		JSONArray removed = (JSONArray) JSONValue.parse(filesToRemove);
		for(int i = 0;i<removed.size();i++){
			String fileId = removed.get(i).toString();
			DBObject objectToSet = new BasicDBObject("task.$.file",fileId);
			DBObject objectSet = new BasicDBObject("$pull",objectToSet);
			collStudent.update(objectFind, objectSet);
		}
	}

	private void UpdateStudentTask(DBCollection collStudent, DBObject objectFind, String name, String description,
			int duration) {
		DBObject objectToSet= new BasicDBObject("task.$.duration",duration)
					.append("task.$.name",name)
					.append("task.$.description",description);
		
		DBObject objectSet= new BasicDBObject("$set",objectToSet);
		collStudent.update(objectFind, objectSet);
	}

}
