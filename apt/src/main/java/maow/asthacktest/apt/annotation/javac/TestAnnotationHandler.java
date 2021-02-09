package maow.asthacktest.apt.annotation.javac;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import static com.sun.tools.javac.tree.JCTree.*;

@Deprecated
public class TestAnnotationHandler extends TreeTranslator {
    private final TreeMaker make;
    private final Names names;

    public TestAnnotationHandler(TreeMaker make, Names names) {
        this.make = make;
        this.names = names;
    }

    @Override
    public void visitClassDef(JCClassDecl clazz) {
        super.visitClassDef(clazz);
        addMemeMethod(clazz);
        addField(clazz);
        result = clazz;
    }

    private void addMemeMethod(JCClassDecl clazz) {
        // Modifiers : public
        final JCModifiers mods = make.Modifiers(Flags.PUBLIC);
        // Name : "meme"
        final Name name = getName("meme");
        // Return : void
        final JCExpression returnType = make.TypeIdent(TypeTag.VOID);
        // Body : System.out.println("meme school")
        final JCBlock block = getMemeMethodBody();
        // Create
        final JCMethodDecl method = make.MethodDef(
                        mods,
                        name,
                        returnType,
                        List.nil(), // params
                        List.nil(), // types
                        List.nil(), // throws
                        block,
                        null
                );
        // Add & Return
        clazz.defs = clazz.defs.append(method);
    }

    private JCBlock getMemeMethodBody() {
        // System.out.println("meme school")
        final JCStatement memeSchoolPrintln = getPrintlnCall("meme school");
        // if (true) { System.out.println("cheese school") }
        final JCStatement cheeseSchoolPrintln = getPrintlnCall("cheese school");
        final JCStatement ifStatement = getIfStatement(cheeseSchoolPrintln);

        return make.Block(0L, List.of(memeSchoolPrintln, ifStatement)); // Finish method body
    }

    private JCStatement getPrintlnCall(String message) {
        JCExpression println = make.Ident(getName("System"));
        println = make.Select(println, getName("out"));
        println = make.Select(println, getName("println"));
        final JCLiteral string = make.Literal(message);
        println = make.Apply(List.nil(), println, List.of(string));
        return make.Exec(println);
    }

    private JCStatement getIfStatement(JCStatement ifTrue) {
        final JCExpression memeString = make.Literal("meme"); // "meme"
        JCExpression equalsExpr = make.Select(memeString, getName("equals")); // "meme".equals
        equalsExpr = make.Apply(List.nil(), equalsExpr, List.of(memeString)); // "meme".equals("meme")
        return make.If(equalsExpr, ifTrue, null); // if ("meme".equals("meme")) <statement>
    }

    private void addField(JCClassDecl clazz) {
        // Modifiers : private final
        final JCModifiers mods = make.Modifiers(Flags.PRIVATE | Flags.FINAL);
        // Name : memerTown
        final Name name = getName("memerTown");
        // Type : String
        final JCExpression stringType = make.Ident(getName("String"));
        // Value : "occupants: 0"
        final JCExpression value = make.Literal("occupants: 0");
        final JCVariableDecl field = make.VarDef(
                mods,
                name,
                stringType,
                value
        );
        clazz.defs = clazz.defs.append(field);
    }

    private Name getName(String name) {
        return names.fromString(name);
    }
}
