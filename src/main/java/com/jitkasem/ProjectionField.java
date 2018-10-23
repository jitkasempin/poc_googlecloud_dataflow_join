package com.jitkasem;

//import com.google.cloud.dataflow.sdk.transforms.DoFn;
//import com.google.cloud.dataflow.sdk.values.KV;
import org.apache.beam.sdk.transforms.DoFn;


public class ProjectionField extends DoFn<String[],BqModel> {

    @ProcessElement
    public void processElement(ProcessContext processContext) throws Exception {
    	
    	String[] row = processContext.element();

    	BqModel gInfo = new BqModel((String)row[1], Integer.parseInt((String)row[4]), (String)row[12], (String)row[13]);
    	
        processContext.output(gInfo);

    }
}
