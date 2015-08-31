package de.csgis.geobricks.proxy;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ReadListener;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

public class ConfigurableHttpServletRequestTest {
	private HttpServletRequest original;
	private ConfigurableHttpServletRequest wrapper;

	@Before
	public void setup() {
		original = mock(HttpServletRequest.class);
		wrapper = new ConfigurableHttpServletRequest(original);
	}

	@Test
	public void returnsOriginalHeaderValue() {
		String name = "myheader";
		String value = "myvalue";

		when(original.getHeader(name)).thenReturn(value);
		when(original.getHeaders(name)).thenReturn(
				new Vector<String>(Collections.singleton(value)).elements());

		assertEquals(value, wrapper.getHeader(name));
		Enumeration<String> headers = wrapper.getHeaders(name);
		assertEquals(value, headers.nextElement());
		assertFalse(headers.hasMoreElements());
	}

	@Test
	public void returnsCustomHeaderValue() {
		String name = "myheader";
		String value = "myvalue";

		wrapper.addHeader(name, value);
		assertEquals(value, wrapper.getHeader(name));
		Enumeration<String> headers = wrapper.getHeaders(name);
		assertEquals(value, headers.nextElement());
		assertFalse(headers.hasMoreElements());
	}

	@Test
	public void returnsCustomHeaderValues() {
		wrapper.addHeader("myheader", "myvalue");
		Enumeration<String> values = wrapper.getHeaders("myheader");

		assertTrue(values.hasMoreElements());
		assertEquals("myvalue", values.nextElement());
		assertFalse(values.hasMoreElements());
	}

	@Test
	public void customHeadersTakePrecedence() {
		when(original.getHeader("myheader")).thenReturn("myvalue");
		wrapper.addHeader("myheader", "mynewvalue");
		assertEquals("mynewvalue", wrapper.getHeader("myheader"));
	}

	@Test
	public void returnsAllHeaderNames() {
		when(original.getHeaderNames()).thenReturn(
				Collections.enumeration(Collections.singleton("myheader1")));
		wrapper.addHeader("myheader2", "myvalue2");
		ArrayList<String> names = Collections.list(wrapper.getHeaderNames());
		assertEquals(2, names.size());
		assertTrue(names.contains("myheader1"));
		assertTrue(names.contains("myheader2"));
	}

	@Test
	public void returnsOriginalParamValue() {
		String name = "myparam";
		String value = "myvalue";

		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put(name, new String[] { value });

		when(original.getParameter(name)).thenReturn(value);
		when(original.getParameterNames()).thenReturn(
				new Vector<String>(Collections.singleton(name)).elements());
		when(original.getParameterValues(name)).thenReturn(
				new String[] { value });
		when(original.getParameterMap()).thenReturn(map);

		assertEquals(value, wrapper.getParameter(name));
		String[] params = wrapper.getParameterValues(name);
		assertEquals(1, params.length);
		assertEquals(value, params[0]);
	}

	@Test
	public void returnsCustomParamValue() {
		String name = "myheader";
		String value = "myvalue";

		wrapper.addParam(name, value);

		assertEquals(value, wrapper.getParameter(name));
		String[] params = wrapper.getParameterValues(name);
		assertEquals(1, params.length);
		assertEquals(value, params[0]);
	}

	@Test
	public void getParameterNames() {
		String name = "myparam";
		String value = "myvalue";

		// Mock original
		Map<String, String[]> originalMap = new HashMap<String, String[]>();
		originalMap.put(name, new String[] { value });
		when(original.getParameter(name)).thenReturn(value);
		when(original.getParameterNames()).thenReturn(
				new Vector<String>(Collections.singleton(name)).elements());
		when(original.getParameterValues(name)).thenReturn(
				new String[] { value });
		when(original.getParameterMap()).thenReturn(originalMap);

		// Mock wrapper
		wrapper.addParam(name, value + "_diff");
		wrapper.addParam(name + "2", value + "2");

		// Test
		Map<String, String[]> map = wrapper.getParameterMap();
		Enumeration<String> names = wrapper.getParameterNames();

		assertEquals(2, map.size());
		assertArrayEquals(new String[] { value + "_diff" }, map.get(name));
		assertArrayEquals(new String[] { value + "2" }, map.get(name + "2"));
		assertTrue(Arrays.asList(name, name + "2")
				.contains(names.nextElement()));
		assertTrue(Arrays.asList(name, name + "2")
				.contains(names.nextElement()));
		assertFalse(names.hasMoreElements());
	}

	@Test
	public void handlesNull() {
		assertNull(wrapper.getHeader(null));
	}

	@Test
	public void nullQueryString() {
		when(original.getQueryString()).thenReturn(null);
		wrapper.addParam("p1", "v1");
		assertNull(wrapper.getQueryString());
	}

	@Test
	public void validQueryString() {
		when(original.getQueryString()).thenReturn("p1=57&p2=42");
		wrapper.addParam("p1", "v1");
		wrapper.addParam("p3", "v3");
		assertEquals("p1=v1&p2=42&p3=v3", wrapper.getQueryString());
	}

	@Test
	public void setBodyAndUseReader() throws Exception {
		String originalBody = "original_body";
		String newBody = "new_body";
		when(original.getReader()).thenReturn(
				new BufferedReader(new StringReader(originalBody)));
		wrapper.setBody(newBody);

		assertEquals(newBody, wrapper.getReader().readLine());
		assertEquals(newBody.length(), wrapper.getContentLength());
		assertEquals(newBody.length(), wrapper.getContentLengthLong());
	}

	@Test
	public void getContentLengthFromDelegate() throws Exception {
		when(original.getContentLength()).thenReturn(512);
		when(original.getContentLengthLong()).thenReturn(512L);

		assertEquals(512, wrapper.getContentLength());
		assertEquals(512, wrapper.getContentLengthLong());
	}

	@Test
	public void setBodyAndUseStream() throws Exception {
		String originalBody = "original_body";
		String newBody = "new_body";
		when(original.getReader()).thenReturn(
				new BufferedReader(new StringReader(originalBody)));
		wrapper.setBody(newBody);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				wrapper.getInputStream()));
		assertTrue(wrapper.getInputStream().isReady());
		assertEquals(newBody, reader.readLine());
		assertEquals(newBody.length(), wrapper.getContentLength());
		assertFalse(wrapper.getInputStream().isFinished());
	}

	@Test
	public void setBodyInputStreamDoesNotSupportListener() throws Exception {
		String originalBody = "original_body";
		String newBody = "new_body";
		when(original.getReader()).thenReturn(
				new BufferedReader(new StringReader(originalBody)));
		wrapper.setBody(newBody);
		try {
			wrapper.getInputStream().setReadListener(mock(ReadListener.class));
			fail();
		} catch (UnsupportedOperationException e) {
		}
	}
}
