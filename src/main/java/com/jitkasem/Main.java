package com.jitkasem;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.commons.lang3.ArrayUtils;


public class Main {

    private static Properties config = new Properties();
    private static ClassLoader classLoader = Main.class.getClassLoader();

    private static TableSchema buildSchemaProjection() {
        List<TableFieldSchema> fields = new ArrayList<>();
        fields.add(new TableFieldSchema().setName("tmn_id").setType("STRING"));
        fields.add(new TableFieldSchema().setName("promotion_id").setType("INTEGER"));
        fields.add(new TableFieldSchema().setName("report_ts").setType("STRING"));
        fields.add(new TableFieldSchema().setName("trans_id").setType("STRING"));
        TableSchema schema = new TableSchema().setFields(fields);
        return schema;
      }

    public static void main(String[] args) throws Exception {
        String configFile = "application.properties";
        config.load(classLoader.getResourceAsStream(configFile));

        DataflowPipelineOptions options = PipelineOptionsFactory.as(DataflowPipelineOptions.class);
        options.setProject("testingbigq-1170");
        options.setStagingLocation("gs://poc_dataflow/staging");
        options.setTempLocation("gs://poc_dataflow/temp");
        options.setJobName("testop");

        Pipeline p = Pipeline.create(options);
        
        TableSchema schema = buildSchemaProjection();

        PCollection<String[]> campaignAction = p.apply("ReadLines",TextIO.read().from("gs://poc_dataflow/user_logs.csv"))
        .apply(MapElements.via(new SimpleFunction<String, String[]>() {
            public String[] apply (String element){
                return element.split(";");
            }
        }))
        .apply(Filter.by(new SerializableFunction<String[], Boolean>() {
            public Boolean apply(String[] strings) {
                return strings[9].equals("\"2\"");
            }
        }));
        
        PCollection<String[]> summaryTrans = p.apply("SummaryTransLines",TextIO.read().from("gs://poc_dataflow/summary_acts.csv"))
        			.apply(MapElements.via(new SimpleFunction<String, String[]>() {
        				public String[] apply(String element){
        					return element.split(";");
        				}
        			}));
        

        // develop the side input here

        final PCollectionView<Iterable<String[]>> summaryTransSideInput = summaryTrans.apply(View.<String[]>asIterable());
        
        PCollection<String[]> joinTwoDS = campaignAction.apply("JoinWithTrans", ParDo.of(new DoFn<String[],String[]>() {
        				@ProcessElement
        				public void processElement(ProcessContext c) {
        					
        					String toComp = c.element()[8];
        					
        					Iterable<String[]> si = c.sideInput(summaryTransSideInput);
        					
        					for(String[] strArray : si) {
        						
        						if(strArray[3].equalsIgnoreCase(toComp)){
        							c.output(ArrayUtils.addAll(c.element(), strArray));
        						}
        					}
        				}
        			}
        		).withSideInputs(summaryTransSideInput));
        

        joinTwoDS.apply("ToString",ParDo.of(new StringTransform()))
        		 .apply("ProjectionField",ParDo.of(new ProjectionField()))
        		 .apply("ExtractBQ",ParDo.of(new DoFn<BqModel, TableRow>() {
        				@ProcessElement
        	            public void processElement(ProcessContext c) {
        					BqModel b = c.element();

        					TableRow outRow = new TableRow()
        		  			.set("tmn_id",b.getTmn_id())
        		  			.set("promotion_id", b.getPromotion_id() )
        		  			.set("report_ts", b.getReport_ts() )
        		  			.set("trans_id", b.getTrans_id() );
//        					.set("window_timestamp", c.timestamp().toString());
        					c.output(outRow);		
        				}
        			}))

        		 .apply(BigQueryIO.writeTableRows()
        				 .to("testingbigq-1170:POC_DATA_JOIN.testJoin")
        				 .withSchema(schema)
        				 .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
        				 .withWriteDisposition(WriteDisposition.WRITE_APPEND));
        
        

        PipelineResult result = p.run();
        if (result.getState().equals(PipelineResult.State.FAILED)) {
            System.out.println(result.toString());
        } else {
            System.out.println(result.toString());
        }
    }

}
