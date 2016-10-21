package reccomender;

import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class CollaborativeFiltering {
	JSONArray neighbor, nn;
	JSONObject main, df, sim;
	
	public CollaborativeFiltering(JSONObject main, JSONArray neighbor){
		this.neighbor=neighbor;
		this.main=main;
		cekDistance();
	}
	
	@SuppressWarnings("unchecked")
	public void cekDistance(){
		int i, j, val;
		JSONObject n, rcopy;
		JSONObject r=new JSONObject();
		Iterator<Map.Entry<String, Object>> iter;
		JSONArray field;
		field=(JSONArray)((JSONObject)main.get("thesis")).get("field");
		for(i=0;i<field.size();i++){
			r.put(field.get(i).toString(), 1);
		}
		for(i=0;i<neighbor.size();i++){
			n=(JSONObject) JSONValue.parse(neighbor.get(i).toString());
			field=(JSONArray)((JSONObject)JSONValue.parse(n.get("thesis").toString())).get("field");
			rcopy=new JSONObject();
			rcopy.putAll(r);
			for(j=0;j<field.size();j++){
				val=((rcopy.get(field.get(j).toString()))==null)?1:0;
				rcopy.put(field.get(j).toString(), val);
			}
			iter=rcopy.entrySet().iterator();
			val=0;
			while(iter.hasNext()){
				val+=(int) iter.next().getValue();
			}
			n.put("dist", val);
			neighbor.set(i, n);
		}
		setNN();
	}
	
	@SuppressWarnings("unchecked")
	public void setNN(){
		nn=new JSONArray();
		int size=(int) Math.sqrt(neighbor.size());
		int i,j, minIdx;
		JSONObject minVal, n;
		for(i=0;i<size;i++){
			minVal=(JSONObject) JSONValue.parse(neighbor.get(i).toString());
			minIdx=i;
			for(j=i+1;j<neighbor.size();j++){
				n=(JSONObject) JSONValue.parse(neighbor.get(j).toString());
				if((Long)minVal.get("dist")>(Long)n.get("dist")){
					minVal=n;
					minIdx=j;
				}
			}
			n=(JSONObject) neighbor.get(i);
			neighbor.set(i, minVal);
			neighbor.set(minIdx, n);
			System.out.println(minVal.toString());
			nn.add(minVal);
		}
		setDF();
	}
	
	@SuppressWarnings("unchecked")
	public void setDF(){
		int i,j, value;
		JSONObject n;
		JSONArray ref, mainRef;
		df=new JSONObject();
		mainRef=(JSONArray)main.get("references");
		for(i=0;i<nn.size();i++){
			n=(JSONObject)JSONValue.parse(nn.get(i).toString());
			ref=(JSONArray)n.get("references");
			for(j=0;j<ref.size();j++){
				if(!mainRef.contains(ref.get(j).toString())){
					value=(df.get(ref.get(j).toString())==null)?0:(int)df.get(ref.get(j).toString());
					value++;
					df.put(ref.get(j).toString(), value);
				}
			}
		}
		setSim();
	}
	
	@SuppressWarnings("unchecked")
	public void setSim(){
		int val;
		double max=0;
		Map.Entry<String, Object> entry;
		sim=new JSONObject();
		Iterator<Map.Entry<String, Object>> iter=df.entrySet().iterator();
		while(iter.hasNext()){
			val=(int) iter.next().getValue();
			if(val>max)
				max=val;
		}
		iter=df.entrySet().iterator();
		while(iter.hasNext()){
			entry=iter.next();
			sim.put(entry.getKey(), ((double)((Integer)entry.getValue()).intValue())/max);
		}
	}
	
	public JSONObject getSim(){
		System.out.println(sim);
		return sim;
	}
}
