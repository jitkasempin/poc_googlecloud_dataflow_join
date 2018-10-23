package com.jitkasem;

//import com.google.cloud.dataflow.sdk.transforms.DoFn;
//import com.google.cloud.dataflow.sdk.values.KV;

import org.apache.beam.sdk.transforms.DoFn;


public class StringTransform extends DoFn<String[],String[]> {

	@ProcessElement
	public void processElement(ProcessContext processContext) throws Exception {
    	    	
    	String[] strArr = processContext.element();
    	
    	String[] resStr = new String[strArr.length];
    	
    	for (int i = 0; i < strArr.length; i++) {
    		String temp = strArr[i];
    		temp = temp.replace("\"","");    		
    		resStr[i] = temp;
    	}
        
    	processContext.output(resStr);
    }
}
