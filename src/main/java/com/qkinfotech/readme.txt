这里放置启动类，测试类

module qkinfotech.hibernate  {
  // Version: 

  requires java.base;
  requires java.compiler;
  requires java.instrument;
  requires jdk.unsupported;

  requires jdk.javadoc;
  requires jdk.jshell;
  requires jdk.jdeps;

  uses com.sun.tools.javac.api.JavacTrees;
  uses com.sun.tools.javac.processing.JavacProcessingEnvironment;
  uses com.sun.tools.javac.tree.JCTree;
  uses com.sun.tools.javac.tree.TreeMaker;
  uses com.sun.tools.javac.tree.TreeTranslator;
  uses com.sun.tools.javac.util.Context;
  uses com.sun.tools.javac.util.List;
  uses com.sun.tools.javac.util.Name;
  uses com.sun.tools.javac.util.Names;
  
  uses com.qkinfotech.extension.Composition;
  uses com.qkinfotech.extension.CompositionTreeTranslator;

  uses javax.annotation.processing.Processor;
  
  
  provides javax.annotation.processing.Processor with com.qkinfotech.extension.CompositionProcessor;

  exports com.qkinfotech.extension to qkinfotech.extension;
  
  opens qkinfotech.extension;
  
  opens java.compiler to com.qkinfotech.extension.CompositionTreeTranslator;
  opens java.javadoc to com.qkinfotech.extension.CompositionTreeTranslator;
  opens java.jshell to com.qkinfotech.extension.CompositionTreeTranslator;
  opens java.jdeps to com.qkinfotech.extension.CompositionTreeTranslator;
}