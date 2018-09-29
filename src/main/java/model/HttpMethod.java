package model;

public enum HttpMethod {
	GET,
	POST;
	
	public boolean isGET() {
		return this == GET;
	}
	
	public boolean isPost() {
		return this == POST;
	}
	
}
