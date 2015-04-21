package org.semanticweb.karma2.exception;

import java.util.concurrent.ExecutionException;

public class QueryExecutionException extends ExecutionException {

	private static final long serialVersionUID = 4082514276158055768L;

	public QueryExecutionException(String msg) {
		super(msg);
	}
	
}
