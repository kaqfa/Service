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

import restws.GeneralService;
import restws.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import connector.MONGODB;

@WebServlet("/rest/su/updatetasktemplate")
public class UpdateTaskTemplate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public UpdateTaskTemplate() {
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
			DBCollection collSupervisor = db.getCollection("supervisor");
			
			String appKey = request.getParameter("appkey");
			String token = request.getParameter("token");

			String username = GeneralService.TokenCheck(token, collToken);
			String templateCode = request.getParameter("template");
			String oldName = request.getParameter("oldname");
			String newName = request.getParameter("newname");
			String description = request.getParameter("description");
			int duration = Integer.parseInt(request.getParameter("duration"));
			
			GeneralService.AppkeyCheck(appKey, collApplication);
	
			DBObject objectFind	= new BasicDBObject("_id",username)
				.append("template.code", templateCode)
				.append("template.task.name", oldName);
			
			int index = getIndex(collSupervisor, objectFind, oldName);
			
			UpdateTemplateTask(collSupervisor, objectFind, index, duration, newName, description);
			if(!request.getParameter("remove").isEmpty())
				RemoveFile(request.getParameter("remove"), collSupervisor, objectFind, index);
			UploadFile(request.getParts(), collFile, collSupervisor, objectFind, index);
			
			outputJsonObj.put("code", 1);
		    outputJsonObj.put("message", "success");
		    outputJsonObj.put("username", username);
		    
		} catch (Exception e) {
			outputJsonObj.put("code", -1);
		    outputJsonObj.put("message", e.toString());
		    e.printStackTrace();
		}

		out.write(outputJsonObj.toString());	
	}

	private void UploadFile(Collection<Part> parts, DBCollection collFile, DBCollection collSupervisor,
			DBObject objectFind, int index) throws IOException {
		for (Part part : parts) {
			if(part.getName().startsWith("file")){
				try (InputStream input = part.getInputStream()) {	   
					String dirPath = getServletContext().getRealPath(Service.dirAttachment);
		        	String fileName = GeneralService.GetFileName(part);
		        	String fileID = GeneralService.GetFileID(fileName,collFile,dirPath);
			        
			        DBObject fileObject = new BasicDBObject();
			        fileObject.put("fileid", fileID);
					fileObject.put("filename", fileName);
					fileObject.put("upload_date", new Date());
					
		            DBObject objectToSet = new BasicDBObject("template.$.task."+index+".file", fileObject);
		            DBObject objectSet = new BasicDBObject("$push", objectToSet);
		            collSupervisor.update(objectFind, objectSet);
					
		            File uploads = new File(dirPath);
			        File file = new File(uploads, fileID);
		            Files.copy(input, file.toPath());
		        }
			}
		}
	}

	private void RemoveFile(String filesToRemove, DBCollection collSupervisor, DBObject objectFind, int index) {
		JSONArray removed = (JSONArray) JSONValue.parse(filesToRemove);
		for(int i = 0;i<removed.size();i++){
			String fileId = removed.get(i).toString();
			DBObject objectToPull = new BasicDBObject("fileid", fileId);
			DBObject objectToSet = new BasicDBObject("template.$.task."+index+".file", objectToPull);
			DBObject objectSet = new BasicDBObject("$pull", objectToSet);
			collSupervisor.update(objectFind, objectSet);
		}
	}

	private static void UpdateTemplateTask(DBCollection collSupervisor, DBObject objectFind, int index, int duration,
			String name, String description) {
		DBObject objectToSet= new BasicDBObject("template.$.task."+index+".duration", duration)
										.append("template.$.task."+index+".name", name)
										.append("template.$.task."+index+".description", description);
		
		DBObject objectSet= new BasicDBObject("$set",objectToSet);
		collSupervisor.updateMulti(objectFind, objectSet);
	}
	
	private int getIndex(DBCollection collSupervisor, DBObject objectFind, String taskId) {
		DBObject findOne = collSupervisor.findOne(objectFind, new BasicDBObject("template.$", 1));
		
		JSONArray templateArray = (JSONArray) JSONValue.parse(findOne.get("template").toString());
		JSONObject templateObject = (JSONObject) templateArray.get(0);
		JSONArray taskArray = (JSONArray) templateObject.get("task");
		
		return GeneralService.GetIndexArray(taskArray, "name", taskId);
	}
}
