package maow.asthacktest.apt.util;

import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

public final class HandlerInfo {
    private final Trees trees;
    private final TreeMaker make;
    private final Names names;

    public HandlerInfo(Trees trees, TreeMaker make, Names names) {
        this.trees = trees;
        this.make = make;
        this.names = names;
    }

    public Trees trees() {
        return trees;
    }

    public TreeMaker make() {
        return make;
    }

    public Names names() {
        return names;
    }
}
