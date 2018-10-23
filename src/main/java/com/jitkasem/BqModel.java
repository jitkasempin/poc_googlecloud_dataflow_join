package com.jitkasem;

import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

//import com.google.cloud.dataflow.sdk.coders.AvroCoder;
//import com.google.cloud.dataflow.sdk.coders.DefaultCoder;

@DefaultCoder(AvroCoder.class)
public class BqModel {

    public String getTmn_id() {
		return tmn_id;
	}

    public Integer getPromotion_id() {
		return promotion_id;
	}

	public String getReport_ts() {
		return report_ts;
	}

	public String getTrans_id() {
		return trans_id;
	}

	public BqModel(String tmn_id, Integer promotion_id, String report_ts, String trans_id) {
		this.tmn_id = tmn_id;
		this.promotion_id = promotion_id;
		this.report_ts = report_ts;
		this.trans_id = trans_id;
	}

	@Nullable String tmn_id;
	@Nullable Integer promotion_id;
	@Nullable String report_ts;
	@Nullable String trans_id;

	public BqModel() {}
}
