package org.microemu.android.util;

import java.util.List;

/**
 * 
 * @ClassName: JSONUtil
 * @Description: 常用的系统基础对象扩展方法.
 * @author mobilenpsite-lky
 * @date 2013-03-14
 * 
 */
public class Tools {

	/**
	 * 
	 * IntArray2String(将 int[] 通过 英文逗号拼接成字符串)
	 * 
	 */
	public static String IntArray2String(List<Integer> ints) {
		String result = "";
		for (Integer integer : ints) {
			result += integer + ",";
		}
		if (!Tools.IsNullOrWhiteSpace(result)) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
	/**
	 * 
	 * IntArray2String(将 int[] 通过 英文逗号拼接成字符串)
	 * 
	 */
	public static String IntArray2String(Integer[] ints) {
		String result = "";
		for (Integer integer : ints) {
			result += integer + ",";
		}
		if (!Tools.IsNullOrWhiteSpace(result)) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
	public static String[] Ints2Strings(int[] ints) {
		String[] result = new String[ints.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = String.valueOf(ints[i]);
		}
		return result;
	}
//
//	/**
//	 * 
//	 * String(将 String[] 通过 英文逗号拼接成字符串)
//	 * 
//	 */
//	public static String ToString(String[] ints) {
//		String result = "";
//		for (String integer : ints) {
//			result += integer + ",";
//		}
//		if (!Tools.IsNullOrWhiteSpace(result)) {
//			result = result.substring(0, result.length() - 1);
//		}
//		return result;
//	}
	public static String ToString(int[] ints) {
		String result = "";
		for (int integer : ints) {
			result += integer + ",";
		}
		if (!Tools.IsNullOrWhiteSpace(result)) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
//	public static String ToString(Integer[] ints) {
//		String result = "";
//		for (int integer : ints) {
//			result += integer + ",";
//		}
//		if (!Tools.IsNullOrWhiteSpace(result)) {
//			result = result.substring(0, result.length() - 1);
//		}
//		return result;
//	}
//
//
//	/**
//	 * 截取字符串
//	 */
//	public static String SubString(String obj,int length) {
//		if(!IsNullOrWhiteSpace(obj)&&length>0){
//			if (obj.length()<=length)length= obj.length()-1;
//			obj = obj.substring(0, length);
//		}
//		return obj;
//	}
	/**
	 * 
	 * 判断是否是 null , 空 "" , 是否是多个空格 "    "
	 * 
	 */
	public static Boolean IsNullOrWhiteSpace(String obj) {
		boolean result = obj == null;
		if (!result) {
			result = "".equals(obj.trim());
		}
		return result;
	}

//	/**
//	 * 
//	 * 判断是否是 null ,是否大于零
//	 * 
//	 */
//	public static boolean IsGreaterThanZero(Integer obj) {
//		boolean result = obj == null;
//		if (!result)
//			result = obj <= 0;
//		return !result;
//	}
//
	/**
	 * 
	 * 从数组中查找对象所在的位置<br />
	 * 如果不存在则返回 null
	 * 
	 */
	public static <T> int getIndex(T[] obj, T t) {
		int index = -1;
		for (int i = 0; i < obj.length; i++) {
			if (obj[i].equals(t)) {
				index = i;
				break;
			}
		}
		return index;
	}
	public static <T> int getIndex(int[] obj, T t) {
		int index = -1;
		for (int i = 0; i < obj.length; i++) {
			if (t.equals(obj[i])) {
				index = i;
				break;
			}
		}
		return index;
	}
//
//	/**
//	 * TODO 根据属性获取 List 中指定属性<br />
//	 * 类似于 C# List.Select(d=>d.Name)
//	 */
//	public static <T, T1> List<T1> Select(List<T> list, String name) {
//		// 通过关联表查询
//		List<T1> resultList = new ArrayList<T1>();
//		for (int i = 0; i < list.size(); i++) {
//			// resultList.add();
//		}
//		return resultList;
//	}
//
//	/**
//	 * Url 转码
//	 */
//	public static String UrlEncode(String name) {
//		name = name.replaceAll(" ", "%20");
//		return name;
//	}
//
//	/**
//	 * paras to string
//	 */
//	public static String ToString(List<NameValuePair> paras) {
//		String url = "";
//		for (NameValuePair nameValuePair : paras) {
//			String valueString = nameValuePair.getValue();
//			if (!IsNullOrWhiteSpace(valueString))
//				// ignore value is null and empty
//				// 编码一下,如果 url 出现中文会异常.
//				url += nameValuePair.getName() + "=" + valueString.replaceAll(" ", "%20") + "&";
//		}
//		return url;
//	}
//
//	/**
//	 * string to paras
//	 */
//	public static List<NameValuePair> ToParas(String url) {
//		List<NameValuePair> paras = new ArrayList<NameValuePair>();
//		if (url!=null) {
//			String[] paraStrings = url.split("&");
//			for (String item : paraStrings) {
//				String[] itemSplitStrings = item.split("=");
//				if (itemSplitStrings.length > 1 && !IsNullOrWhiteSpace(itemSplitStrings[1])) {
//					paras.add(new BasicNameValuePair(itemSplitStrings[0], itemSplitStrings[1]));
//				}
//			}
//		}
//		return paras;
//	}
//	public static String getParas(List<NameValuePair> para, String name) {
//		String value = "";
//		if (para!=null) {
//			for (NameValuePair nameValuePair : para) {
//				if(nameValuePair.getName().equals(name)){
//					value = nameValuePair.getValue();
//					break;
//				}
//			}
//		}
//		return value;
//	}
//
//	public static MultipartEntity ToMultipartEntity(List<NameValuePair> paras) throws UnsupportedEncodingException {
//		MultipartEntity multipart = new MultipartEntity();
//		for (NameValuePair nameValuePair : paras) {
//			String valueString = nameValuePair.getValue();
//			if (!IsNullOrWhiteSpace(valueString))
//				multipart.addPart(nameValuePair.getName(), new StringBody(valueString));
//		}
//		return multipart;
//	}
//	/**
//	 * 两个字的中间添加空格使其与三个字的名字对其
//	 */
//	public static String Two2Three(String name) {
//		if(name.length()==2)name = name.charAt(0)+"    "+name.charAt(1);
//		return name;
//	}
	public static String getName(Class<?> class1) {
		//this$0特指该内部类所在的外部类的引用,不需要手动定义,编译时自动加上 
	    String name=class1.getName();
	    String[] nameS=null;
//	    Field this0=null;
//		try {
//			this0 = class1.getDeclaredField("this$0");
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (NoSuchFieldException e) {
//			e.printStackTrace();
//		} 
//		if(this0!=null)name =this0.getName();
		nameS =name.split("\\.");
		name = nameS[nameS.length-1];
		return name;
	}
//	
//	// 拼接图像的绝对地址,当前选择的医院域名 + 当前图片的根地址
//	public static String GetImageUrl(MyApp app, String imageUrl) {
//		return GetImageUrl(app, null, imageUrl);
//	}
//	public static String GetImageUrl(MyApp app,String website, String ImageUrl) {
//		String realImageUrl = ImageUrl;
//		if (!IsNullOrWhiteSpace(realImageUrl)){
//			if(!Tools.isUrl(realImageUrl)){
//				String Main_Website = Config.Main_Website;
//				if(Tools.isUrl(website)){
//					Main_Website = website;
//				}else if(app.hospital!=null&&Tools.isUrl(app.hospital.Website))
//					Main_Website = app.hospital.Website;
//				realImageUrl = Main_Website + realImageUrl;
//			}
//		}
//		else {
//			realImageUrl = "";
//		}
//		return realImageUrl;
//	}
//	/**
//	  * 是否是网址.如 http://www.google.com
//	 */
//	public static boolean isUrl(String website) {
//		boolean v = false;
//		if(!IsNullOrWhiteSpace(website)&&website.toLowerCase().startsWith("http://")){
//			v = true;
//		}
//		return v;
//	}
//	// null 时返回 false
//	public static boolean getValue(Boolean value) {
//		return value==null?false:value;
//	}
//	// null 时返回  0
//	public static int getValue(Integer value) {
//		return value==null?0:value;
//	}
//	
//	public static int[] ToIntArray(String strings) {
//		String[] strs = strings.split(",");
//		int[] ints = new int[strs.length];
//		for (int i = 0; i < strs.length; i++) {
//			ints[i] = Integer.parseInt(strs[i]);
//		}
//		return ints;
//	}
//
//	/**
//     * 解压缩文件到指定的目录.
//     * 
//     * @param unZipfileName 需要解压缩的文件
//     * @param mDestPath 解压缩后存放的路径
//     */
//    public static void unZip(String unZipfileName, String mDestPath) {
//        if (!mDestPath.endsWith("/")) {
//            mDestPath = mDestPath + "/";
//        }
//        FileOutputStream fileOut = null;
//        ZipInputStream zipIn = null;
//        ZipEntry zipEntry = null;
//        File file = null;
//        int readedBytes = 0;
//        byte buf[] = new byte[4096];
//        try {
//            zipIn = new ZipInputStream(new BufferedInputStream(new FileInputStream(unZipfileName)));
//            while ((zipEntry = zipIn.getNextEntry()) != null) {
//                file = new File(mDestPath + zipEntry.getName());
//                if (zipEntry.isDirectory()) {
//                    file.mkdirs();
//                } else {
//                    // 如果指定文件的目录不存在,则创建之.
//                    File parent = file.getParentFile();
//                    if (!parent.exists()) {
//                        parent.mkdirs();
//                    }
//                    fileOut = new FileOutputStream(file);
//                    while ((readedBytes = zipIn.read(buf)) > 0) {
//                        fileOut.write(buf, 0, readedBytes);
//                    }
//                    fileOut.close();
//                }
//                zipIn.closeEntry();
//            }
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//   }
//
//	public static String readFile(String filepath){  
//        //得到文件输入流  
//        File file = new File(filepath);  
//        FileInputStream is = null;  
//        ByteArrayOutputStream outputStream = null;  
//        try {  
//        	 is = new FileInputStream(file);  
//             int size = is.available();
//             byte[] buffer = new byte[size];
//             is.read(buffer);
//             is.close();
//            return new String(buffer);  
//        } catch (FileNotFoundException e) {  
//            e.printStackTrace();  
//        } catch (IOException e) {  
//            e.printStackTrace();  
//        }finally{  
//            if(is!=null){  
//                try {  
//                	is.close();  
//                } catch (IOException e) {  
//                    e.printStackTrace();  
//                }  
//            }  
//            if(outputStream!=null){  
//                try {  
//                    outputStream.flush();  
//                    outputStream.close();  
//                } catch (IOException e) {  
//                    e.printStackTrace();  
//                }  
//            }  
//        }  
//        return null;  
//    }
//	public static <T> T readFile(String filepath,Class<T> class1){
//		T t = null;
//		File file = new File(filepath);
//		if (file.exists()) {
//	        String json = readFile(filepath);
//			if(!IsNullOrWhiteSpace(json)) t = JSON.parseObject(json, class1);
//		}
//		return t;  
//    }
//	/**
//	 * 得到格式化后的html.替换 html 中的图片地址为绝对地址
//	 * 
//	 * @param imgType
//	 * @param html
//	 * @return
//	 */
//	public static String FormatRemoteHtmlWithImg(MyApp app, String html) {
//		if (!Tools.IsNullOrWhiteSpace(html)) {
//			List<String> listSrc = GetImagesList(html.replace("<IMG", "<img"));
//			for (String src : listSrc) {
//				if (!src.startsWith("http"))// 只替换
//					html = html.replace(src, GetImageUrl(app,src));
//			}
//		}
//		return html;
//	}
//
//	/**
//	 * 得到html中的图片地址
//	 * 
//	 * @param html
//	 * @return
//	 */
//	static final Pattern patternImgSrc = Pattern
//			.compile("<img(.+?)src=\"(.+?)\"(.+?)>");
//	private static List<String> GetImagesList(String html) {
//		List<String> listSrc = new ArrayList<String>();
//		Matcher m = patternImgSrc.matcher(html);
//		while (m.find()) {// 要防止重复
//			if(!listSrc.contains(m.group(2)))
//				listSrc.add(m.group(2));
//		}
//
//		return listSrc;
//	}
//	
//	// 拼接数据包下载地址以及本地保存的文件名
//	public static String setDownPara(String downFileName, AjaxParams params, AdapterModel adapterModel) {
//		int hid = 0,departmentId = 0,doctorid = 0,diseaseId = 0;
//		hid = adapterModel.getHospitalId();
//		Class<? extends BaseClass> adaClass = adapterModel.getObject().getClass();
//		if (Hospital.class.equals(adaClass)) {
//			hid = adapterModel.getId();
//		}else if (Department.class.equals(adaClass)) {
//			departmentId = adapterModel.getId();
//		}else if (Doctor.class.equals(adaClass)) {
//			doctorid = adapterModel.getId();
//		}else if (Disease.class.equals(adaClass)) {
//			diseaseId =adapterModel.getId();
//		}
//		return downFileName = setDownPara(downFileName, params, hid, departmentId, doctorid, diseaseId);
//	}
//	// 拼接数据包下载地址以及本地保存的文件名
//	public static String setDownPara(String downFileName, AjaxParams params
//			,int hid ,int departmentId,int doctorid,int diseaseId) {
//		if(params==null)params = new AjaxParams();
//		if (hid>0) {
//			downFileName +="hid-"+hid;
//	        params.put("hid", hid+"");
//		}
//		if (departmentId>0) {
//			downFileName +="_departmentId-"+departmentId;
//	        params.put("departmentId",departmentId+"");
//		}
//		if (doctorid>0) {
//			downFileName +="_doctorid-"+doctorid;
//	        params.put("doctorid", doctorid+"");
//		}
//		if (diseaseId>0) {
//			downFileName +="_diseaseId-"+diseaseId;
//	        params.put("diseaseId",diseaseId+"");
//		}		
//		downFileName +=".zip";
//		return downFileName;
//	}
//	// 拼接数据包下载地址以及本地保存的文件名
//	public static String setDownPara(String downFileName, AjaxParams params, IdConfig idConfig) {
//		int hid = idConfig.getHospitalId();
//		int departmentId = idConfig.getDepartmentId();
//		int doctorid = idConfig.getDoctorId();
//		int diseaseId = idConfig.getDiseaseId();
//		return Tools.setDownPara(downFileName, params, hid, departmentId, doctorid, diseaseId);
//	}
//	
//	/**
//	 * 删除字符串中所有的空格,包括前后,中间
//	 * 用途:如登录时,部分输入法会在每个单词后面追加空格
//	 */
//	public static String trimAll(String str) {
//		if(!IsNullOrWhiteSpace(str)){
//			str = str.trim();
//			if(str.indexOf(" ")>-1)str=str.replace(" ", "");
//		}
//		return str;
//	}
//	
//	public static <T> List<NameValuePair> ToParas(T t) {
//		List<NameValuePair> paras = new ArrayList<NameValuePair>();
//		if (t!=null) {
//			try {
//				Class classT = t.getClass();
//				if(classT!=null){
//					Field[] Fields = classT.getDeclaredFields();
//					for (Field field : Fields) {
//						// 排除关联的对象属性
//						if(!field.getType().getName().startsWith(Config.ModelPackageName)
//								&&!field.getType().equals(List.class)){
//
//							String fieldName=field.getName();    
//					        String stringLetter=fieldName.substring(0, 1).toUpperCase();    
//					        Class type=field.getType();    
//					        //获得相应属性的getXXX和setXXX方法名称    
//					        String getName="get"+stringLetter+fieldName.substring(1);    
//					        if(type.equals(boolean.class))
//					        	 getName="is"+stringLetter+fieldName.substring(1);    
//							//获取相应的方法    
//							Method getMethod = classT.getMethod(getName, new Class[]{});
//							Object value = getMethod.invoke(t, new Object[]{});
//							if(value!=null&&!IsNullOrWhiteSpace((String.valueOf(value)))){
//								if(type.equals(Date.class)){
//									value = DateTools.format((Date)value, DateTools.DateFormat3);
//								}
//								paras.add(new BasicNameValuePair(fieldName,String.valueOf(value)));
//							}
//						}
//					}
//				}
//			} catch (SecurityException e) {
//				e.printStackTrace();
//			} catch (NoSuchMethodException e) {
//				e.printStackTrace();
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				e.printStackTrace();
//			}
//		}
//		return paras;
//	}
//	
//	// 复制 apk 中 asset 中文件到指定目录下
//	public static boolean CloneFileFromAsset(MyApp app,String assetFile,String targetFilePath) {
//		boolean r = false;String msg="";
//		try {
//			if(Arrays.binarySearch(app.getAssets().list(""),assetFile)>=0){
//				InputStream is = app.getAssets().open(assetFile);
//				File targetFile = new File(targetFilePath);
//				if(targetFile.getParentFile().exists())FileAccess.MakeDir(targetFile.getParent());
//				// 输出流
//				FileOutputStream fos = new FileOutputStream(targetFilePath);
//				byte[] buffer = new byte[8192];
//				int count = 0;
//				// 开始复制db文件
//				while ((count = is.read(buffer)) > 0) {
//					fos.write(buffer, 0, count);
//				}
//				fos.close();is.close();
//				r=true;
//				app.Log.i("CloneFileFromAsset", "DB2Data success");
//			}else {
//				msg = "Assets 中不存在 "+assetFile+" 文件";
//				app.Log.i("CloneFileFromAsset", msg);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//			app.Log.e("", e.getMessage());
//		}
//		return r;
//	}
//	
//	public static <T> T getIdConfig(MyApp app, String assetFile, Class<T> class1){
//		T t = null;
//		try {
//			InputStream is = app.getAssets().open(assetFile);
//			byte [] buffer = new byte[is.available()] ; 
//			is.read(buffer);
//			String json = new String(buffer,"utf-8");
//			if(!IsNullOrWhiteSpace(json)) t = JSON.parseObject(json, class1);
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return t;
//	}
//	
//	public static String[] getNowWeek() {
//		String[] nowWeekStrings = new String[2];
//		Date dt = new Date();
//		int this_day = dt.getDay();
//		// 如果周日作为一周的最后一天
//		int step_s2 = -this_day + 1; // 上周日距离今天的天数（负数表示）
//		if (this_day == 0) {
//			step_s2 = -6; // 如果今天是周日
//		}
//		int step_m2 = 7 - this_day; // 周日距离今天的天数（负数表示）
//		long thisTime = System.currentTimeMillis();
//		Date monday = new Date(thisTime + (long) step_s2 * 24 * 3600 * 1000);
//		Date sunday = new Date(thisTime + (long) step_m2 * 24 * 3600 * 1000);
//		nowWeekStrings[0] = getDateToString(monday, "yyyy-MM-dd");
//		nowWeekStrings[1] = getDateToString(sunday, "yyyy-MM-dd");
//		return nowWeekStrings;
//	}
//
//	/**
//	 * 日期转换为字符串格式
//	 * 
//	 * @param date
//	 *            Date类型
//	 * @param sFormate
//	 *            : "yyyy-MM-dd HH:mm:ss:SSSS"
//	 * @return 字符串
//	 */
//	public static String getDateToString(Date date, String sFormate) {
//		SimpleDateFormat dateFormat = new SimpleDateFormat(sFormate);
//		return dateFormat.format(date);
//	}

}