package com.fanatics.parser.logger;

public class FanaticLogger {
	
   public static final int ERROR = 1;
   
   public static void log(String classname , String description , int loggingType){
	   if(loggingType == ERROR){
		   description = " ERROR - "+description ;
	   }
	   log(classname , description);
   }
   
   public static void log(String classname , String description){
	   System.out.println(classname +" :: "+description);
   }
   
   
}
