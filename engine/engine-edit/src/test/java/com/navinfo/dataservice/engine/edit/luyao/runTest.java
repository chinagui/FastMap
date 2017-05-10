package com.navinfo.dataservice.engine.edit.luyao;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import org.junit.Before;
import org.junit.Test;

public class runTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void run_00425_2() throws Exception {

		String parameter = "{\"command\":\"BATCH\",\"type\":\"RDNODE\",\"dbId\":13,\"data\":[{\"kind\":2,\"rowId\":\"EFEF28E7830A4F98AC0BFBEDB0A7E67F\",\"pid\":507000045,\"objStatus\":\"UPDATE\"},{\"kind\":2,\"rowId\":\"035D24ECD1C44180B812D3519CF008AF\",\"pid\":510000047,\"objStatus\":\"UPDATE\"}],\"subtaskId\":1}";
		
		
		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00425_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDGSC\",\"dbId\":13,\"subtaskId\":1,\"data\":{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[116.5500470995903,39.73500082461386],[116.55032873153687,39.73500082461386],[116.55032873153687,39.73528340618207],[116.5500470995903,39.73528340618207],[116.5500470995903,39.73500082461386]]]},\"gscPoint\":{\"longitude\":116.55021582673737,\"latitude\":39.735121259117044},\"linkObjs\":[{\"pid\":510000054,\"type\":\"RDLINK\",\"zlevel\":0,\"lineNum\":1},{\"pid\":510000054,\"type\":\"RDLINK\",\"zlevel\":1,\"lineNum\":7}]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00426_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDGSC\",\"dbId\":13,\"subtaskId\":1,\"data\":{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[116.5500470995903,39.73500082461386],[116.55032873153687,39.73500082461386],[116.55032873153687,39.73528340618207],[116.5500470995903,39.73528340618207],[116.5500470995903,39.73500082461386]]]},\"gscPoint\":{\"longitude\":116.55021582673737,\"latitude\":39.735121259117044},\"linkObjs\":[{\"pid\":510000054,\"type\":\"RDLINK\",\"zlevel\":0,\"lineNum\":1},{\"pid\":510000054,\"type\":\"RDLINK\",\"zlevel\":1,\"lineNum\":7}]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_00427_1() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"dbId\":13,\"type\":\"RDLINK\",\"objId\":407000097,\"data\":{\"urban\":1,\"rowId\":\"11D86C9E83CB426FB8E9ABC7F29A1A66\",\"pid\":407000097,\"objStatus\":\"UPDATE\"},\"subtaskId\":1}";


		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0505_1() throws Exception {

		String pids="402000011,402000012,402000017,402000018,402000019,402000020,402000021,402000022,402000023,402000025,402000026,402000027,402000028,402000029,402000030,402000031,402000032,402000033,402000034";




		String parameter = "{\"command\":\"BATCHDELETE\",\"type\":\"RDNODE\",\"dbId\":13,\"subtaskId\":1,\"objIds\":["+pids+"]}";


		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	@Test
	public void run_0505_2() throws Exception {

		String parameter = "{\"command\":\"BATCHDELETE\",\"type\":\"RDLINK\",\"dbId\":13,\"subtaskId\":1,\"objIds\":[400000001,400000002,400000006,400000008,400000009,400000011,400000012,400000013,400000014,400000015,400000016,400000017,400000019,400000021,400000022,400000023,400000027,400000029,400000033]}";


		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

}
