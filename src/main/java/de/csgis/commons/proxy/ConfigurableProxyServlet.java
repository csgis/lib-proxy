package de.csgis.commons.proxy;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.proxy.ProxyServlet;

public class ConfigurableProxyServlet extends ProxyServlet {
	private String destinationUri;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// We need to set this here so the async Jetty proxy works as
		// expected. Supposedly it should work simply with async-supported
		// on web-fragment.xml and Servlet 3.0, but it does not in our
		// environment.
		req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
		super.service(req, resp);
	}

	@Override
	protected URI rewriteURI(HttpServletRequest request) {
		String query = request.getQueryString();
		String uri = destinationUri;
		if (query != null) {
			uri += "?" + query;
		}
		return URI.create(uri);
	}

	public void setDestinationUri(String destinationUri) {
		this.destinationUri = destinationUri;
	}
}
