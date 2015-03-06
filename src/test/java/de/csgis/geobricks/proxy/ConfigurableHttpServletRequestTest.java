package de.csgis.geobricks.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

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
		when(original.getHeader("myheader")).thenReturn("myvalue");
		assertEquals("myvalue", wrapper.getHeader("myheader"));
	}

	@Test
	public void returnsCustomHeaderValue() {
		wrapper.addHeader("myheader", "myvalue");
		assertEquals("myvalue", wrapper.getHeader("myheader"));
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
	public void setBodyAndUseReader() throws Exception {
		String originalBody = "original_body";
		String newBody = "new_body";
		when(original.getReader()).thenReturn(
				new BufferedReader(new StringReader(originalBody)));
		wrapper.setBody(newBody);

		assertEquals(newBody, wrapper.getReader().readLine());
		assertEquals(newBody.length(), wrapper.getContentLength());
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
		assertEquals(newBody, reader.readLine());
		assertEquals(newBody.length(), wrapper.getContentLength());
	}
}
