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

import restws.GeneralService;
import restws.Service;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import connector.MONGODB;

@WebServlet("/rest/f/creatework")
public class CreateWork extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public CreateWork() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		JSONObject output_json = new JSONObject();
		DB db = null;
		Date now = new Date();
        JSONArray files = new JSONArray();
		try 
		{
			db = MONGODB.GetMongoDB();
			DBCollection collApp = db.getCollection("application");
			DBCollection collToken = db.getCollection("token");
			DBCollection collFile = db.getCollection("file");
			DBCollection collStudent = db.getCollection("student");

			String appKey = request.getParameter("appkey").toString();
			String token = request.getParameter("token").toString();
			GeneralService.AppkeyCheck(appKey,collApp);
			
			// Initiate Parameter
			String by = GeneralService.TokenCheck(token, collToken);
			String taskid = request.getParameter("id_task").toString();
			String studentId = request.getParameter("student").toString();
			Collection<Part> parts = request.getParts();			
	        for (Part part : parts) {
	        	if(part.getName().startsWith("file")){
	        		try (InputStream input = part.getInputStream()) {	   
	        			String dirPath = getServletContext().getRealPath(Service.dirAttachment);
	    	        	String fileName = GeneralService.GetFileName(part);
	    	        	String fileID = GeneralService.GetFileID(fileName,collFile,dirPath);
	    		        
	    		        JSONObject fileObj = new JSONObject();
	    		        fileObj.put("fileid", fileID);
	    				fileObj.put("filename", fileName);
	    				fileObj.put("upload_date", Service.today);
	    				
	    				DBObject ObjectId = new BasicDBObject();
	    				ObjectId.put("_id",studentId);
	    				ObjectId.put("task.id_task",taskid);
	    				
	    				DBObject ObjectToBeSet = new BasicDBObject();
	    				ObjectToBeSet.put("task.$.file",fileObj);
	    				
	    				DBObject ObjectQuery = new BasicDBObject();
	    				ObjectQuery.put("$push", ObjectToBeSet);
	    				
	    				collStudent.update(ObjectId, ObjectQuery);
	    				
	    	            files.add(fileObj);
	    	            
	    	            File uploads = new File(dirPath);
	    		        File file = new File(uploads, fileID);
	    	            Files.copy(input, file.toPath());
    	            }
	        	}
			}
			output_json.put("code", 1);
			output_json.put("message","success");
			output_json.put("data", files);
		} 
		catch (Exception e) 
		{
			output_json.put("code", -1);
			output_json.put("message",e.toString());
			output_json.put("data", files);
		}

		out.write(output_json.toString());
	}

}
