package maow.asthacktest.apt.annotation.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import maow.asthacktest.apt.util.HandlerInfo;
import maow.asthacktest.apt.util.HandlerUtils;
import maow.asthacktest.apt.util.Modifiers;

import static com.sun.tools.javac.tree.JCTree.*;
import static maow.asthacktest.apt.util.Modifiers.*;

/*
    This project is not a long-term project that would usually require javadocs, HOWEVER:
    I realize that this topic is something that a lot of people would want to mess around with or work on.
    Therefore, I'm documenting this class to make my intentions more obvious to help the dev community.

    Overall, this project is just a testing ground for messing around with AST manipulation, which I find very fun.
    As always, this code is MIT-licensed, meaning you may use it any way you want.
*/

/**
 * TreeTranslator that visits classes annotated by GenerateFile.<br>
 * Generates a method that creates a new file when run.
 *
 * @author Maow
 * @since 1.0.0
 */
public class GenerateFileHandler extends TreeTranslator {
    /** Utility class for reducing boilerplate when working with the AST. */
    private final HandlerUtils utils;
    /** Allows the modification of the compiler's source AST. */
    private final TreeMaker make;
    /** The name of the output file. */
    private final String name;
    /** The contents of the output file. */
    private final String text;

    /**
     * Creates a new instance of GenerateFileHandler with the provided info and strings.
     *
     * @param info HandlerInfo instance. Contains instances of Trees, TreeMaker, and Names provided by the annotation processor.
     * @param name Output file name, provided by the annotated element during processing
     * @param text Output file contents, provided by the annotated element during processing
     */
    public GenerateFileHandler(HandlerInfo info, String name, String text) {
        this.utils = new HandlerUtils(info);
        this.make = info.make();
        this.name = name;
        this.text = text;
    }

    /**
     * Visits a class declaration.<br>
     * During this visit, a new <b>writeFile</b> method will be generated and added to this class's methods.
     *
     * @param clazz Class declaration
     */
    @Override
    public void visitClassDef(JCClassDecl clazz) {
        super.visitClassDef(clazz);

        addWriteFileMethod(clazz);

        result = clazz;
    }

    /**
     * Generates a new method called <b>writeFile</b> in the target class and adds it to the class's members.
     *
     * @param clazz Class declaration provided by the visitor methods
     */
    private void addWriteFileMethod(JCClassDecl clazz) {
        final JCModifiers mods = utils.getModifiers(PUBLIC);
        final Name name = utils.getName("writeFile");
        final JCBlock body = generateWriteFileMethodBody();

        final JCMethodDecl method = generateVoidMethod(mods, name, body);
        clazz.defs = clazz.defs.append(method); // Replaces the class members with an appended version of them.
    }

    /**
     * Generates a method body for the generated <b>writeFile</b> method.
     *
     * @return Method body
     */
    private JCBlock generateWriteFileMethodBody() {
        final JCVariableDecl pathVariable = generateImmutableLocal(
                utils.getChainedIdentifier("java.nio.file.Path"),
                utils.getName("path"),
                generateGetPath()
        );
        // CODE: final BufferedWriter bw = Files.newBufferedWriter(Paths.get(<output name here>));
        final JCVariableDecl bwVariable = generateImmutableLocal(
                utils.getChainedIdentifier("java.io.BufferedWriter"),
                utils.getName("bw"),
                generateNewBufferedWriter(pathVariable)
        );
        // CODE:
        // bw.write(<output file text>);
        // bw.flush();
        final List<JCStatement> statements = generateWriteMethods(bwVariable); // Place the buffered writer before the writer calls.
        final JCBlock body = make.Block(0L, statements); // Create body.
        final JCTry tryBlock = wrapInTryBlock(bwVariable, body); // Wrap body in try/catch.
        return make.Block(0L, List.of(pathVariable, tryBlock)); // Return that try/catch wrapped in a new block.
    }

    /**
     * Generates an immutable local variable.
     *
     * @param type The local variable's type
     * @param name The local variable's name
     * @param init The initializer expression for the local variable
     * @return A local variable that is <b>final</b>
     */
    private JCVariableDecl generateImmutableLocal(JCExpression type, Name name, JCExpression init) {
        return make.VarDef(
                utils.getModifiers(FINAL),
                name,
                type,
                init
        );
    }

