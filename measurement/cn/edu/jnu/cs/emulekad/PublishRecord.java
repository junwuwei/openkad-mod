/**
 * 
 */
package cn.edu.jnu.cs.emulekad;

import il.technion.ewolf.kbr.Key;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Zhike Chan (zk.chan007@gmail.com)
 */
public class PublishRecord {
	public final Key targetKey;
	public final String vanishString;
	public final long publishTime;
	public final int nrRecipient;
	public long vanishTime=0;



	public PublishRecord(Key targetKey, String vanishString, long publishTime,
			int nrRecipient) {
		this.targetKey = targetKey;
		this.vanishString = vanishString;
		this.publishTime = publishTime;
		this.nrRecipient = nrRecipient;
	}

	public double getTimespan(TimeUnit timeUnit) {
		return 1.0 * (vanishTime - publishTime) / timeUnit.toMillis(1);
	}

	public String toString() {
//		return String.format(Locale.CHINA, "vanishString=[%1$s] " +
				return String.format(Locale.CHINA,
				"publishTime=[%2$tm-%2$td %2$tH:%2$tM:%2$tS] nrRecipient=[%3$2d] " +
				"vanishTime=[%4$tm-%4$td %4$tH:%4$tM:%4$tS] timespan=[%5$.3fh]",
				vanishString,new Date(publishTime),nrRecipient,new Date(vanishTime),
				getTimespan(TimeUnit.HOURS));

	}
	
	public static void main(String[] args) {
		PublishRecord record=new PublishRecord(null,"test",System.currentTimeMillis(),10);
		record.vanishTime=System.currentTimeMillis()+1000000;
		System.out.println(record);
	}
	
	public boolean isVanished(){
		return vanishTime!=0;
	}

}
