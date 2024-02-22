package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class CommonOperations {

    public void listPackagesAndClasses(File projectDir) {
        new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            System.out.println(path);
            System.out.println("=".repeat(path.length()));
            try {
                new VoidVisitorAdapter<Object>() {
                    //1. Các package trong dự án phải theo mẫu: com.companyname.* (*:tên bất kỳ)
                    @Override
                    public void visit(PackageDeclaration n, Object arg) {
                        super.visit(n, arg);
                        String packageName = n.getNameAsString();
                        if (packageName.matches("com\\.companyname\\..*")) {
                            System.out.println("Package match: " + packageName);
                        } else {
                            System.out.println("Package name does not match pattern com.companyname.*: " + packageName);
                        }
                    }


                    //2. Các class phải có tên là một danh từ hoặc cụm danh ngữ và phải bắt đầu bằng chữ hoa.
                    @Override
                    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
                        super.visit(n, arg);
                        String className = n.getNameAsString();
                        System.out.println(" * " + className);

                        if (!isValidClassName(className)) {
                            System.out.println("Invalid class name: " + className);
                        }
                        checkClassComment(n, className);

                    }

                    //3. 3. Mỗi lớp phải có một comment mô tả cho lớp. Trong comment đó phải có ngày tạo
                    //(created-date) và author.
                    private void checkClassComment(ClassOrInterfaceDeclaration n, String className) {
                        if (!n.getComment().isPresent()) {
                            System.out.println("Missing comment for class: " + className);
                        } else {
                            String commentContent = n.getComment().get().getContent();
                            if (!commentContent.contains("created-date")) {
                                System.out.println("Missing created-date comment ");
                            }
                            if (!commentContent.contains("author")) {
                                System.out.println("Missing author comment ");
                            }
                        }
                    }
                    @Override
                    public void visit(FieldDeclaration n, Object arg) {
                        super.visit(n, arg);
                        n.getVariables().forEach(variable -> {
                            String fieldName = variable.getNameAsString();
                            if (!isValidFieldName(fieldName) || !isValidClassName(fieldName)) {
                                System.out.println("Field name doesn't start with lowercase letter: " + fieldName);
                            }
                        });
                    }

                    private boolean isValidClassName(String className) {
                        return className.matches("[A-Z][a-zA-Z]*") && isNounPhrase(className);
                    }

                    private boolean isNounPhrase(String className) {
                        char firstWord = className.charAt(0);

                        String finalClassName = convertCamelCaseToRealName(className);

                        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
                        String[] tokens = tokenizer.tokenize(finalClassName);

                        InputStream inputStreamPOSTagger = null;
                        InputStream inputStreamChunker = null;
                        try {
                            inputStreamPOSTagger = new FileInputStream("pos/en-pos-maxent.bin");
                            inputStreamChunker = new FileInputStream(("pos/en-chunker.bin"));

                            POSModel posModel = new POSModel(inputStreamPOSTagger);
                            POSTaggerME posTagger = new POSTaggerME(posModel);
                            String tags[] = posTagger.tag(tokens);

                            ChunkerModel chunkerModel = new ChunkerModel(inputStreamChunker);
                            ChunkerME chunker = new ChunkerME(chunkerModel);
                            String[] chunks = chunker.chunk(tokens, tags);

                            for (String chunk : chunks) {
                                if (!chunk.contains("N") || chunk.trim().isEmpty()) {
                                    return false;
                                }
                            }
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        } finally {
                            try {
                                if (inputStreamPOSTagger != null) {
                                    inputStreamPOSTagger.close();
                                }
                                if (inputStreamChunker != null) {
                                    inputStreamChunker.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    private boolean isValidFieldName(String fieldName) {
                        char firstLetter = fieldName.charAt(0);
                        return Character.isLowerCase(firstLetter);
                    }

                    private String convertCamelCaseToRealName(String clsName) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < clsName.length(); i++) {
                            if (i != 0 && clsName.charAt(i) >= 'A' && clsName.charAt(i) <= 'Z') {
                                stringBuilder.append(" ");
                                stringBuilder.append(clsName.charAt(i));
                            } else {
                                stringBuilder.append(clsName.charAt(i));
                            }
                        }
                        return stringBuilder.toString();
                    }
                }.visit(StaticJavaParser.parse(file), null);
                System.out.println();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).explore(projectDir);
    }
}
