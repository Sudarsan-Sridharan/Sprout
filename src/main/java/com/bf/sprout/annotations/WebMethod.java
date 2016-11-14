/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bf.sprout.annotations;

import com.bf.sprout.dispatcher.RequestType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.atteo.classindex.IndexAnnotated;

/**
 *
 * @author Brandon Alexander Ragland
 */
@IndexAnnotated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) 
public @interface WebMethod{
	
	String path() default "";
	
	RequestType requestType() default RequestType.BOTH;
}
