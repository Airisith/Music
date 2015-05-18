package com.airisith.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BluToothConnect {

	private static final String TAG = "BluToothConnect";

	private static File file;
	private String fileString;
	private byte[] bytes;
	private final static BluetoothAdapter bluetoothAdapter = BluetoothAdapter
			.getDefaultAdapter();
	private static BluetoothDevice BLdevice;
	private static final String NAME = "BluetoothDelivery";
	private static final UUID MY_UUID = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private static Context context;

	@SuppressWarnings("static-access")
	public BluToothConnect(Context context, String filePath) {
		this.context = context;
		file = new File(filePath);
		try {
			FileInputStream fis = new FileInputStream(file);
			bytes = new byte[fis.available()];
			fis.read(bytes);
			fileString = bytes2Hex(bytes);
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 连接蓝牙
	 * 
	 * @return
	 */
	public boolean sendFile() {
		BLdevice = checkBluetooth();
		if (null != BLdevice) {
			Log.w(TAG, "已配对设备：" + BLdevice.getName());
			Log.w(TAG, "开始传送文件");
			new Thread(new serverThread()).start();
			return true;
		} else {
			Log.w(TAG, "无连接设备");
			 Toast.makeText(context, "无连接设备", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	/**
	 * 启动文件传输
	 * @author Administrator
	 *
	 */
	class serverThread implements Runnable {

		private final BluetoothServerSocket mmServerSocket;

		public serverThread() {
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client
				// code
				tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME,
						MY_UUID);
			} catch (IOException e) {
			}
			mmServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					socket = mmServerSocket.accept();
					OutputStream os = socket.getOutputStream();
					os.write(fileString.getBytes());

				} catch (IOException e) {
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					try {
						mmServerSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 获取已经配对的设备
	 * @return
	 */
	public static BluetoothDevice checkBluetooth() {
		BluetoothDevice lastBluetoothDevice = null;
		// 创建bluetoothAdapter
		if (null != bluetoothAdapter) {
			if (true == bluetoothAdapter.isEnabled()) {
				// 或者bluetoothAdapter.enable();
				Intent intent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				context.startActivity(intent);
			} else {
				return null;
			}
		}
		Set<BluetoothDevice> bluetoothDevice = bluetoothAdapter
				.getBondedDevices();
		if (bluetoothDevice.size() > 0) {
			Iterator<BluetoothDevice> iterator = bluetoothDevice.iterator();
			for (; iterator.hasNext();) {
				BluetoothDevice device = iterator.next();
				lastBluetoothDevice = device;
			}
		} else {
			return null;
		}
		return lastBluetoothDevice;
	}

	/**
	 * 将byte转为hex
	 * @param src
	 * @return
	 */
	 public static String bytes2Hex(byte[] src){         
	        char[] res = new char[src.length*2];    
	        final char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};    
	        for(int i=0,j=0; i<src.length; i++){    
	            res[j++] = hexDigits[src[i] >>>4 & 0x0f]; // 无符号右移,高位补0”； 与>>类似   
	            res[j++] = hexDigits[src[i] & 0x0f];    
	        }    
	            
	        return new String(res);    
	    }
}
