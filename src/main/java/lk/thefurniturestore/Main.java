package lk.thefurniturestore;

import lk.thefurniturestore.config.AppConfig;
import lk.thefurniturestore.listener.ContextPathListener;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.File;
import java.net.URISyntaxException;

public class Main {
    private static final int SERVER_PORT = Integer.getInteger("server.port", 8080);
    private static final String CONTEXT_PATH = "/thefurniturestore";

    public static void main(String[] args) {
        try {
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(SERVER_PORT);
            tomcat.getConnector();

            File webAppDirectory = resolveWebAppDirectory();
            System.out.println("Serving web files from: " + webAppDirectory.getAbsolutePath());

            Context context = tomcat.addWebapp(CONTEXT_PATH, webAppDirectory.getAbsolutePath());
            context.setParentClassLoader(Main.class.getClassLoader());
            Tomcat.addServlet(context, "JerseyServlet", new ServletContainer(new AppConfig()));
            context.addServletMappingDecoded( "/api/*", "JerseyServlet");

            context.addApplicationListener(ContextPathListener.class.getName());

            tomcat.start();
            System.out.println("App URL: http://localhost:" + SERVER_PORT + CONTEXT_PATH + "/");
            System.out.println("App URL: https://stipulate-chirping-material.ngrok-free.dev/thefurniturestore");
            tomcat.getServer().await();

        }catch(LifecycleException e){
            throw new RuntimeException("Tomcat Embedded Server loading failed: " + e.getMessage());
        }
    }

    private static File resolveWebAppDirectory() {
        File fromWorkingDirectory = findWebAppDirectory(new File(System.getProperty("user.dir")));
        if (fromWorkingDirectory != null) {
            return fromWorkingDirectory;
        }

        try {
            File codeLocation = new File(Main.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            File fromCodeLocation = findWebAppDirectory(codeLocation);
            if (fromCodeLocation != null) {
                return fromCodeLocation;
            }
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Could not resolve the application location.", exception);
        }

        throw new IllegalStateException(
                "Could not find src/main/webapp. Run the application from the project directory.");
    }

    private static File findWebAppDirectory(File startingDirectory) {
        File directory = startingDirectory.isDirectory()
                ? startingDirectory
                : startingDirectory.getParentFile();

        while (directory != null) {
            File webAppDirectory = new File(directory, "src/main/webapp");
            if (webAppDirectory.isDirectory()) {
                return webAppDirectory;
            }
            directory = directory.getParentFile();
        }
        return null;
    }
}
