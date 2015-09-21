package com.fanatics.parser.services;

import java.util.concurrent.ConcurrentHashMap;

public class GlobalServiceProvider {

	private ConcurrentHashMap<Integer,Object> store = null;
	
	private GlobalServiceProvider(){ 
		store = new ConcurrentHashMap<>();
	}
	
	public static GlobalServiceProvider getInstance(){
		return GlobalServiceProviderHolder.globalServiceProvider;
	}
	
	public void addGlobalProperty(int key , Object value){
		store.putIfAbsent(key, value);
	}
	
	public Object getProperty(int key){
		return store.get(key);
	}
	
	public void destroy() {
		if(store != null){
			store.clear();
			store = null;
		}
	}
	
	private static class GlobalServiceProviderHolder {
		static GlobalServiceProvider globalServiceProvider = new GlobalServiceProvider();
	}

}
