package ${package};

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  ${modelClassName} 
* @author code generator
* @date ${date} 
* @Description: TODO
*/
public class ${modelClassName} extends BasicRow {
	<#list columns as column>
	protected ${column.javaType} ${column.camelName} ;
	</#list>
	
	public ${modelClassName} (long objPid){
		super(objPid);
	}
	
	<#list columns as column>
	public ${column.javaType} get${column.camelName?cap_first}() {
		return ${column.camelName};
	}
	public void set${column.camelName?cap_first}(${column.javaType} ${column.camelName}) {
		if(this.checkValue("${column.name}",this.${column.camelName},${column.camelName})){
			this.${column.camelName} = ${column.camelName};
		}
	}
	</#list>
	
	@Override
	public String tableName() {
		return "IX_POI";
	}
}
