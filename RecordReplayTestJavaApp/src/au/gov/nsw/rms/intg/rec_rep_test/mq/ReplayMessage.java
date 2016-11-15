package au.gov.nsw.rms.intg.rec_rep_test.mq;

import java.util.Enumeration;
import java.util.Properties;

import com.ibm.broker.config.proxy.BrokerProxy;
import com.ibm.broker.config.proxy.ConfigManagerProxyLoggedException;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.config.proxy.DataCaptureEntry;
import com.ibm.broker.config.proxy.DataCaptureProxy;
import com.ibm.broker.config.proxy.ExecutionGroupProxy;

public class ReplayMessage {

	/**
	 * @param args
	 * @throws ConfigManagerProxyLoggedException 
	 * @throws ConfigManagerProxyPropertyNotInitializedException 
	 */
	public static void main(String[] args) throws ConfigManagerProxyLoggedException, ConfigManagerProxyPropertyNotInitializedException {
		String dataCaptureStore = "RecordReplayDataStore";
		String transactionId = "EBX0USM4BHQIO19TYX8GWFGGFKWZ7V3Y";
		String replayEG = "RecordReplay";
		
		Properties dataCaptureProps = new Properties();
		dataCaptureProps.setProperty(DataCaptureEntry.PROPERTY_LOCAL_TRANSACTION_ID, transactionId);
		DataCaptureEntry entry = new DataCaptureEntry(dataCaptureProps);
		
		BrokerProxy bp = BrokerProxy.getLocalInstance("LOCALIIB");
		ExecutionGroupProxy eg = bp.getExecutionGroupByName(replayEG);
		DataCaptureProxy dcp = eg.getDataCapture(dataCaptureStore,entry);
		System.out.println("XML output: "+dcp.getDataCaptureEntryAsXml(1));
		Enumeration<DataCaptureEntry> entries = dcp.elements();
		while (entries.hasMoreElements()) {
			DataCaptureEntry dataCaptureEntry = (DataCaptureEntry) entries
					.nextElement();
			System.out.println(dataCaptureEntry.getAllProperties());
		}
	}

}
