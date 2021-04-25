package CodeRank.app.src.main.impl.graphbuilder;

import CodeRank.app.src.main.impl.graph.Node;
import CodeRank.app.src.main.impl.pagerank.PageRankLauncher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class GraphBuilderLoader<T> {
    private final String graphBuilderLocation;
    private final String graphBuilderName;
    private Class<?> customGraphBuilder;
    public Object instance;

    public GraphBuilderLoader(String graphBuilderLocation, String graphBuilderName) {
        this.graphBuilderLocation = graphBuilderLocation;
        this.graphBuilderName = graphBuilderName;
    }

    public void createInstanceFromDirectory() throws GraphBuilderException {
        try {
            File inputDirectory = new File(graphBuilderLocation);
            ClassLoader classLoader = new URLClassLoader(
                    new URL[]{inputDirectory.toURI().toURL()},
                    this.getClass().getClassLoader()
            );
            customGraphBuilder = classLoader.loadClass(graphBuilderName);
            instance = customGraphBuilder.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new GraphBuilderException("Unable to create instance.");
        }
    }

    public void createInstanceFromJar() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        URL[] urls = {new URL("jar:file:" + graphBuilderLocation + "!/")};
        URLClassLoader classLoader = URLClassLoader.newInstance(urls);
        customGraphBuilder = classLoader.loadClass(graphBuilderName);
        instance = customGraphBuilder.getDeclaredConstructor().newInstance();
    }

    public void loadGraphBuilder() throws GraphBuilderException {
        try {
            Method constructGraph = customGraphBuilder.getMethod("constructGraph");
            constructGraph.invoke(instance);
        } catch (Exception e) {
            // TODO: fix exceptions
            throw new GraphBuilderException("Unable to load graph builder.");
        }
    }

    @SuppressWarnings("unchecked")
    public void applyParameters() throws GraphBuilderException {
        try {
            Method getStorage = customGraphBuilder.getMethod("getGraphStorage");
            Object objectStorage = getStorage.invoke(instance);
            HashSet<Node<T>> builderStorage = (HashSet<Node<T>>) objectStorage;

            Method getGraphEdges = customGraphBuilder.getMethod("getGraphEdges");
            Object objectEdges = getGraphEdges.invoke(instance);
            HashMap<Node<T>, List<Node<T>>> builderEdges = (HashMap<Node<T>, List<Node<T>>>) objectEdges;

            Method getGraphParents = customGraphBuilder.getMethod("getGraphParents");
            HashMap<Node<T>, List<Node<T>>> builderParents = (HashMap<Node<T>, List<Node<T>>>) getGraphParents.invoke(instance);

            PageRankLauncher<T> launcher = new PageRankLauncher<>();
            launcher.launch(builderStorage, builderEdges, builderParents);

        } catch (Exception e) {
            throw new GraphBuilderException("Unable to apply methods.");
        }
    }

    @SuppressWarnings("unchecked")
    public HashSet<Node<T>> applyGetStorage() throws GraphBuilderException {
        try {
            Method getStorage = customGraphBuilder.getMethod("getGraphStorage");
            return (HashSet<Node<T>>) getStorage.invoke(instance);
        } catch (Exception e) {
            throw new GraphBuilderException("Unable to get graph storage.");
        }
    }
}
