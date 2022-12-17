package com.meta.handler.exceptions;

public class NotenoughCashException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public NotenoughCashException(String msg) {
		super(msg);
	}
}
