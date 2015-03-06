package de.csgis.geobricks.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.proxy.ProxyServlet;

import de.csgis.geobricks.Geobricks;

/**
 * Reverse proxy for GeoServer requests.
 * 
 * <ul>
 * <li>If the user is not authenticated, it returns a 403 (Forbidden) error.</li>
 * <li>If the user is authenticated, it adds an HTTP header with the user roles
 * to the request and passes the request to GeoServer.</li>
 * </ul>
 * 
 * Note that this servlet only checks authentication. It is possible that a
 * logged user is not authorized to see the requested resource. This is handled
 * by GeoServer using the provided user roles.
 * 
 * @author vicgonco
 */
public abstract class AbstractProxyServlet extends ProxyServlet {
	private static final Logger logger = Logger
			.getLogger(AbstractProxyServlet.class);

	public static final String PROP_HEADER_NAME = "de.csgis.geobricks.login.header_name";
	public static final String PROP_PROXY_URL = "de.csgis.geobricks.login.proxy_url";

	private Properties config;

	@Override
	public void init() throws ServletException {
		super.init();

		try {
			config = new Properties();
			// Increase buffer size for both request and response to 64k
			getHttpClient().setRequestBufferSize(64 * 1024);
			getHttpClient().setResponseBufferSize(64 * 1024);
			File conf = new File(getServletContext().getAttribute(
					Geobricks.ATTR_CONF_DIR).toString(), "app.properties");
			config.load(new FileInputStream(conf));
		} catch (IOException e) {
			logger.error("Cannot read app.properties file");
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String user = getAuthorizedUser(req, resp);
		if (user != null) {
			// We need to set this here so the async Jetty proxy works as
			// expected. Supposedly it should work simply with async-supported
			// on web-fragment.xml and Servlet 3.0, but it does not in our
			// environment.
			req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);

			ConfigurableHttpServletRequest wrapper = new ConfigurableHttpServletRequest(
					req);
			wrapper.addHeader(getConfig().getProperty(PROP_HEADER_NAME), user);
			modifyRequest(wrapper, resp);

			doReverseProxy(wrapper, resp);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	Properties getConfig() {
		return config;
	}

	/**
	 * We have separated this method for testing purposes.
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	void doReverseProxy(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.service(req, resp);
	}

	@Override
	protected URI rewriteURI(HttpServletRequest request) {
		return URI.create(getConfig().getProperty(PROP_PROXY_URL) + "?"
				+ request.getQueryString());
	}

	protected abstract String getAuthorizedUser(HttpServletRequest request,
			HttpServletResponse response) throws IOException;

	protected abstract void modifyRequest(
			ConfigurableHttpServletRequest request, HttpServletResponse response)
			throws IOException;
}
