package org.zenframework.z8.compiler.workspace;

import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.zenframework.z8.compiler.file.File;
import org.zenframework.z8.compiler.file.FileException;

public class StartupCodeGenerator {
    private static final String RuntimePostfix = "Runtime";
    private static final String RuntimePackage = "org.zenframework.z8.";

    public static final IPath Z8RuntimePath = new Path("META-INF/z8.runtime");

    static public boolean generate(Project project) {
        try {
            // генерация runtime-класса
            generateResource(project, getRuntimeJavaPath(project), getRuntimeClassContent(project));
            // генерация META-INF/z8.runtime
            generateResource(project, Z8RuntimePath, getRuntimeClassQualifiedName(project));
            return true;
        } catch (FileException e) {
            project.error(e);
            return false;
        }
    }

    public static IPath getRuntimeJavaPath(Project project) {
        return new Path(getRuntimeClassQualifiedName(project).replace('.', '/') + ".java");
    }

    public static IPath getRuntimeClassPath(Project project) {
        return new Path(getRuntimeClassQualifiedName(project).replace('.', '/') + ".class");
    }

    public static String getRuntimeClassQualifiedName(Project project) {
        return RuntimePackage + getRuntimeClassSimpleName(project);
    }

    public static String getRuntimeClassSimpleName(Project project) {
        String projectName = project.getName();
        StringBuilder str = new StringBuilder(projectName.length() + RuntimePostfix.length());
        str.append(Character.toUpperCase(projectName.charAt(0)));
        boolean toUpper = false;
        for (int i = 1; i < projectName.length(); i++) {
            char c = projectName.charAt(i);
            if (isSymbolAllowed(c)) {
                str.append(toUpper ? Character.toUpperCase(c) : c);
                toUpper = false;
            } else {
                toUpper = true;
            }
        }
        str.append(RuntimePostfix);
        return str.toString();
    }
    
    private static boolean isSymbolAllowed(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= 'а' && c <= 'я' || c >= 'А' && c <= 'Я' || c >= '0' && c <= '9';
    }

    private static void generateResource(Project project, IPath resourcePath, String content) throws FileException {
        // генерация runtime-класса
        IPath outputPath = project.getOutputPath().append(resourcePath);
        IPath folder = outputPath.removeLastSegments(1);

        String oldContent = "";

        try {
            oldContent = new String(File.fromPath(outputPath).read());
        } catch (FileException e) {} catch (UnsupportedEncodingException e) {}

        if (!content.equals(oldContent)) {
            File.fromPath(folder).makeDirectories();
            File.fromPath(outputPath).write(content);
        }
    }

    private static String getRuntimeClassContent(Project project) {
        CompilationUnit[] compilationUnits = project.getCompilationUnits();

        String addTable = "";
        String addJob = "";
        String addEntry = "";
        String addActivator = "";

        for (CompilationUnit compilationUnit : compilationUnits) {
            StartupCodeLines startupCodeLines = compilationUnit.getStartupCodeLines();

            if (startupCodeLines.addTable != null) {
                addTable += "\t\t" + startupCodeLines.addTable + '\n';
            }

            if (startupCodeLines.addJob != null) {
                addJob += "\t\t" + startupCodeLines.addJob + '\n';
            }

            if (startupCodeLines.addEntry != null) {
                addEntry += "\t\t" + startupCodeLines.addEntry + '\n';
            }

            if (startupCodeLines.addActivator != null) {
                addActivator += "\t\t" + startupCodeLines.addActivator + '\n';
            }
        }

        String className = getRuntimeClassSimpleName(project);

        return "package org.zenframework.z8;\n\n" +
               "import org.zenframework.z8.server.runtime.*;\n" +
               "public final class " + className + " extends org.zenframework.z8.server.runtime.AbstractRuntime\n" + '{' + '\n' +
                   '\t' + "public " + className + "()\n" +
                   '\t' + '{' + '\n' +
                       addTable + "\n\n" +
                       addEntry + "\n\n" +
                       addJob + "\n\n" +
                       addActivator + "\n\t}\n}";
    }

}