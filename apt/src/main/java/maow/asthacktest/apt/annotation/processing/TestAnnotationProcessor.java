package maow.asthacktest.apt.annotation.processing;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import maow.asthacktest.apt.annotation.TestAnnotation;
import maow.asthacktest.apt.annotation.javac.TestAnnotationHandler;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@Deprecated
@SupportedAnnotationTypes({ "maow.asthacktest.apt.annotation.TestAnnotation" })
//@AutoService(Processor.class)
public class TestAnnotationProcessor extends AbstractProcessor {
    private Trees trees;
    private TreeMaker make;
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.trees = Trees.instance(env);
        final Context context = ((JavacProcessingEnvironment) env).getContext();
        this.make = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (!env.processingOver()) {
            final Set<? extends Element> elements = env.getElementsAnnotatedWith(TestAnnotation.class);
            for (Element element : elements) {
                if (element.getKind() == ElementKind.CLASS) {
                    final JCTree tree = (JCTree) trees.getTree(element);
                    final TreeTranslator translator = new TestAnnotationHandler(make, names);
                    tree.accept(translator);
                }
            }
        }
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
