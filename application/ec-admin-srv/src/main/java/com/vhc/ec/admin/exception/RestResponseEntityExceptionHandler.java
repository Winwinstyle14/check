package com.vhc.ec.admin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { Exception.class })
    public @ResponseBody ResponseEntity<CustomResponseWrapper> handleException(HttpServletRequest request,
                                                                               CustomResponseWrapper responseWrapper){

        return ResponseEntity.ok(responseWrapper);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomResponseWrapper> handleIOException(HttpServletRequest request, CustomException e){
        var errorList = new RestErrorList(HttpStatus.NOT_ACCEPTABLE, e.getErrorCode());
        var responseWrapper = new CustomResponseWrapper(null, Collections.singletonMap("status", HttpStatus.NOT_ACCEPTABLE), errorList);
        return ResponseEntity.ok(responseWrapper);
    }
}
