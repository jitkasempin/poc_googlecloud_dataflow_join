package com.jitkasem;

//import com.google.cloud.dataflow.sdk.transforms.DoFn;
//import com.google.cloud.dataflow.sdk.values.KV;
import org.apache.beam.sdk.transforms.DoFn;


public class SplitTransform extends DoFn<String,String[]> {

    @ProcessElement
    public void processElement(ProcessContext processContext) throws Exception {
        String temp = processContext.element();
        String[] e = temp.split(";");
        processContext.output(e);
    }
}
