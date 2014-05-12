package com.monkey.android.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class Chart {
	private static Date date = null;
	private static SimpleDateFormat format = null;

	public static void writeChart(String chartType, ArrayList<String> data)
			throws Exception {
		// ��������ͼ
		date = new Date();
		format = new SimpleDateFormat("yyyy-MM-dd");
		String excuteTime = format.format(date);
		String str = null;
		if (chartType.equalsIgnoreCase("Memory")) {
			str = chartType + "(kb)";
		} else if (chartType.equalsIgnoreCase("CPU")) {
			str = chartType + "(%)";
		}
		JFreeChart chart = ChartFactory.createLineChart(chartType + "ռ��ͼ",
				"ִ��ʱ��:" + excuteTime, str, getDataset(chartType, data), PlotOrientation.VERTICAL, true, false,
				false);

		// �������ͼ
		FileOutputStream picture = null;
		picture = new FileOutputStream("results"+File.separator+chartType + ".jpg");
		ChartUtilities.writeChartAsJPEG(picture, 1.0f, chart, 800, 600, null);
		picture.close();
	}
	
	//������
	private static CategoryDataset getDataset(String type,
			ArrayList<String> data) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 2; i <= data.size(); i += 3) {
			if (type.equalsIgnoreCase("Memory")) {
				dataset.addValue(Double.parseDouble(data.get(i - 1)), "Memory",
						data.get(i - 2));
			} else if (type.equalsIgnoreCase("CPU")) {
				dataset.addValue(Double.parseDouble(data.get(i)), "CPU",
						data.get(i - 2));
			}
		}
		return dataset;
	}
}
