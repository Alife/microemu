/**
 *  MicroEmulator
 *  Copyright (C) 2008 Bartek Teodorczyk <barteo@barteo.net>
 *
 *  It is licensed under the following two licenses as alternatives:
 *    1. GNU Lesser General Public License (the "LGPL") version 2.1 or any newer version
 *    2. Apache License (the "AL") Version 2.0
 *
 *  You may not use this file except in compliance with at least one of
 *  the above two licenses.
 *
 *  You may obtain a copy of the LGPL at
 *      http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *
 *  You may obtain a copy of the AL at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the LGPL or the AL for the specific language governing permissions and
 *  limitations.
 *
 *  @version $Id: AndroidCanvasUI.java 2528 2012-03-19 16:06:11Z barteo@gmail.com $
 */

package org.microemu.android.device.ui;

import java.util.Date;

import javax.microedition.lcdui.Canvas;

import org.microemu.DisplayAccess;
import org.microemu.MIDletAccess;
import org.microemu.MIDletBridge;
import org.microemu.android.MicroEmulatorActivity;
import org.microemu.android.device.AndroidDeviceDisplay;
import org.microemu.android.device.AndroidDisplayGraphics;
import org.microemu.android.device.AndroidInputMethod;
import org.microemu.android.util.AndroidRepaintListener;
import org.microemu.android.util.Overlay;
import org.microemu.app.ui.DisplayRepaintListener;
import org.microemu.device.Device;
import org.microemu.device.DeviceFactory;
import org.microemu.device.ui.CanvasUI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Region;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class AndroidCanvasUI extends AndroidDisplayableUI implements CanvasUI {   
    
    private static AndroidDisplayGraphics gameCanvasGraphics = null;   
    
    private static Bitmap gameCanvasBitmap = null;
	//View viewCanvas;
    
    public AndroidCanvasUI(final MicroEmulatorActivity activity, Canvas canvas) {
        super(activity, canvas, false);
       
        activity.post(new Runnable() {
            public void run() {
                view = new CanvasView(activity, AndroidCanvasUI.this);
                view.setId(view.hashCode());
				//viewCanvas.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
				//LinearLayout layout = new LinearLayout(activity);
				//view=layout;
				//((LinearLayout)view).addView(viewCanvas);
				//view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            }
        });
    }
        
    public View getView() {
        return view;
    }
    
    @Override
    public void hideNotify()
    {
        ((AndroidDeviceDisplay) activity.getEmulatorContext().getDeviceDisplay()).removeDisplayRepaintListener((DisplayRepaintListener) view);
        
        super.hideNotify();
    }

    @Override
    public void showNotify()
    {
        super.showNotify();
        
        activity.post(new Runnable() {
            public void run() {
		        ((AndroidDeviceDisplay) activity.getEmulatorContext().getDeviceDisplay()).addDisplayRepaintListener((DisplayRepaintListener) view);
		        ((Canvas) displayable).repaint();
            }
        });
    }
    
	public AndroidDisplayGraphics getGraphics() {
		gameCanvasGraphics = new AndroidDisplayGraphics();
        if (gameCanvasBitmap != null) {
        	gameCanvasGraphics.reset(new android.graphics.Canvas(gameCanvasBitmap));
        }
		
		return gameCanvasGraphics;
	}

    //
    // CanvasUI
    //
    
    public static class CanvasView extends View implements DisplayRepaintListener  {
        
        private final static int FIRST_DRAG_SENSITIVITY_X = 5;
        
        private final static int FIRST_DRAG_SENSITIVITY_Y = 5;
        
        private AndroidCanvasUI ui;
        
        private int pressedX = -FIRST_DRAG_SENSITIVITY_X;
        
        private int pressedY = -FIRST_DRAG_SENSITIVITY_Y;
        
        private Overlay overlay = null;
        
        private static Matrix scale = new Matrix();

        private AndroidKeyListener keyListener = null;
        
        private int inputType = InputType.TYPE_CLASS_TEXT;
   
        private static final AndroidDisplayGraphics graphics = new AndroidDisplayGraphics();
		private static MIDletAccess ma;
		static DisplayAccess da;
		static AndroidDeviceDisplay deviceDisplay;
		
        public CanvasView(Context context, AndroidCanvasUI ui) {
            super(context);
            this.ui = ui;            
            setFocusable(true);
            setFocusableInTouchMode(true);
			//setPadding(0,30,0,0);
            /*setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                    Log.d("microemuinput", "key="+String.valueOf(keyCode));
                return false;
            }
        });*/
            if (ma == null) {ma = MIDletBridge.getMIDletAccess();}
            if (da==null)da=ma.getDisplayAccess();
            if (deviceDisplay==null)deviceDisplay = (AndroidDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay();
        }
   
        public AndroidCanvasUI getUI() {
            return ui;
        }             

        public void flushGraphics(int x, int y, int width, int height) {
            // TODO handle x, y, width and height
            if (repaintListener == null) {
                postInvalidate();
            } else {
                repaintListener.flushGraphics();
            }
        }

        public void setOverlay(Overlay overlay) {
            this.overlay = overlay;
        }
        
        public void setScale(float sx, float sy) {
            scale.reset();
            scale.postScale(sx, sy);
        }

        public void setKeyListener(AndroidKeyListener keyListener, int inputType) {
        	this.keyListener = keyListener;
        	this.inputType = inputType;
        }

        /*@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        	//outAttrs.imeOptions |= EditorInfo.IME_ACTION_NEXT;
        	//outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        	outAttrs.inputType = InputType.TYPE_NULL;
        	
        	return new BaseInputConnection(this, false) {

				@Override
				public boolean performEditorAction(int actionCode) {
					if (actionCode == EditorInfo.IME_ACTION_DONE) {
						InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		                imm.hideSoftInputFromWindow(CanvasView.this.getWindowToken(), 0);		                
		                return true;
					} else {
						return super.performEditorAction(actionCode);
					}
				}

				@Override
				public boolean commitText(CharSequence text, int newCursorPosition) {
					if (keyListener != null) {
						keyListener.insert(keyListener.getCaretPosition(), text);
					}
					
					return true;
				}

				@Override
				public boolean deleteSurroundingText(int leftLength, int rightLength) {
					if (keyListener != null) {
						int caret = keyListener.getCaretPosition();
						keyListener.delete(caret - leftLength, caret + rightLength);
					}
					
					return true;
				}

				@Override
				public boolean sendKeyEvent(KeyEvent event) {
					return super.sendKeyEvent(event);
				}
        		
        	};
	}*/        
        
        //
        // View
        //
		@Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
			outAttrs.imeOptions |=
				EditorInfo.IME_FLAG_NO_EXTRACT_UI |
				EditorInfo.IME_FLAG_NO_ENTER_ACTION |
				EditorInfo.IME_ACTION_NONE;
			outAttrs.inputType = EditorInfo.TYPE_NULL;
			return new BaseInputConnection(this, false) {
				@Override
				public boolean deleteSurroundingText (int leftLength, int rightLength) {
					if (rightLength == 0 && leftLength == 0) {
						return this.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
					}
					for (int i = 0; i < leftLength; i++) {
						this.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
					}
					// TODO: forward delete
					return true;
				}
			};
		}
		
		Date startData = new Date();

		@Override
		public void onDraw(android.graphics.Canvas androidCanvas) {
			super.onDraw(androidCanvas);
			_draw(androidCanvas,"onDraw");
		}  

		@Override
		public void draw(android.graphics.Canvas androidCanvas) {
			super.draw(androidCanvas);
			_draw(androidCanvas, "draw");
		}
//		@Override
//		public void dispatchDraw(android.graphics.Canvas androidCanvas) {
//			super.dispatchDraw(androidCanvas);
//			_draw(androidCanvas, "dispatchDraw");
//		}
        public void _draw(android.graphics.Canvas androidCanvas,String methord) {
            startData = new Date();
            //if (ma == null) {ma = MIDletBridge.getMIDletAccess();}
//        	Matrix originalMatrix = androidCanvas.getMatrix();
            graphics.reset(androidCanvas);
            if (gameCanvasGraphics != null) {androidCanvas.drawBitmap(gameCanvasBitmap, scale, null);}
            androidCanvas.setMatrix(scale);
//			AndroidDeviceDisplay deviceDisplay = (AndroidDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay();
            androidCanvas.clipRect(0, 0, deviceDisplay.getFullWidth(), deviceDisplay.getFullHeight(), Region.Op.REPLACE);
            //Log.i(VIEW_LOG_TAG, msg+" paint 1");
//            ma.getDisplayAccess().paint(graphics);
            da.paint(graphics);
            if (overlay != null) {
            	androidCanvas.setMatrix(androidCanvas.getMatrix());
            	androidCanvas.clipRect(0, 0, getWidth(), getHeight(), Region.Op.REPLACE);
                overlay.onDraw(androidCanvas);
            }
            //Log.i(VIEW_LOG_TAG, (new Date().getTime() - startData.getTime()) + " "+methord);
        }   
        
		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			
			AndroidDeviceDisplay deviceDisplay = (AndroidDeviceDisplay) DeviceFactory.getDevice().getDeviceDisplay();
			deviceDisplay.setSize(w, h);

        	if (gameCanvasBitmap == null || gameCanvasBitmap.getWidth() != w || gameCanvasBitmap.getHeight() != h) {
	        	if (gameCanvasBitmap != null) {
	        		gameCanvasBitmap.recycle();
	        	}
	        	gameCanvasBitmap = Bitmap.createBitmap(deviceDisplay.getFullWidth(), deviceDisplay.getFullHeight(), Bitmap.Config.RGB_565);
	        }
	        if (gameCanvasGraphics != null) {
	        	gameCanvasGraphics.reset(new android.graphics.Canvas(gameCanvasBitmap));
	        }
			
			MIDletAccess ma = MIDletBridge.getMIDletAccess();
			if (ma == null) {
				return;
			}
			DisplayAccess da = ma.getDisplayAccess();
			if (da != null) {
				da.sizeChanged();
			}
		}

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (overlay != null && overlay.onTouchEvent(event)) {
                return true;
            }
            Device device = DeviceFactory.getDevice();
            AndroidInputMethod inputMethod = (AndroidInputMethod) device.getInputMethod();

            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                inputMethod.pointerPressed(x, y);
                pressedX = x;
                pressedY = y;
                break;
            case MotionEvent.ACTION_UP :
                inputMethod.pointerReleased(x, y);
                break;
            case MotionEvent.ACTION_MOVE :
                if (x > (pressedX - FIRST_DRAG_SENSITIVITY_X) &&  x < (pressedX + FIRST_DRAG_SENSITIVITY_X)
                        && y > (pressedY - FIRST_DRAG_SENSITIVITY_Y) &&  y < (pressedY + FIRST_DRAG_SENSITIVITY_Y)) {
                } else {
                    pressedX = -FIRST_DRAG_SENSITIVITY_X;
                    pressedY = -FIRST_DRAG_SENSITIVITY_Y;
                    inputMethod.pointerDragged(x, y);
                }
                break;
            default:
                return false;
            }
            
            return true;
        }
        @Override
        //
        // DisplayRepaintListener
        //
      
        public void repaintInvoked(Object repaintObject)
        {
            Rect r = (Rect) repaintObject;
            flushGraphics(r.left, r.top, r.width(), r.height());
        }       
        
        private AndroidRepaintListener repaintListener;

        public void setAndroidRepaintListener(AndroidRepaintListener repaintListener)
        {
            this.repaintListener = repaintListener;
        }
    }
}
