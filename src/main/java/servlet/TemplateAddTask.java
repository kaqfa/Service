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

import restws.Service;

import restws.GeneralService;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import connector.MONGODB;

@WebServlet("/rest/su/addtask")
public class TemplateAddTask extends HttpServlet {
	private static final long serialVersionUID = 1L;
      
    public TemplateAddTask() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

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
			DBCollection collSupervisor = db.getCollection("supervisor");
			DBCollection collFile = db.getCollection("file");
			
			String appKey = request.getParameter("appkey");
	        String token = request.getParameter("token");
			GeneralService.AppkeyCheck(appKey,collApp);
			
			String username = GeneralService.TokenCheck(token, collToken);
			String templateCode = request.getParameter("template");
	        String taskname = request.getParameter("name");
	        String taskDescription = request.getParameter("description");
	        int taskDuration = Integer.parseInt(request.getParameter("duration"));
	        Date now = new Date();
	        JSONArray files = new JSONArray();
	        
			Collection<Part> parts = request.getParts();
	        for (Part part : parts) {
	        	if(part.getName().startsWith("file")){
	        		try (InputStream input = part.getInputStream()) {	   
	        			String dirPath = getServletContext().getRealPath(Service.dirAttachment);
	    	        	String fileName = GeneralService.GetFileName(part);
	    	        	String fileID = GeneralService.GetFileID(fileName,collFile,dirPath);
	    	        	File uploads = new File(dirPath);
	    		        File file = new File(uploads, fileID);
	    		        
	    		        BasicDBObject obj = new BasicDBObject()
	    		        	.append("fileid",fileID)
	    		        	.append("filename", fileName)
	    		        	.append("upload_date", now);
	    		        files.add(obj);
	    		        
	    	            Files.copy(input, file.toPath());
    	            }
	        	}
			}
			
			JSONObject fileObj = new JSONObject();
			fileObj.put("name", taskname);
			fileObj.put("description", taskDescription);
			fileObj.put("duration", taskDuration);
			fileObj.put("file", files);
			
			DBObject ObjectId = new BasicDBObject();
			ObjectId.put("_id",username);
			ObjectId.put("template.code",templateCode);
			
			DBObject ObjectToBeSet = new BasicDBObject();
			ObjectToBeSet.put("template.$.task",fileObj);
			
			DBObject ObjectQuery = new BasicDBObject();
			ObjectQuery.put("$push", ObjectToBeSet);
			
			collSupervisor.update(ObjectId, ObjectQuery);
			
			output_json.put("code", 1);
			output_json.put("message","success");
		} 
		catch (Exception e) 
		{
			output_json.put("code", -1);
			output_json.put("message",e.toString());
		}

		out.write(output_json.toString());
	}

}
