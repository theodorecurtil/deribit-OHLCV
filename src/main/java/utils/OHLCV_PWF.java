package utils;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import com.theodorecurtil.schemas.Data;
import java.util.Iterator;

public class OHLCV_PWF extends ProcessWindowFunction<Data, String, String, TimeWindow> {

	@Override
	public void process(String key, Context context, Iterable<Data> input, Collector<String> out) {

		Data initial_value = input.iterator().next();
		Double high = initial_value.getPrice();
		Double low = initial_value.getPrice();
		Long time_open = initial_value.getTimestamp();
		Long time_close = initial_value.getTimestamp();
		Double open = initial_value.getPrice();
		Double close = initial_value.getPrice();
		Double volume = 0D;

		Iterator<Data> object_iterator = input.iterator();
		while (object_iterator.hasNext()) {
			Data current_record = object_iterator.next();

			if (current_record.getPrice() > high) {
				high = current_record.getPrice();
			}

			if (current_record.getPrice() < low) {
				low = current_record.getPrice();
			}

			if (current_record.getTimestamp() < time_open) {
				time_open = current_record.getTimestamp();
				open = current_record.getPrice();
			}

			if (current_record.getTimestamp() > time_close) {
				time_close = current_record.getTimestamp();
				close = current_record.getPrice();
			}

			volume += current_record.getAmount();

		}
		out.collect(key + " - Window: " + context.window() + " OPEN: " + open + " - HIGH: " + high + " - LOW: " + low + " - CLOSE: " + close + " - VOLUME: " + volume);
	}
}