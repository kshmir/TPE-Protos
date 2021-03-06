package ar.edu.it.itba.pdc.v2.implementations.utils;

import java.util.HashMap;
import java.util.Map;

import ar.edu.it.itba.pdc.v2.interfaces.HTTPHeaders;

/**
 * This class parses an array of bytes and builds up the headers according to de
 * HTTP rfc
 * */
public class HTTPPacket implements HTTPHeaders {

	private Map<String, String> headers;
	private int headerBytes = 0;
	private boolean bodyHeaders = false;
	private boolean contentExpected = true;

	public HTTPPacket() {

		headers = new HashMap<String, String>();

	}

	/**
	 * Asumes all the headers are in the data sent
	 * */
	public void parseHeaders(byte[] data, int count) {

		String s = new String(data).substring(0, count);
		String headers[] = s.split("\r\n");
		String startLine = headers[0];

		if (startLine.contains("GET") || startLine.contains("POST")
				|| startLine.contains("HEAD")) {
			parseRequest(headers);
		} else if (startLine.contains("HTTP")) {
			parseResponse(headers);
		}
	}

	private void parseRequest(String[] message) {

		String[] lines = message;
		String firstLine = lines[0];
		headerBytes += firstLine.length() + 2;
		String[] args = firstLine.split(" ");
		String method = args[0];
		headers.put("Method", method);
		String requestURI = args[1];
		headers.put("RequestedURI", requestURI);
		String httpVersion = args[2];
		headers.put("HTTPVersion", httpVersion);

		parseHeaders(lines);

	}

	private void parseResponse(String[] message) {

		// HTTP-Version SP Status-Code SP Reason-Phrase CRLF
		String[] lines = message;

		String firstLine = lines[0];
		headerBytes += firstLine.length() + 2;
		String[] args = firstLine.split(" ");
		String statusCode = args[1];
		headers.put("StatusCode", statusCode);
		String reason = args[2];
		headers.put("Reason", reason);
		String httpVersion = args[0];
		headers.put("HTTPVersion", httpVersion);

		statusCode = statusCode.replaceAll(" ", "");

		if (statusCode.matches("1..") || statusCode.equals("204")
				|| statusCode.equals("304")) {
			contentExpected = false;
		}

		parseHeaders(lines);
	}

	private void parseHeaders(String[] lines) {
		// will read until an empty line appears
		boolean completeHeaders = false;
		int length = lines.length;
		int i = 1;
		for (i = 1; i < length && !completeHeaders; i++) {
			if (lines[i].isEmpty()) {
				completeHeaders = true;
				headerBytes += 2;
			} else {
				headerBytes += lines[i].length() + 2;
				String[] headerValue = lines[i].split(":");
				// headerValue[1] = headerValue[1].replaceAll(" ", "");
				headers.put(headerValue[0], headerValue[1]);
			}
		}

	}

	@Override
	public String getHeader(String header) {
		return this.headers.get(header);
	}

	@Override
	public int getReadBytes() {
		return headerBytes;
	}

	@Override
	public boolean contentExpected() {
		return contentExpected;
	}

	@Override
	public void dumpHeaders() {
		for (String h : headers.keySet()) {
			System.out.print(h + ": ");
			System.out.println(headers.get(h));
		}

	}
	
	@Override
	public Map<String, String> getAllHeaders() {
		return headers;
	}

}