    /**
     * @return "Files.newBufferedWriter()" expression.
     */
    private JCExpression generateNewBufferedWriter(JCVariableDecl path) {
        JCExpression filesExpr = utils.getChainedIdentifier("java.nio.file.Files"); // Files
        filesExpr = make.Select(filesExpr, utils.getName("newBufferedWriter")); // Files.newBufferedWriter
        filesExpr = make.Apply(
                List.nil(),
                filesExpr,
                List.of(make.Ident(path.name))
        ); // Files.newBufferedWriter(path);
        return filesExpr;
    }

    /**
     * @return "Paths.get()" expression.
     */
    private JCExpression generateGetPath() {
        JCExpression pathExpr = utils.getChainedIdentifier("java.nio.file.Paths"); // Paths
        pathExpr = make.Select(pathExpr, utils.getName("get")); // Paths.get
        pathExpr = make.Apply(
                List.nil(),
                pathExpr,
                List.of(make.Literal(name)) // Arguments - Singleton list of a literal, value being the file name.
        ); // Paths.get(<output file name>);
        return pathExpr;
    }

    /**
     * Generates a write, flush, and close method call.
     *
     * @param writer Instance of BufferedWriter
     * @return Write, flush, and close method call within a list of statements
     */
    private List<JCStatement> generateWriteMethods(JCVariableDecl writer) {
        return List.of(
                generateWriterMethodCall(writer, "write", text), // .write(<output text here>);
                generateWriterMethodCall(writer, "flush", "") // .flush();
        );
    }

    /**
     * Generates a method call using a BufferedWriter instance with an optional arg.
     *
     * @param writer Instance of BufferedWriter
     * @param name Name of the method
     * @param arg A single argument for the method call
     * @return Method call statement
     */
    private JCStatement generateWriterMethodCall(JCVariableDecl writer, String name, String arg) {
        JCExpression nameExpr = make.Ident(writer.name);
        nameExpr = make.Select(nameExpr, utils.getName(name));
        if (arg.equals("")) {
            nameExpr = make.Apply(List.nil(), nameExpr, List.nil());
        } else {
            nameExpr = make.Apply(
                    List.nil(),
                    nameExpr,
                    List.of(make.Literal(arg))
            );
        }
        return make.Exec(nameExpr);
    }

    /**
     * Wraps a method body in a try/catch statement.<br>
     * Note: This method does not provide the ability to generate a <b>finally</b> block.
     *
     * @param body The method body
     * @return A try block containing a method body, as well as a sibling catch block
     */
    private JCTry wrapInTryBlock(JCTree resource, JCBlock body) {
        final JCCatch catchBlock = generateCatchBlock();
        return make.Try(List.of(resource), body, List.of(catchBlock), null);
    }

    /**
     * Generates a catch block for a try/catch statement that catches an {@link java.io.IOException}.
     *
     * @return Instance of a catch block
     */
    private JCCatch generateCatchBlock() {
        final JCExpression type = utils.getChainedIdentifier("java.io.IOException");
        // Exception variable.
        final JCVariableDecl exception = make.VarDef(
                utils.getModifiers(PARAMETER), // Flags the variable as a parameter, as well as makes it final.
                utils.getName("ex"),
                type,
                null
        );
        final JCBlock body = generateCatchBlockBody(exception);
        return make.Catch(exception, body);
    }

    /**
     * Generates a catch block body.<br>
     * This body has no other functionality other than to call {@link Exception#printStackTrace()}.
     *
     * @return Catch block body
     */
    private JCBlock generateCatchBlockBody(JCVariableDecl exception) {
        JCExpression printExpr = make.Ident(exception.name);
        printExpr = make.Select(printExpr, utils.getName("printStackTrace")); // ex.printStackTrace
        printExpr = make.Apply(List.nil(), printExpr, List.nil()); // ex.printStackTrace();
        return make.Block(
                0L,
                List.of(make.Exec(printExpr))
        );
    }

    /**
     * Generates a new void-returning method based on the supplied parameters.
     *
     * @param mods The modifiers of the method
     * @param name The name of the method
     * @param body The method body, contains the method's statements
     * @return A method with return type of "void"
     *
     * @see HandlerUtils#getModifiers(Modifiers)
     * @see HandlerUtils#getName(String)
     */
    private JCMethodDecl generateVoidMethod(JCModifiers mods, Name name, JCBlock body) {
        final JCPrimitiveTypeTree type = utils.getVoidType();
        return make.MethodDef(
                mods,
                name,
                type,
                List.nil(), // Method parameters.
                List.nil(), // Type parameters.
                List.nil(), // Throws clause exceptions list.
                body,
                null
        );
    }
}