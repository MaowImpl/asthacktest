package maow.asthacktest.apt.util;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.TreeMaker;

import static com.sun.tools.javac.tree.JCTree.JCModifiers;

public enum Modifiers {
    PUBLIC(Flags.PUBLIC),
    PUBLIC_FINAL(Flags.PUBLIC | Flags.FINAL),

    PRIVATE(Flags.PRIVATE),
    PRIVATE_FINAL(Flags.PRIVATE | Flags.FINAL),

    PROTECTED(Flags.PROTECTED),
    PROTECTED_FINAL(Flags.PROTECTED | Flags.FINAL),

    FINAL(Flags.FINAL),
    PARAMETER(Flags.FINAL | Flags.PARAMETER),
    ;

    private final long flags;

    Modifiers(long flags) {
        this.flags = flags;
    }

    JCModifiers getModifiers(TreeMaker make) {
        return make.Modifiers(flags);
    }
}
