package reccomender;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ContentBased {
	JSONArray dokumen, mhsfield;
	JSONObject sim;
	
	public ContentBased(JSONArray dokumen, JSONArray mhsfield){
		this.dokumen=dokumen;
		this.mhsfield=mhsfield;
		calcJaccard();
	}
	
	@SuppressWarnings("unchecked")
	public void calcJaccard(){
		int i, j;
		int intersect=0;
		double union=0;
		sim=new JSONObject();
		JSONObject n;
		JSONArray keywords;
		for(i=0;i<dokumen.size();i++){
			intersect=0;
			n=(JSONObject) JSONValue.parse(dokumen.get(i).toString());
			keywords=(JSONArray)n.get("keywords");
			for(j=0;j<keywords.size();j++){
				if(mhsfield.contains(keywords.get(j)))
					intersect++;
			}
			union=keywords.size()+mhsfield.size();
			if(intersect!=0)
				sim.put(n.get("_id"), (intersect/union));
		}
	}
	
	public JSONObject getSim(){
		System.out.println(sim);
		return sim;
	}
}
