package com.qkinfotech.core.security.filter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.web.header.Header;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DiagnosisHeaderWriter implements HeaderWriter {

	private final HeaderWriter delegate;
	
	public DiagnosisHeaderWriter(String nodeName) {
		List<Header> headers = new ArrayList<>(3);
		headers.add(new Header("X-Server-Name", nodeName));
		this.delegate = new StaticHeadersWriter(headers);
	}

	@Override
	public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
		this.delegate.writeHeaders(request, response);
		Long start = (Long)request.getAttribute("diag-start-time");
		if(start != null) {
			long cost = System.currentTimeMillis() - start;
			response.addHeader("X-COST-TIME", cost + "ms");
		}
	}

}
