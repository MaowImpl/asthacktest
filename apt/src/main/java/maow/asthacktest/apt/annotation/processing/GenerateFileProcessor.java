package maow.asthacktest.apt.annotation.processing;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import maow.asthacktest.apt.annotation.GenerateFile;
import maow.asthacktest.apt.annotation.javac.GenerateFileHandler;
import maow.asthacktest.apt.util.HandlerInfo;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes({ GenerateFileProcessor.GENERATE_FILE })
//@AutoService(Processor.class)
public final class GenerateFileProcessor extends AbstractProcessor {
    public static final String GENERATE_FILE = "maow.asthacktest.apt.annotation.GenerateFile";

    private Messager messager;
    private HandlerInfo info;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        this.messager = env.getMessager();

        final Trees trees = Trees.instance(env);
        final Context context = ((JavacProcessingEnvironment) env).getContext();
        final TreeMaker make = TreeMaker.instance(context);
        final Names names = Names.instance(context);

        info = new HandlerInfo(trees, make, names);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        final Class<GenerateFile> annotationClass = GenerateFile.class;
        if (!env.processingOver()) {
            final Set<? extends Element> elements = env.getElementsAnnotatedWith(annotationClass);
            for (Element element : elements) {
                if (element.getKind() == ElementKind.CLASS) {
                    final GenerateFile annotation = element.getAnnotation(annotationClass);
                    final JCTree tree = (JCTree) info.trees().getTree(element);
                    final TreeTranslator translator = new GenerateFileHandler(info, annotation.name(), annotation.text());
                    tree.accept(translator);
                    messager.printMessage(Diagnostic.Kind.NOTE, "Generated a new file (" + annotation.name() + ") from class : " + element.getSimpleName());
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
