
package com.navinfo.navicommons.config;

import java.util.HashMap;
import java.util.Map;


public class VariableExpander
{
	private VariableStore variables;

	private String pre;
	private String post;

	private Map cache;

	public VariableExpander(VariableStore variables, String pre, String post)
	{
		this.variables = variables;
		this.pre = pre;
		this.post = post;
		cache = new HashMap();
	}

	public void clearCache()
	{
		cache.clear();
	}
	
	public String expandVariables(String source)
	{
		String result = (String)this.cache.get(source);
		
		if (source == null || result != null) {
			return result;
		}
		
		int fIndex = source.indexOf(this.pre);
		
		if (fIndex == -1) {
			return source;
		}
		
		StringBuffer sb = new StringBuffer(source);
		
		while (fIndex > -1) {
			int lIndex = sb.indexOf(this.post);
			
			int start = fIndex + this.pre.length();
			
			if (fIndex == 0) {
				String varName = sb.substring(start, start + lIndex - this.pre.length());
				sb.replace(fIndex, fIndex + lIndex + 1, this.variables.getVariableValue(varName));
			}
			else {
				String varName = sb.substring(start, lIndex);
				sb.replace(fIndex, lIndex + 1, this.variables.getVariableValue(varName));
			}
			
			fIndex = sb.indexOf(this.pre);
		}
		
		result = sb.toString();
		
		this.cache.put(source, result);
		
		return result;
	}
}
