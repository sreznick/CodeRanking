package ru.hse.coderank.analysis.main;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import ru.hse.coderank.analysis.asm.ClassDescriptor;
import ru.hse.coderank.analysis.asm.Configuration;
import ru.hse.coderank.analysis.graph.Graph;
import ru.hse.coderank.analysis.graph.MethodNode;
import ru.hse.coderank.analysis.graph.Node;
import ru.hse.coderank.analysis.pagerank.PageGraph;

// temporary argument: "/home/olesya/HSE_2020-1/java/maze/out/artifacts/maze_jar/maze.jar"
// "/home/olesya/github_scala/scala/out/artifacts/scala_jar2/scala.jar"
// "/home/olesya/Downloads/junit-4.13.2.jar"
// "/home/olesya/HSE_2020-1/JARsmth/scala-library-2.12.13.jar"

public class Main {

    public static Graph<MethodNode> graph = new Graph<>();

    public static void main(String[] args) throws IOException {
        long time = System.currentTimeMillis();
        String jarPath = args[0];
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> entries = jarFile.entries();
        new Configuration();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
//            System.out.println(name);
            if (name.endsWith(".class") && Configuration.processPackage(name)) {
                try (InputStream stream = new BufferedInputStream(jarFile.getInputStream(entry), 1024)) {
                    ClassReader re = new ClassReader(stream);
                    ClassDescriptor cv = new ClassDescriptor(stream);
                    re.accept(cv, 0);
                }
            }
        }

        graph.constructGraph();
//        for (Node<MethodNode> m : graph.storage) {
//            System.out.println("\nNEW METHOD");
//            System.out.println(m.payload.getName());
//            if (!graph.edges.get(m).isEmpty()) {
//                System.out.println("EDGES");
//
//                for (Node<MethodNode> me : graph.edges.get(m)) {
//                    System.out.println(me.payload.getName());
//                }
//            }
//        }

        System.out.print("\nSTARTING PAGERANK AT ");
        System.out.println(System.currentTimeMillis() - time);
        PageGraph<MethodNode> pageGraph = new PageGraph<>(graph.storage, graph.edges, graph.parents);
        pageGraph.launchPageRank(50);
        pageGraph.getPageRank();

        long usedBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.print("FINAL TIME: ");
        System.out.println(System.currentTimeMillis() - time);
        System.out.print("FINAL SPACE: ");
        System.out.println(usedBytes);
    }
}

