package maow.asthacktest.apt.util;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.util.Name;

import static com.sun.tools.javac.tree.JCTree.*;

public final class HandlerUtils {
    private final HandlerInfo info;

    public HandlerUtils(HandlerInfo info) {
        this.info = info;
    }

    public JCModifiers getModifiers(Modifiers mods) {
        return mods.getModifiers(info.make());
    }

    public Name getName(String name) {
        return info.names().fromString(name);
    }

    public JCPrimitiveTypeTree getVoidType() {
        return info.make().TypeIdent(TypeTag.VOID);
    }

    public JCIdent getIdentifier(String name) {
        final Name nameObj = getName(name);
        return info.make().Ident(nameObj);
    }

    public JCExpression getChainedIdentifier(String string) {
        final String[] strings = string.split("\\.");
        JCExpression e = null;
        for (String string1 : strings) {
            if (e == null)
                e = info.make().Ident(getName(string1));
            else
                e = info.make().Select(e, getName(string1));
        }
        return e;
    }
}
