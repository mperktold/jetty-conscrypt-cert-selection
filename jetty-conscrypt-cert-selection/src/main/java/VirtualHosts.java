import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.security.Security;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.conscrypt.OpenSSLProvider;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * VirtualHosts.
 *
 * @author Dietmar Hechensteiner
 * @author Matthias Perktold
 * @since  2019-08-17
 */
public class VirtualHosts extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,
		HttpServletResponse response) throws IOException, ServletException
	{
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
        Writer wr = response.getWriter();
		wr.write("<!DOCTYPE HTML>\n");
		wr.write("<html>\n");
		wr.write("<head>\n");
		wr.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">\n");
		wr.write("<title>Virtual hosts</title>\n");
		wr.write("</head>\n");
		wr.write("<body>");
		wr.write("<h3>Virtual hosts</h3>\n");
		wr.write("<p>Virtual host name is " + request.getServerName() + "</p>");
		wr.write("</body>\n");
		wr.write("</html>\n");
		baseRequest.setHandled(true);
	}

	public static void main(String[] args) throws Exception {
		File keystoreFile = new File("src/main/resources/keystore.jks");
		if (!keystoreFile.exists())
			throw new FileNotFoundException(keystoreFile.getAbsolutePath());

		Server server = new Server();

		HttpConfiguration httpConfig = new HttpConfiguration();
		httpConfig.setSecureScheme("https");
		httpConfig.setSecurePort(443);
		httpConfig.setOutputBufferSize(32768);

		ServerConnector http = new ServerConnector(server,
			new HttpConnectionFactory(httpConfig));
		http.setPort(80);
		http.setIdleTimeout(30000);

		SslContextFactory sslContextFactory = new SslContextFactory.Server();
		sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
		sslContextFactory.setKeyStorePassword("123456");
		sslContextFactory.setKeyManagerPassword("123456");

		Security.addProvider(new OpenSSLProvider());
		sslContextFactory.setProvider("Conscrypt");

		HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        SecureRequestCustomizer src = new SecureRequestCustomizer();
        src.setStsMaxAge(2000);
        src.setStsIncludeSubDomains(true);
        httpsConfig.addCustomizer(src);

		ServerConnector https = new ServerConnector(server,
			new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
			new HttpConnectionFactory(httpsConfig));
		https.setPort(443);
		https.setIdleTimeout(500000);

		server.setConnectors(new Connector[]{http, https});
		server.setHandler(new HandlerList(new SecuredRedirectHandler(), new VirtualHosts()));

		server.start();
		server.dumpStdErr();
		server.join();
	}
}
