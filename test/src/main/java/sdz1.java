import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class sdz1 
{
	private class test
	{
		@GET
		@Path("/path")
		public String getId(@PathParam("id")String id)
		{
			System.out.print("coucou");			
			return "caca";
		}
	}
	public static void main (String[] args) throws Exception 
	{
	System.out.print("bite");
	Server server = new Server(8080);

	ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

	server.setHandler(context);
		
	ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/path");
	jerseyServlet.setInitOrder(0);
	jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", "test");

	server.start();
	server.join();
	}
}
