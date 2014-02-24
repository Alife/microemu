/**
 *  MicroEmulator
 *  Copyright (C) 2011 Bartek Teodorczyk <barteo@gmail.com>
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
 *  @version $Id: AndroidClassVisitor.java 2472 2011-01-23 13:39:51Z barteo@gmail.com $
 */

package org.microemu.android.util;

import java.lang.reflect.Field;

public class Log {
	
	public static void i(String tag,Object object){
        //ʹ�÷��似����ɶ������Ե����  
        Class<?> c = null;  
        c = object.getClass();  
        Field [] fields = c.getDeclaredFields();  
          
        for(Field f:fields){  
            f.setAccessible(true);  
        }  
        //�� object ����������  
        for(Field f:fields){  
            String field = f.toString().substring(f.toString().lastIndexOf(".")+1);         //ȡ����������  
            try {
				android.util.Log.i(tag, field+": "+f.get(object));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
        }   
	}
	
}
