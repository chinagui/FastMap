package com.navinfo.dataservice.codegen;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.DateUtils;

import com.navinfo.dataservice.codegen.db.Column;
import com.navinfo.dataservice.codegen.db.Table;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/** 
 * @ClassName: CodeGen
 * @author MaYunFei
 * @date 上午10:20:13
 * @Description: CodeGen.java
 */
public class CodeGen {
	private static final char SEPARATOR = '_';
	private String packageName;
	private Table table;
	private String destPath;
	private String modelClassName;
	private Configuration cfg;
	
	
	/**
	 * @param packageName
	 * @param tableName
	 * @param destPath
	 * @throws IOException 
	 */
	public CodeGen(String packageName, Table table,String destPath) throws IOException {
		super();
		this.packageName = packageName;
		this.table = table;
		this.destPath = destPath;
		this.modelClassName=StringUtils.capitalize(StringHelper.toCamelCase(this.table.getTableName()));
		this.cfg = this.initFreeMakerConfig();
		
	}
	private  Configuration initFreeMakerConfig() throws IOException{
		 /* Create and adjust the configuration singleton */
        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(new File(getTempaltePath()));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        return  cfg;
	}
	private void genCode(String templateFile,String fileSuffix) throws Exception{
		/* Create a data-model */
        Map<String,Object> root = new HashMap<String,Object>();
		root.put("date", DateUtils.formatDate(new Date(), "yyyy-MM-dd hh:mm:ss"));
        root.put("package", this.packageName);
        root.put("modelClassName", this.modelClassName);
        root.put("tableName", this.table.getTableName() );
        List<Column> columns = this.table.getTableColumns(); 
        root.put("columns", columns);
		root.put("modelAttrs", createModelAttrs(columns));
        /* Get the template (uses cache internally) */
        Template temp = this.cfg.getTemplate(templateFile);

        /* Merge data-model with template */
        String[] arr = {this.destPath,this.packageName.replace('.', File.separatorChar)};
        String fullDestPath = StringUtils.join(arr,File.separator);
        File destPath  = new File(fullDestPath );
        if (!destPath.exists()){
        	destPath.mkdirs();
        }
        String  destFile = fullDestPath+File.separatorChar+this.modelClassName+fileSuffix;
        OutputStream fileOut= new FileOutputStream(destFile);
		Writer out = new OutputStreamWriter(fileOut);
        temp.process(root, out);
	}
	private void genController() throws Exception{
		this.genCode("controller.ftlh", "Controller.java");
	}
	private void genService() throws Exception{
		this.genCode("service.ftlh", "Service.java");
	}
	private void genModel() throws Exception{
		this.genCode("modelPoi.ftlh", ".java");
	}

	/**
	 * @param columns
	 * @return
	 */
	private List<String> createModelAttrs( List<Column>  columns) {
		List<String> modelAttrs = new ArrayList<String>();
		for (Column col:columns){
			modelAttrs.add(StringUtils.capitalize(col.getCamelName()));
		}
		return modelAttrs;
	}
	private static String getTempaltePath(){
		String curPath = (CodeGen.class.getResource ("").getFile ());
	 	String templatePath = curPath+File.separator+"templates";
	 	return templatePath;
	}
	
	/**
	 * @param args
	 */
//	 public static void main(String[] args) throws Exception {
//	    Table table = new Table("jdbc:oracle:thin:@192.168.4.131:1521/orcl","fm_man_sp5","fm_man_sp5","grid");
//	 	CodeGen codeGen = new CodeGen("com.navinfo.dataservice.web.codegen",table,"E:\\workspace\\github2\\DataService\\code-generator\\src\\main\\java");
////	 	codeGen.genController();
////	 	codeGen.genService();
//	 	codeGen.genModel();
//	    }
	 public static void main(String[] args) throws Exception {
		    Table table = new Table("jdbc:oracle:thin:@192.168.4.61:1521/orcl","fm_regiondb_sp6_d_1","fm_regiondb_sp6_d_1","IX_POI_CARRENTAL");
		    CodeGen codeGen = new CodeGen("com.navinfo.dataservice.engine.editplus.model.ixpoi",table,"E:\\Users\\songdongyan\\fm_road\\DataService\\engine\\engine-editplus\\src\\main\\java");
//	 	codeGen.genController();
//		 	codeGen.genService();
		 	codeGen.genModel();
		    }
	
}
