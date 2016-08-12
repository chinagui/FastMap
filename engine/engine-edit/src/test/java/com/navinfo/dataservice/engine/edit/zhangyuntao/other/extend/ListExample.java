package com.navinfo.dataservice.engine.edit.zhangyuntao.other.extend;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Title: ListExample.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月11日 下午2:23:47
 * @version: v1.0
 */
public class ListExample {

	public ListExample() {
	}

	public static void main(String[] args) {
		List<Parent> parents = new ArrayList<Parent>();
		Parent parent = new Parent();
		parent.a = 1;
		parents.add(parent);
		System.out.println("listSize:" + parents.size());
		parent = new Parent();
		parent.a = 2;
		parents.add(parent);
		System.out.println("listSize:" + parents.size());

		// for (int i = parents.size() - 1; i >= 0; i--) {
		// parents.remove(i);
		// System.out.print("i:" + i);
		// System.out.println(",listSize:" + parents.size());
		// }
		// System.out.println(parents.size());

		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);

		for (Iterator<Integer> iter = list.iterator(); iter.hasNext();) {
			System.out.println("before");
			int i = iter.next();
			System.out.println("after");
			if (i == 3) {
				System.out.println("beforeremove");
				list.remove(i);
				System.out.println("afterremove");
			}
		}
	}
}
