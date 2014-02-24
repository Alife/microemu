package org.microemu.android;

import java.io.File;
import java.util.List;
import java.util.Set;

import com.opera.mini.mod422.R;

import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.MySurfaceView;
import android.widget.EditText;
import android.widget.TextView;

/**
 *@author Himi
 *@���뷨����ʶ��
 *@ע��: android.gesture�������api-4(SDK1.6)�ſ�ʼ֧�ֵģ�
 *@���ѣ�Ĭ�ϴ浽SD���У����Ա�������AndroidMainfest.xml����SD����дȨ�ޣ�
 */

public class GestureActivity extends Activity {
	private GestureOverlayView gov;// ����һ����д��ͼ��
	private Gesture gesture;// ��дʵ��
	private GestureLibrary gestureLib;//����һ�����Ʋֿ�
	private TextView tv;
	private EditText et;
	private String path;//�����ļ�·��
	private File file;//
	Set<String> set = null;//ȡ����������
	Object ob[] = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
		//		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gesture);
		tv = (TextView) findViewById(R.id.himi_tv);
		et = (EditText) findViewById(R.id.himi_edit);
		gov = (GestureOverlayView) findViewById(R.id.himi_gesture);
		gov.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);//���ñʻ����� 
		// GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE ����֧�ֶ�ʻ�
		// GestureOverlayView.GESTURE_STROKE_TYPE_SINGLE ��֧�ֵ�һ�ʻ�
		path = new File(Environment.getExternalStorageDirectory(), "gestures").getAbsolutePath();
		//�õ�Ĭ��·�����ļ���/sdcard/gestures
		file = new File(path);//ʵ��gestures���ļ�����
		gestureLib = GestureLibraries.fromFile(path);//ʵ�����Ʋֿ�
		set = gestureLib.getGestureEntries();//ȡ����������
		ob = set.toArray();
		et.setText(String.valueOf((char)((int)'a'+ob.length)));
		gov.addOnGestureListener(new OnGestureListener() { // �����ǰ���д��ͼ��
					@Override
					// ���·�������տ�ʼ�����Ƶ�ʱ�򴥷�
					public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
						tv.setText("�����ڽ��յ�ʱ������һ�ʻ������һ�����ƣ�����~");
					} 
					@Override
					// ���·����ǵ����������γɵ�ʱ�򴥷�
					public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
						gesture = overlay.getGesture();// �ӻ�ͼ��ȡ���γɵ�����
						if (gesture.getStrokesCount() == 1) {//���ж����û��������ʻ�
							//(ǿ�������һ��ʼ�������Ʊʻ������ǵ�һ�ʻ�����������ʼ�յõ���ֻ��1��)
							if (event.getAction() == MotionEvent.ACTION_UP) {//�ж������ʻ��뿪��Ļ
								//if(gesture.getLength()==100){}//�������ж����ȴﵽ100����
								if (et.getText().toString().equals("")) {
									tv.setText("������û�������������ƣ�so~����ʧ����~");
									et.setText(String.valueOf((char)((int)'a'+ob.length)));
								}
								tv.setText("���ڱ�������...");
								addMyGesture(et.getText().toString(), gesture);//���Լ�д��������ƺ��� 
							}
						} else {
							tv.setText("�����ڽ��յ�ʱ���������ʻ������һ�����ƣ�����~");
						}
					} 
					@Override
					public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
					} 
					@Override
					public void onGesture(GestureOverlayView overlay, MotionEvent event) {
					}
				});
		//----�������ڳ���������ʱ����б�����������!------
		if (!gestureLib.load()) {
			tv.setText("Himi��ʾ�����Ƴ���9��������ɾ���������ƵĲ�����Ϊ�˽�������һЩ��"
					+ " ���뷨������ϰ~(*^__^*)~ ������\n�������ܣ�(�����������ñ��뻭���ʻ�����Ŷ~)\n1." +
							"������ƣ���EditText���������ƣ�Ȼ������Ļ�ϻ������ƣ�\n2.ƥ�����ƣ�" 
					+ "��EditText����\"himi\",Ȼ���������Ƽ��ɣ� ");
		} else {
			loadAllGesture(set, ob);
		}
	}

	public void addMyGesture(String name, Gesture gesture) { 
		try {
			String existName = findGesture(gesture);
			if (existName==null) {
				// �������ַ�ʽ����ģ������SDcard�ڡ�Android2D��Ϸ����֮ʮ�������
				if (Environment.getExternalStorageState() != null) {// �����������̽�ն��Ƿ���sdcard!
					if (!file.exists()) {// �ж��Ƿ��Ѿ����������ļ�
						// �������ļ���ʱ������ȥֱ�Ӱ����ǵ������ļ�����
						gestureLib.addGesture(name, gesture);
						if (gestureLib.save()) {////���浽�ļ���
							gov.clear(true);//����ʻ�
							// ע�Ᵽ���·��Ĭ����/sdcard/gesture ,so~������AndroidMainfest.xml���϶�дȨ�ޣ�
							// ���ﱧԹһ�£��ȿȡ���ʵ�����Ӧ�ó���ƪ���ĵģ�������Ϊ���������쳣,������ϸ����
							// �ŷ��ֲ���ûдȨ��,��������Ȼ��AndroidMainfest.xml��д��Ȩ�ޣ�����д����λ��..������
							tv.setText("�������Ƴɹ�����Ϊ�����������ļ���" + "���Ե�һ�α������Ƴɹ���Ĭ���ȴ�" +
									"����һ�������ļ���Ȼ�����Ʊ��浽�ļ���.");
							et.setText(String.valueOf((char)((int)'a'+ob.length+1)));
							MySurfaceView.vec.put(name,gesture.toBitmap(100, 100, 12, Color.GREEN));
						} else {
							tv.setText("��������ʧ�ܣ�");
						}
					} else {//�����ڴ��ļ���ʱ��������Ҫ��ɾ��������Ȼ����µ����Ʒ���
						//��ȡ�Ѿ����ڵ��ļ�,�õ��ļ��е���������
						if (!gestureLib.load()) {//�����ȡʧ��
							tv.setText("�����ļ���ȡʧ�ܣ�");
						} else {//��ȡ�ɹ� 
							set = gestureLib.getGestureEntries();//ȡ����������
							ob = set.toArray();
							if (MySurfaceView.vec.containsKey(name)) {
								//����˱���Ϊtrue˵������ͬname������
								gestureLib.removeGesture(name, gesture);//ɾ���뵱ǰ������ͬ������
								//gestureLib.removeEntry(name);
								gestureLib.addGesture(name, gesture);
								MySurfaceView.vec.remove(name);
							}
							if (gestureLib.save()) {
								gov.clear(true);//����ʻ� 
								//�����ǰ�����ÿ�����Ƶ�����Ҳ��������
								MySurfaceView.vec.put(name,gesture.toBitmap(100, 100, 12, Color.GREEN));
								tv.setText("�������Ƴɹ�����ǰ��������һ���У�" + ob.length + "��");
								et.setText(String.valueOf((char)((int)'a'+ob.length+1)));
							} else {
								tv.setText("��������ʧ�ܣ�");
							}
							////------- --���´����ǵ����Ƴ���9����ȫ����� ����--------
							if (ob.length > 9) {
								for (int i = 0; i < ob.length; i++) {//�����Ǳ���ɾ������
									gestureLib.removeEntry((String) ob[i]);
								}
								gestureLib.save();
								MySurfaceView.vec.clear();//ɾ����������ͼ������
								tv.setText("���Ƴ���9������ȫ�����!");
								et.setText("");
							}
							ob = null;
							set = null;
						}
					}
				} else {
					tv.setText("��ǰģ����û��SD�� - -��");
				}
			}
		} catch (Exception e) {
			tv.setText("�����쳣��");
		}
	}

	public void loadAllGesture(Set<String> set, Object ob[]) { //�������е����� 
		if (gestureLib.load()) {//��ȡ���µ������ļ�
			set = gestureLib.getGestureEntries();//ȡ����������
			ob = set.toArray();
			for (int i = 0; i < ob.length; i++) {
//				//������ת��Bitmap
//				gestureToImage(gestureLib.getGestures((String) ob[i]).get(0));
//				//�����ǰ�����ÿ�����Ƶ�����Ҳ��������
//				MySurfaceView.vec_string.addElement((String) ob[i]);
				String name = (String) ob[i];
				Gesture gesture = gestureLib.getGestures((String) ob[i]).get(0);
				MySurfaceView.vec.put(name,gesture.toBitmap(100, 100, 12, Color.GREEN));
			}
		}
	}

	public String findGesture(Gesture gesture) {
		String name=null;
		try {
			// �������ַ�ʽ����ģ������SDcard�ڡ�Android2D��Ϸ����֮ʮ�������
			if (Environment.getExternalStorageState() != null) {// �����������̽�ն��Ƿ���sdcard!
				if (!file.exists()) {// �ж��Ƿ��Ѿ����������ļ�
					tv.setText("ƥ������ʧ�ܣ���Ϊ�����ļ������ڣ���");

				} else {//�����ڴ��ļ���ʱ��������Ҫ��ɾ��������Ȼ����µ����Ʒ���
					//��ȡ�Ѿ����ڵ��ļ�,�õ��ļ��е���������
					if (!gestureLib.load()) {//�����ȡʧ��
						tv.setText("ƥ������ʧ�ܣ������ļ���ȡʧ�ܣ�");
					} else {//��ȡ�ɹ� 
						List<Prediction> predictions = gestureLib.recognize(gesture);
						//recognize()�ķ��ؽ����һ��prediction���ϣ�
						//������������gesture��ƥ��Ľ����
						//�����ƿ��в�ѯƥ������ݣ�ƥ��Ľ�����ܰ���������ƵĽ���� 
						if (!predictions.isEmpty()) {
							Prediction prediction = predictions.get(0);
							//prediction��score���Դ����������Ƶ����Ƴ̶�
							//prediction��name�������ƶ�Ӧ������ 
							//prediction��score���Դ�������gesture�����Ƴ̶ȣ�ͨ������²�����scoreС��1�Ľ������ 
							if (prediction.score >= 1) {
								tv.setText("��ǰ������������ƿ����ҵ������Ƶ����ƣ�name =" + prediction.name);
								name=prediction.name;
							}
						}
					}
				}
			} else {
				tv.setText("ƥ������ʧ�ܣ�,��ǰģ����û��SD�� - -��");
			}
		} catch (Exception e) {
			e.printStackTrace();
			tv.setText("���ڳ����쳣��ƥ������ʧ����~");
		}
		return name;
	}
}


