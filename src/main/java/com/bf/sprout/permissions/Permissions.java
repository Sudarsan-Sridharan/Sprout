/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bf.sprout.permissions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Brandon Alexander Ragland
 */
public class Permissions implements Serializable{
	private List<String> permissions = new ArrayList<>();
	
	public boolean hasPermission(String permission){
		return permissions.contains(permission);
	}
	
	public boolean addPermission(String permission){
		if(!permissions.contains(permission)){
			permissions.add(permission);
			return true;
		}else{
			return false;
		}
	}
	
	public boolean removePermission(String permission){
		return permissions.remove(permission);
	}
}
