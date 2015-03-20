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

import org.json.simple.JSONObject;

import restws.Service;
import restws.Student;

import restws.GeneralService;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import connector.MONGODB;

@WebServlet("/rest/s/claim")
@MultipartConfig
public class ClaimGraduation extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public ClaimGraduation() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		JSONObject output_json = new JSONObject();
		DB db = null;
		try 
		{
			db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collFile = db.getCollection("file");
			DBCollection collStudent = db.getCollection("student");
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			String appKey = request.getParameter("appkey").toString();
			String token = request.getParameter("token").toString();
			GeneralService.AppkeyCheck(appKey,collApp);
			
			String username = GeneralService.TokenCheck(token, collToken);
			
			DBObject studentObject = GeneralService.GetDBObjectFromId(collStudent, username);
			if((int) studentObject.get("status") == Student.STATUS_ACTIVE){				
				Collection<Part> parts = request.getParts();			
		        for (Part part : parts) {
		        	if(part.getName().startsWith("file")){
		        		try (InputStream input = part.getInputStream()) {	   
		        			String dirPath = getServletContext().getRealPath(Service.dirAttachment);
		    	        	String fileName = GeneralService.GetFileName(part);
		    	        	String fileID = GeneralService.GetFileID(fileName,collFile,dirPath);
		    		        
		    	        	AddClaimInfo(collSupervisor, username, studentObject, fileName, fileID);
		    	    		ChangeStatus(collStudent, username);
		    	    		
		    	            File uploads = new File(dirPath);
		    		        File file = new File(uploads, fileID);
		    	            Files.copy(input, file.toPath());
	    	            }
		        	}
		        }
				
				output_json.put("code", 1);
				output_json.put("message","success");
			}else{
				output_json.put("code", 0);
				output_json.put("message","Wrong status");
			}
		} 
		catch (Exception e) 
		{
			output_json.put("code", -1);
			output_json.put("message",e.toString());
		}

		out.write(output_json.toString());
	}

	@SuppressWarnings("unchecked")
	private void AddClaimInfo(DBCollection collSupervisor, String username, DBObject studentObject,
			String fileName, String fileID) {
		JSONObject fileObj = new JSONObject();
		fileObj.put("username", username);
		fileObj.put("filename", fileName);
		fileObj.put("fileid", fileID);
		fileObj.put("upload_date", new Date());
		
		DBObject ObjectId = new BasicDBObject();
		ObjectId.put("_id",studentObject.get("supervisor").toString());
		
		DBObject ObjectToBeSet = new BasicDBObject();
		ObjectToBeSet.put("claim",fileObj);
		
		DBObject ObjectQuery = new BasicDBObject();
		ObjectQuery.put("$push", ObjectToBeSet);
		
		collSupervisor.update(ObjectId, ObjectQuery);
	}
	
	private void ChangeStatus(DBCollection collStudent, String username) {
		DBObject ObjectId = new BasicDBObject();
		ObjectId.put("_id",username);
		
		DBObject ObjectToBeSet = new BasicDBObject();
		ObjectToBeSet.put("status",Student.STATUS_CLAIM);
		
		DBObject ObjectQuery = new BasicDBObject();
		ObjectQuery.put("$set", ObjectToBeSet);
		
		collStudent.update(ObjectId, ObjectQuery);
	}
}
