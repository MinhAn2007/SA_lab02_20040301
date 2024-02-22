package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;


public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        File file=new File("D:\\An\\SA_lab02_20040301\\ProjectTest\\src\\main\\java");

        CommonOperations projectHandler = new CommonOperations();
        projectHandler.listPackagesAndClasses(file);

    }
}