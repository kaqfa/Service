package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import restws.GeneralService;
import restws.Service;

import com.mongodb.DB;
import com.mongodb.DBCollection;

import connector.MONGODB;
import exception.ExceptionValidation;

@WebServlet("/rest/g/getlistthesis")
public class GetAllThesis extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public GetAllThesis() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		JSONObject outputJsonObj = new JSONObject();
		JSONArray outputData = new JSONArray();
		try {
			DB db = MONGODB.GetMongoDB();
			DBCollection collApplication = db.getCollection("application");
			
			String appKey = request.getParameter("appkey");			
			GeneralService.AppkeyCheck(appKey, collApplication);
	
			File[] files = new File(getServletContext().getRealPath(Service.dirFinalWork)).listFiles();
		    for (File file : files) {
		      if (file.isFile()) {
		    	  outputData.add(file.getName());
		      }
		    }
		    
			outputJsonObj.put("code", 1);
		    outputJsonObj.put("message", "success");
		    outputJsonObj.put("data", outputData);
		} catch (ExceptionValidation e) {
			outputJsonObj.put("code", 0);
		    outputJsonObj.put("message", e.toString());
		    outputJsonObj.put("data", outputData);
		    e.printStackTrace();
		} catch (Exception e) {
			outputJsonObj.put("code", -1);
		    outputJsonObj.put("message", e.toString());
		    outputJsonObj.put("data", outputData);
		    e.printStackTrace();
		}

		out.write(outputJsonObj.toString());
	}

}
