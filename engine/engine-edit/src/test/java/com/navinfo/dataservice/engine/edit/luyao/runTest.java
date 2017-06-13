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

		String pids="400000135,400000136,400000137,400000138,400000140,400000142,400000143,400000144,400000145,400000146,400000147,400000148,400000149,400000152,400000154,400000156,400000157,400000158,400000162,400000163,400000164,400000165,400000166,400000167,400000168,400000169,400000170,400000171,400000172,400000173,400000174,400000175,400000176,400000177,400000179,400000180,400000182,400000183,400000184,400000185,400000186,400000187,400000188,400000189,400000190,400000191,401000139,401000140,401000141,401000142,401000143,401000144,401000145,401000146,401000147,401000148,401000150,401000151,401000154,401000155,401000156,401000157,401000158,401000159,401000164,401000166,401000168,401000169,401000170,401000171,401000172,401000173,401000174,401000175,401000176,401000177,401000178,401000179,401000180,401000181,401000182,401000183,401000184,401000185,401000186,401000187,401000188,401000189,401000190,401000191,401000192,401000193,401000194,401000197,402000136,402000137,402000138,402000139,402000140,402000141,402000142";



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

	@Test
	public void run_0512_2() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDSAMENODE\",\"dbId\":13,\"subtaskId\":1,\"data\":{\"nodes\":[{\"nodePid\":420000009,\"isMain\":1,\"type\":\"RWNODE\"},{\"nodePid\":420000014,\"isMain\":0,\"type\":\"ZONENODE\"},{\"nodePid\":505000016,\"isMain\":0,\"type\":\"LUNODE\"}]}}";


		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	@Test
	public void run_0512_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDSAMENODE\",\"dbId\":13,\"subtaskId\":1,\"data\":{\"nodes\":[{\"nodePid\":501000150,\"isMain\":1,\"type\":\"RDNODE\"},{\"nodePid\":400000009,\"isMain\":0,\"type\":\"RWNODE\"}]}}";


		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0516_1() throws Exception {

		String parameter = "{\"command\":\"DELETE\",\"dbId\":13,\"type\":\"RWNODE\",\"objId\":506000007,\"infect\":0,\"subtaskId\":1}";



		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0517_1() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDSAMELINK\",\"dbId\":13,\"subtaskId\":1,\"data\":{\"links\":[{\"linkPid\":402000009,\"type\":\"ZONELINK\",\"isMain\":1},{\"linkPid\":405000007,\"type\":\"LULINK\",\"isMain\":0}]}}";


		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}

	@Test
	public void run_0524_1() throws Exception {

		String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDDIRECTROUTE\",\"dbId\":13,\"subtaskId\":1,\"data\":{\"outLinkPid\":403000199,\"rowId\":\"A7296BBE49E04F7D8ECB38CC137214B8\",\"pid\":507000001,\"objStatus\":\"UPDATE\"}}";


		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
	@Test
	public void run_001() throws Exception {

		String parameter = "{\"command\":\"CREATE\",\"type\":\"RDSAMENODE\",\"dbId\":13,\"subtaskId\":29,\"data\":{\"nodes\":[{\"nodePid\":506000034,\"isMain\":1,\"type\":\"ADNODE\"},{\"nodePid\":503000021,\"isMain\":0,\"type\":\"ZONENODE\"}]}}";

		Transaction t = new Transaction(parameter);

		String msg = t.run();
	}
}
