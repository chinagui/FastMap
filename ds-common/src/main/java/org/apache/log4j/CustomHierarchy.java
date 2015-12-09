package org.apache.log4j;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-8-1
 * Time: 下午3:07
 */
public class CustomHierarchy extends Hierarchy {
    /**
     * Create a new logger hierarchy.
     *
     * @param root The root of the new hierarchy.
     */
    public CustomHierarchy(Logger root) {
        super(root);
    }

    public void remove(String name) {
        synchronized (ht) {
            ht.remove(new CategoryKey(name));
        }
    }


}
