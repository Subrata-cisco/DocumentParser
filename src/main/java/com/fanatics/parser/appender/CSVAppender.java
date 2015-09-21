package com.fanatics.parser.appender;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.fanatics.parser.contracts.IAppender;

public class CSVAppender implements IAppender {

	public enum STATEMENT_OF {
		NA(0), CITIBANK(1) ,HDFCBANK(2) ;
		int type = -1;
		STATEMENT_OF(int size) {
			this.type = size;
		}
		int getType() {
			return type;
		}
	}
	
	private HashSet<String> setOfData = new HashSet<>();
	private int count = 0;
	private STATEMENT_OF CC_TYPE  = STATEMENT_OF.NA;
	
	
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	private static final String HDFC_CC = "Statement for HDFC Bank Credit Card";
	
	private static final String CITIBANK_CC = "INDIANOIL CITIBANK TITANIUM CREDIT CARD ";
	
	
	@Override
	public boolean append(LogEvent event) {
		boolean resume = true;
		count++;
		
		String data = event.getData();
		if(data != null && data.length() == 0){
			return resume;
		}
		
		if(data != null){
			if(CC_TYPE.getType() == STATEMENT_OF.NA.getType() && data.equalsIgnoreCase(CITIBANK_CC)){
				CC_TYPE = STATEMENT_OF.CITIBANK;
			}else if(CC_TYPE.getType() == STATEMENT_OF.NA.getType() && data.equalsIgnoreCase(HDFC_CC)){
				CC_TYPE = STATEMENT_OF.HDFCBANK;
			} else if (CC_TYPE.getType() != -1){
				processStatements(data);
			}
		}
		
		if(setOfData.size() == 0 && count > 50)  {
			resume = false;
		}
		return resume;
	}
	
	
	private void processStatements(String data){
		if(CC_TYPE.getType() == CC_TYPE.CITIBANK.getType()){
			processCitiBankStatements(data);
		}else if(CC_TYPE.getType() == CC_TYPE.HDFCBANK.getType()){
			processHdfcBankStatements(data);
		}
	}


	private void processHdfcBankStatements(String data) {
		//FanaticLogger.log("CSVAppender.processHdfcBankStatements", "Started adding meaningful records !!");
		// TODO
	}

	List<String> list = new ArrayList<>();
    boolean emailIDFound = false;
    boolean amountInRupeesFound = false;
    boolean redemTurboPoints = false;
    static int columncount = 0;
    static int MAX_COUNT = 4;
    String userID = null;
    StringBuilder sb = new StringBuilder();
    
    // TODO - Need to move the following API to out of the class and make separate Rule engine for Citibank, as this kind of method will grow here. 
	private void processCitiBankStatements(String data) {
		//FanaticLogger.log("CSVAppender.processCitiBankStatements", "Started adding meaningful records !!");
		if(emailIDFound){
			userID = data.trim();
			emailIDFound = false;
			return;
		}
		
		if(data.equals("Email id: ")){
			emailIDFound = true;
		}
		
		if(data.equals("Amount (in Rs)")){
			amountInRupeesFound = true;
			return;
		}
		
		if(data.equals("Redeem your Turbo Points - Get Rewarded for your Card usage!")){
			redemTurboPoints = true;
		}
		
		if(amountInRupeesFound && !redemTurboPoints){
			
			data = data.trim();
			
			if(columncount < MAX_COUNT){
				
				if (data.isEmpty() || data.equals("Date") || data.equals("Reference no")
						|| data.equals("Transaction Details")
						|| data.equals("Amount (in Rs)")){
					return;
				}
				
				if(data.equals("PAYMENT RECD, THANK YOU")){
					data = "PAYMENT RECD - THANK YOU";
				}
			
				if(columncount == 1){
					try {
						Double.parseDouble(data);
					} catch (NumberFormatException exception) {
						//add dummy if reference no not found.
						sb.append("1111111");
					}
				}
				
				sb.append(data);
				
				if(columncount != MAX_COUNT){
					sb.append(",");
				}
				
				columncount++;
			}else{
				columncount = 0;
				sb.insert(0, userID+",");
				list.add(sb.toString());
				sb = null;
				sb = new StringBuilder();
				processCitiBankStatements(data);
			}
		}
	}

	@Override
	public List<String> getResult() {
		return list;
	}

}
