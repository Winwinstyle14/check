package com.vhc.ec.customer.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;


/**
 * Response Wrapper
 * @author ihuaylupo
 * @version
 * @since Jun 25, 2018
 */
@JsonInclude(NON_NULL)

public class CustomResponseWrapper {

    private Object data;
    private Object metadata;
    private List<ErrorCode> errors;

	/**
	 * @param data
	 * @param metadata
	 * @param errors
	 */
	public CustomResponseWrapper(Object data, Object metadata, List<ErrorCode> errors) {
		super();
		this.data = data;
		this.metadata = metadata;
		this.errors = errors;
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * @return the metadata
	 */
	public Object getMetadata() {
		return metadata;
	}

	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(Object metadata) {
		this.metadata = metadata;
	}

	/**
	 * @return the errors
	 */
	public List<ErrorCode> getErrors() {
		return errors;
	}

	/**
	 * @param errors the errors to set
	 */
	public void setErrors(List<ErrorCode> errors) {
		this.errors = errors;
	}
	
	

    
   
}
