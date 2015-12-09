package de.csgis.geobricks.proxy;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.proxy.ProxyServlet;

/**
 * Reverse proxy that adds a custom header with user roles.
 * 
 * <ul>
 * <li>If the user is not authenticated, it returns a 404 (Not Found) error.</li>
 * <li>If the user is authenticated, it adds an HTTP header with the user roles
 * to the request and passes the request to the specified URL.</li>
 * </ul>
 * 
 * Subclasses can either implement authorization themselves or rely on the
 * destination component (forward URL) to do that; for example, it's not
 * necessary to check authorization when forwarding requests to GeoServer since
 * it will handle authorization with the given roles.
 * 
 * @author vicgonco
 */
public abstract class AbstractProxyServlet extends ProxyServlet {
	/**
	 * Property name to use for header name.
	 */
	public static final String PROP_HEADER_NAME = "de.csgis.geobricks.login.header_name";
	/**
	 * Property name to use for proxy URL.
	 */
	public static final String PROP_PROXY_URL = "de.csgis.geobricks.login.proxy_url";

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// We need to set this here so the async Jetty proxy works as
		// expected. Supposedly it should work simply with async-supported
		// on web-fragment.xml and Servlet 3.0, but it does not in our
		// environment.
		req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);

		String user = getAuthorizedUser(req, resp);
		if (user != null) {

			ConfigurableHttpServletRequest wrapper = new ConfigurableHttpServletRequest(
					req);
			wrapper.addHeader(getHeaderName(), user);
			modifyRequest(wrapper, resp);

			doReverseProxy(wrapper, resp);
		} else {
			doReverseProxy(req, resp);
		}
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
		return URI.create(getProxyURL() + "?" + request.getQueryString());
	}

	/**
	 * Returns the URL to use to forward requests.
	 * 
	 * @return the destination URL.
	 */
	protected abstract String getProxyURL();

	/**
	 * Returns the name of the header to append to the forwarded request.
	 * 
	 * @return The name of the header.
	 */
	protected abstract String getHeaderName();

	/**
	 * Returns the user for the given HTTP request if authentication is valid.
	 * 
	 * @param request
	 *            The HTTP request with the data to check authentication.
	 * @param response
	 *            The HTTP response, in case it needs to be modified when
	 *            checking the authentication (cookies, headers, etc.)
	 * @return The user if the request provides valid authentication,
	 *         <code>null</code> otherwise.
	 * @throws IOException
	 *             if any I/O error occurs while obtaining the roles.
	 */
	protected abstract String getAuthorizedUser(HttpServletRequest request,
			HttpServletResponse response) throws IOException;

	/**
	 * Modifies the request just before it's forwarded.
	 * 
	 * @param request
	 *            The HTTP request to modify.
	 * @param response
	 *            The HTTP response to modify (cookies, headers, etc.)
	 * @throws IOException
	 *             if any I/O error occurs while modifying the request.
	 */
	protected abstract void modifyRequest(
			ConfigurableHttpServletRequest request, HttpServletResponse response)
			throws IOException;
}
