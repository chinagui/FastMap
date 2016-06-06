package ${package}.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

/** 
* @ClassName:  ${modelClassName} 
* @author code generator
* @date ${date} 
* @Description: TODO
*/
public class ${modelClassName}  {
	<#list columns as column>
	private ${column.javaType} ${column.camelName} ;
	</#list>
	
	public ${modelClassName} (){
	}
	
	public ${modelClassName} (<#list columns as column><#if column?is_first>${column.javaType} ${column.camelName} <#else>,${column.javaType} ${column.camelName}</#if></#list>){
		<#list columns as column>
		this.${column.camelName}=${column.camelName} ;
		</#list>
	}
	<#list columns as column>
	public ${column.javaType} get${column.camelName?cap_first}() {
		return ${column.camelName};
	}
	public void set${column.camelName?cap_first}(${column.javaType} ${column.camelName}) {
		this.${column.camelName} = ${column.camelName};
	}
	</#list>
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "${modelClassName} [<#list columns as column><#if column?is_first>${column.camelName}=" + ${column.camelName} +<#else>",${column.camelName}="+${column.camelName}+</#if></#list>"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		<#list columns as column>
		result = prime * result + ((${column.camelName} == null) ? 0 : ${column.camelName}.hashCode());
		</#list>
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		${modelClassName} other = (${modelClassName}) obj;
		<#list columns as column>
		if (${column.camelName} == null) {
			if (other.${column.camelName} != null)
				return false;
		} else if (!${column.camelName}.equals(other.${column.camelName}))
			return false;
		</#list>		
		return true;
	}
	
	
	
}
