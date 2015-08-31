package de.csgis.geobricks.proxy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Request wrapper to add custom headers and parameters. It is also possible to
 * modify the body of the request.
 * 
 * @author vicgonco
 */
public class ConfigurableHttpServletRequest extends HttpServletRequestWrapper {
	private Map<String, String> headers = new HashMap<String, String>();
	private Map<String, String> params = new HashMap<String, String>();

	private String body;

	public ConfigurableHttpServletRequest(HttpServletRequest request) {
		super(request);
	}

	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	public void addParam(String name, String value) {
		params.put(name, value);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		List<String> headerNames = new ArrayList<String>();
		headerNames.addAll(Collections.list(super.getHeaderNames()));
		headerNames.addAll(headers.keySet());
		return Collections.enumeration(headerNames);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		if (headers.containsKey(name)) {
			return Collections.enumeration(Collections.singleton(headers
					.get(name)));
		} else {
			return super.getHeaders(name);
		}
	}

	@Override
	public String getHeader(String name) {
		String addedHeader = headers.get(name);
		return addedHeader != null ? addedHeader : super.getHeader(name);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		Set<String> paramNames = new HashSet<String>();
		paramNames.addAll(Collections.list(super.getParameterNames()));
		paramNames.addAll(params.keySet());
		return Collections.enumeration(paramNames);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> ret = new HashMap<String, String[]>();

		Map<String, String[]> superParams = super.getParameterMap();
		for (String param : this.params.keySet()) {
			ret.put(param, new String[] { this.params.get(param) });
		}
		for (String superParam : superParams.keySet()) {
			if (!ret.keySet().contains(superParam)) {
				ret.put(superParam, superParams.get(superParam));
			}
		}
		return ret;
	}

	@Override
	public String[] getParameterValues(String name) {
		if (params.containsKey(name)) {
			return new String[] { params.get(name) };
		} else {
			return super.getParameterValues(name);
		}
	}

	@Override
	public String getParameter(String name) {
		if (params.containsKey(name)) {
			return params.get(name);
		} else {
			return super.getParameter(name);
		}
	}

	@Override
	public String getQueryString() {
		String ret = super.getQueryString();

		if (ret == null) {
			return null;
		}

		for (String paramName : params.keySet()) {
			String paramValue = getParameter(paramName);
			if (ret.contains(paramName)) {
				ret = ret
						.replaceAll("(?<=" + paramName + "=)[^&]+", paramValue);
			} else {
				ret += "&" + paramName + "=" + paramValue;
			}
		}

		return ret;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		if (body != null) {
			return new BufferedReader(new StringReader(body));
		} else {
			return super.getReader();
		}
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (body != null) {
			return new StringServletInputStream(body);
		} else {
			return super.getInputStream();
		}
	}

	@Override
	public int getContentLength() {
		return body != null ? body.length() : super.getContentLength();
	}

	@Override
	public long getContentLengthLong() {
		return body != null ? body.length() : super.getContentLengthLong();
	}

	public void setBody(String body) {
		this.body = body;
	}

	private class StringServletInputStream extends ServletInputStream {
		private ByteArrayInputStream delegate;
		private boolean finished;

		public StringServletInputStream(String string) {
			this.finished = false;
			this.delegate = new ByteArrayInputStream(string.getBytes());
		}

		@Override
		public boolean isFinished() {
			return finished;
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setReadListener(ReadListener readListener) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int read() throws IOException {
			int i = delegate.read();
			finished = i == -1;
			return i;
		}
	}
}