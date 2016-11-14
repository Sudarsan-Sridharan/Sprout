/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bf.sprout.dispatcher;

import com.bf.sprout.annotations.HttpComponent;
import com.bf.sprout.annotations.HttpSocket;
import com.bf.sprout.annotations.RestMethod;
import org.atteo.classindex.ClassIndex;

/**
 *
 * @author Brandon Alexander Ragland
 */
@HttpSocket
public class ComponentManager{

	@RestMethod
	public Object getComponent(String name){
		try{
			for(Class<?> klass : ClassIndex.getAnnotated(HttpComponent.class)){
				HttpComponent annotation = klass.getAnnotation(HttpComponent.class);

				if(annotation.name() != null && !annotation.name().isEmpty() && annotation.name().equalsIgnoreCase(name)){
					return klass.newInstance();
				}else if(klass.getSimpleName().equalsIgnoreCase(name)){
					return klass.newInstance();
				}
			}
		}catch(Exception e){
			//eat because we don't need unnecessaryily long error logs. 
		}
		return null;
	}
}
