package de.csgis.geobricks.proxy;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AbstractProxyServletTest {
	private AbstractProxyServlet servlet;

	@Before
	public void setup() throws Exception {
		// Prevent/stub logic in super.method()
		servlet = spy(new AbstractProxyServlet() {
			@Override
			protected String getAuthorizedUser(HttpServletRequest request,
					HttpServletResponse response) {
				return null;
			}

			@Override
			protected void modifyRequest(
					ConfigurableHttpServletRequest request,
					HttpServletResponse response) throws IOException {
			}

			@Override
			protected Properties getAppProperties() {
				return new Properties();
			}
		});

		doNothing().when(servlet).doReverseProxy(any(HttpServletRequest.class),
				any(HttpServletResponse.class));
	}

	@Test
	public void sendErrorIfNotLogged() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		servlet.service(request, response);

		verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void proxyWithAddedHeaderIfLogged() throws Exception {
		String user = "valid_user";
		String headerName = "myheader";

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		Properties properties = new Properties();
		properties.put(AbstractProxyServlet.PROP_HEADER_NAME, headerName);

		when(servlet.getAuthorizedUser(request, response)).thenReturn(user);
		when(servlet.getAppProperties()).thenReturn(properties);

		servlet.service(request, response);

		ArgumentCaptor<HttpServletRequest> req = ArgumentCaptor
				.forClass(HttpServletRequest.class);
		verify(servlet).doReverseProxy(req.capture(), eq(response));

		assertEquals(user, req.getValue().getHeader(headerName));
	}
}
