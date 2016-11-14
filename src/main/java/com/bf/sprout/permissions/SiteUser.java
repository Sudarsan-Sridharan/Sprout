/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bf.sprout.permissions;

import java.io.Serializable;

/**
 *
 * @author Brandon Alexander Ragland
 */
public class SiteUser implements Serializable{
	protected Permissions permissions;
	protected SiteRole role;
	
	protected String name;
	protected String description;

	public Permissions getPermissions(){
		return permissions;
	}

	public void setPermissions(Permissions permissions){
		this.permissions = permissions;
	}

	public SiteRole getRole(){
		return role;
	}

	public void setRole(SiteRole role){
		this.role = role;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getDescription(){
		return description;
	}

	public void setDescription(String description){
		this.description = description;
	}
	
}
