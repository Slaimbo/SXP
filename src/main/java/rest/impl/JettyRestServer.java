package rest.impl;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.http.HttpVersion;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import org.eclipse.jetty.webapp.WebAppContext;








import java.io.IOException;

import com.google.common.reflect.ClassPath;

import rest.api.RestServer;
import rest.api.ServletPath;

public class JettyRestServer implements RestServer{
	
	private ServletContextHandler context;
	private Server server;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(String packageName) {
		context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
			for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
			  if (info.getName().startsWith(packageName + ".")) {
			    final Class<?> clazz = info.load();
			    ServletPath path = clazz.getAnnotation(ServletPath.class);
				if(path == null) {
					continue;
				}
				ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, path.value());
				jerseyServlet.setInitOrder(0);
				jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", clazz.getCanonicalName());
			  }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        /*for(Class<?> c : entryPoints) {
        	
        	ServletPath path = c.getAnnotation(ServletPath.class);
        	if(path == null) {
        		throw new RuntimeException("No servlet path annotation on class " + c.getCanonicalName());
        	}
        	ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, path.value());
        	jerseyServlet.setInitOrder(0);
        	jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", c.getCanonicalName());
        }*/
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(int port) throws Exception {
		server = new Server();

		
		// Http config
		HttpConfiguration http_config = new HttpConfiguration();
		http_config.setSecureScheme("https");
		http_config.setSecurePort(port);
		http_config.setOutputBufferSize(38768);
		/*
		// Http Connector
		ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config) );
		http.setPort(port);
		//http.setIdleTimeout(30000);
		*/
		// SSL Context factory for HTTPS
		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath("./keystore.jks");
		sslContextFactory.setKeyStorePassword("123456");
		sslContextFactory.setKeyManagerPassword("123456");

		// HTTPS Config
		HttpConfiguration https_config = new HttpConfiguration(http_config);
		SecureRequestCustomizer src = new SecureRequestCustomizer();
		src.setStsMaxAge(2000);
		src.setStsIncludeSubDomains(true);
		https_config.addCustomizer(src);
		
		// HTTPS Connector
		ServerConnector https = new ServerConnector(server,
						    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
						    new HttpConnectionFactory(https_config));
		https.setPort(port);
		https.setIdleTimeout(500000);

		server.setConnectors(new Connector[] {https}); //http, 
	
		

			
        server.setHandler(context);
		server.start();
        server.join();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() {
		server.destroy();
	}

}
