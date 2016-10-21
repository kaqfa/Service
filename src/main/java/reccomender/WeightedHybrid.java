package reccomender;

import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WeightedHybrid {
	JSONObject cf, cb, weighted;
	JSONArray result;
	int n;
	double wcf, wcb;
	
	public WeightedHybrid(JSONObject cf, JSONObject cb, double wcf, double wcb, int n){
		this.cf=cf;
		this.cb=cb;
		this.wcf=wcf;
		this.wcb=wcb;
		this.n=n;
		this.result=new JSONArray();
		calcWeighted();
	}
	
	@SuppressWarnings("unchecked")
	public void calcWeighted(){
		double val;
		Map.Entry<String, Object> entry;
		weighted=new JSONObject();
		Iterator<Map.Entry<String, Object>> iter=cb.entrySet().iterator();
		while(iter.hasNext()){
			entry=iter.next();
			val=(cf.get(entry.getKey())==null)?0:(double)cf.get(entry.getKey())*wcf;
			val+=(double)entry.getValue()*wcb;
			weighted.put(entry.getKey(), val);
		}
		iter=cf.entrySet().iterator();
		while(iter.hasNext()){
			entry=iter.next();
			if(cb.get(entry.getKey())==null){
				val=(double)entry.getValue()*wcf;
				weighted.put(entry.getKey(), val);
			}
		}
		sort();
	}
	
	@SuppressWarnings("unchecked")
	public void sort(){
		Map.Entry<String, Object> entry;
		String maxKey;
		int i=0;
		double val, maxVal;
		JSONObject newWeighted=new JSONObject();
		Iterator<Map.Entry<String, Object>> iter;
		while(weighted.size()>0&&i<n){
			iter=weighted.entrySet().iterator();
			if(iter.hasNext()){
				entry=iter.next();
				maxKey=entry.getKey();
				maxVal=(double)entry.getValue();
				while(iter.hasNext()){
					entry=iter.next();
					val=(double)entry.getValue();
					if(val>maxVal){
						maxVal=val;
						maxKey=entry.getKey();
					}
				}
				System.out.println(maxKey+" "+maxVal);
				newWeighted.put(maxKey, maxVal);
				result.add(maxKey);
				weighted.remove(maxKey);
			}
			i++;
		}
		weighted=newWeighted;
	}
	
	public JSONArray getResult(){
		return result;
	}
}
