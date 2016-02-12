package com.zanox.lib.simplegraphiteclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;



@RunWith(MockitoJUnitRunner.class)
public class SimpleGraphiteClientTest {

	@Mock
	private Socket socket;	
	private ByteArrayOutputStream out;	
	private SimpleGraphiteClient simpleGraphiteClient;

	private long currentTimestamp;
	
	@Before
	public void setUp() throws IOException {
		out = new ByteArrayOutputStream();
		when(socket.getOutputStream()).thenReturn(out);
		currentTimestamp = System.currentTimeMillis() / 1000;
		
		simpleGraphiteClient = new SimpleGraphiteClient("", 12) {
				@Override
				protected Socket createSocket() {
					return socket;
				}
				
				@Override
				protected long getCurrentTimestamp() {
					return currentTimestamp;
				}
			};
	}
	
	@Test
	public void testSendSingleMetric() throws IOException {
		simpleGraphiteClient.sendMetric("junit.test.metric", 4711, 1l);		
		assertEquals(String.format("junit.test.metric 4711 1%n"), out.toString());
	}

	@Test
	public void testSendSingleMetricCurrentTime() throws IOException, InterruptedException {		
		simpleGraphiteClient.sendMetric("junit.test.metric", 4711);		
		assertEquals(String.format("junit.test.metric 4711 %d%n", currentTimestamp), out.toString());
	}
	
	@Test
	public void testSendMultipleMetrics() {
		Map<String, Number> data = new  HashMap<String, Number>();
		data.put("junit.test.metric1", 4711);
		data.put("junit.test.metric2", 4712);
		simpleGraphiteClient.sendMetrics(data);
		assertTrue(out.toString().contains("junit.test.metric1 4711 " + currentTimestamp));
		assertTrue(out.toString().contains("junit.test.metric2 4712 " + currentTimestamp));
	}
	
	   @Test
	    public void testSendFloatingPointAndLongMetrics() {
	        Map<String, Number> data = new  HashMap<String, Number>();
	        data.put("junit.test.metric1", 4711);
	        data.put("junit.test.metric2", 4712.333);
	        data.put("junit.test.metric3", 4712324723874687236L);
	        data.put("junit.test.metric4", new BigDecimal(3.34, new MathContext(3)));
	        data.put("junit.test.metric5", 4.89767324);
	        simpleGraphiteClient.sendMetrics(data);
	        assertTrue(out.toString().contains("junit.test.metric1 4711 " + currentTimestamp));
	        assertTrue(out.toString().contains("junit.test.metric2 4712.333 " + currentTimestamp));
	        assertTrue(out.toString().contains("junit.test.metric3 4712324723874687236 " + currentTimestamp));
	        assertTrue(out.toString().contains("junit.test.metric4 3.34 " + currentTimestamp));
	        assertTrue(out.toString().contains("junit.test.metric5 4.89767324 " + currentTimestamp));
	    }
	
	@Test
	public void testCurrentTimestamp() {
		long timestamp = new SimpleGraphiteClient("", 0).getCurrentTimestamp();
		assertTrue((System.currentTimeMillis() / 1000) - timestamp < 2);
	}
	
}
