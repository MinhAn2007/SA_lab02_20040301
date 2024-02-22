package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static org.example.CommonOperations.listMethodCalls;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        File file=new File("D:\\An\\SA_lab02_20040301\\ProjectTest\\src\\main\\java");
//		JavaParser parser=new JavaParser();
//		ParseResult<CompilationUnit> cu=parser.parse(file);
        CompilationUnit cu1= StaticJavaParser.parse(file);
        List<MethodDeclaration> methods= cu1.findAll(MethodDeclaration.class);
        for(MethodDeclaration m:methods)
            System.out.println(m.getNameAsString());
        listMethodCalls(file);

    }
}