package com.monkey.android;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.android.chimpchat.adb.AdbBackend;
import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.chimpchat.core.TouchPressType;
import com.monkey.android.utils.Chart;
import com.monkey.android.utils.Enviroment;
import com.monkey.android.utils.GetPerfermanceData;
import com.monkey.android.utils.Log;

public class Monkey{
	private AdbChimpDevice device;
	private AdbBackend adb;
	private String appPackage;
	private String deviceID;
	private Collection<String> categories;
	private FileWriter writer = null;
	private static TimerTask task;
	private ArrayList<String> data;
	private String catogory;
	private String appName;
	private double frequency;
	private int resolutionX, resolutionY;
	
	//TODO �����쳣����
	public static void main(String[] args) {
		Monkey monkey = new Monkey();
		try {
			String durationStr = Enviroment.getConf("duration");
			double duration = Double.parseDouble(durationStr);		
			monkey.init();
			monkey.setUp();
			monkey.excute(duration);
		} catch (Exception e1) {
			Log.print(e1, Log.ERROR);
		} finally {
			if (task != null) {
				task.cancel();
			}
			try {
				monkey.tearDown();
			} catch (Exception e2) {
				Log.print(e2, Log.ERROR);
			}finally{
				System.exit(0);
			}
			System.exit(0);
		}
	}

	private void init() throws Exception {
		writer = new FileWriter("results"+File.separator + "Data.csv");
		data = new ArrayList<String>();
		
		// ��ȡ���ò���
		appPackage = Enviroment.getConf("packageName");
		deviceID = Enviroment.getConf("deviceID");
		categories = new ArrayList<String>();
		catogory = Enviroment.getConf("catogory");
		categories.add(catogory);
		appName = Enviroment.getConf("appName");
		frequency = Double.parseDouble(Enviroment.getConf("frequency"));
		resolutionX = Integer.parseInt(Enviroment.getConf("resolution").split(",")[0]);
		resolutionY = Integer.parseInt(Enviroment.getConf("resolution").split(",")[1]);
		
		// �����豸
		Log.print("----------��ʼ�����豸-----------", Log.DEBUG);
		adb = new AdbBackend();
		if (deviceID == null) {
			System.out.println("deviceID is null");
			return;
		}
		device = (AdbChimpDevice) adb.waitForConnection(5000, deviceID);
		if (device != null) {
			Log.print("----------���ӵ��豸----------", Log.DEBUG);
		} else {
			Log.print("----------û���ҵ��豸----------", Log.DEBUG);
			return;
		}
		Log.print("----------Back to home-----------", Log.DEBUG);
		device.press("KEYCODE_HOME", TouchPressType.DOWN_AND_UP);
		
		// ��װAPP
		Log.print("----------Installing app-----------", Log.DEBUG);
		device.removePackage(appPackage);
		device.installPackage(appName);
		Thread.sleep(5000);
		
	}

	private void setUp() throws Exception {
		// ����app
		Log.print("----------����Ӧ��----------", Log.DEBUG);
		String action = "android.intent.action.MAIN";
		Collection<String> categories = new ArrayList<String>();
		categories.add("android.intent.category.LAUNCHER");
		device.startActivity(null, action, null, null, categories, new HashMap<String, Object>(), "ctrip.android.view/.home.CtripBootActivity", 0);
		Thread.sleep(8000);
	}

	private void excute(double duration) throws Exception {
		Log.print("----------�����ڴ�����ץȡ----------", Log.DEBUG);
		Timer timer = new Timer();
		task = new Task();
		timer.schedule(task, 2000, Integer.parseInt(Enviroment.getConf("dataFrequency")) * 1000);

		Log.print("----------��ʼMonkey����---------", Log.DEBUG);
		Random rand = new Random();
		double touchTime = duration * 3600000 / frequency;
		Log.print("----------�ܹ����������" + (int) touchTime / 2, Log.DEBUG);
		int i = 0;
		while (i < (int) touchTime / 2) {
			// ����������
			int x1 = rand.nextInt(resolutionX);
			int y1 = rand.nextInt(resolutionY) + 50;
			int x2 = rand.nextInt(resolutionX);
			int y2 = rand.nextInt(resolutionY) + 50;
			device.touch(x1, y1, TouchPressType.DOWN_AND_UP);
			Log.print("���꣺(" + x1 + "," + y1 + ")", Log.DEBUG);

			// �������
			device.shell("input swipe " + x1 + " " + y1 + " " + " " + x2 + " " + y2);
			Thread.sleep(Integer.parseInt(Enviroment.getConf("frequency")));
			i++;
		}
	}

	private void tearDown() throws Exception {
		Log.print("----------����ͼ��----------", Log.DEBUG);
		if (Enviroment.getConf("cpu").equalsIgnoreCase("true")) {
			Chart.writeChart("CPU", data);
		}
		if (Enviroment.getConf("memory").equalsIgnoreCase("true")) {
			Chart.writeChart("Memory", data);
		}
		writer.close();
		adb.shutdown();
		Log.print("----------Moneky ���Խ���----------", Log.DEBUG);
	}

	private class Task extends TimerTask {
		@Override
		public void run() {
			try {
				ArrayList<String> meninfo = GetPerfermanceData.getMeminfo();
				ArrayList<String> cpuinfo = GetPerfermanceData.getCpuinfo();
				writer.write(meninfo.get(0) + "," + meninfo.get(1) + "," + cpuinfo.get(0) + "%" + "\r");
				writer.flush();
				data.add(meninfo.get(0));
				data.add(meninfo.get(1));
				data.add(cpuinfo.get(0));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
