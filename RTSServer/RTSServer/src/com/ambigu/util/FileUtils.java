package com.ambigu.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class FileUtils {

	public FileUtils() {
		// TODO Auto-generated constructor stub
	}
	
	 /**
     * ��ͼƬת����Base64������ַ���
     * @param path
     * @return base64������ַ���
     */
    public static String imageToBase64(String path){
        if(path==null){
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try{
            is = new FileInputStream(path);
            //����һ���ַ�����С�����顣
            data = new byte[is.available()];
            //д������
            is.read(data);
            //��Ĭ�ϵı����ʽ���б���
            result = Base64.encode(data);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(null !=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }
    
    /**
     *base64�����ַ���ת����ͼƬ�ļ���
     * @param base64Str
     * @param path �ļ��洢·��
     * @return �Ƿ�ɹ�
     */
    public static boolean base64ToFile(String base64Str,String path){
        byte[] data;
		try {
			File file=new File(path);
			File dirs=new File(file.getParent());
			if(!dirs.exists()) dirs.mkdirs();
			if(!file.exists()) file.createNewFile();
			data = Base64.decode(base64Str);
	        for (int i = 0; i < data.length; i++) {
	            if(data[i] < 0){
	                //�����쳣����
	                data[i] += 256;
	            }
	        }
	        OutputStream os = null;
	        try {
	            os = new FileOutputStream(path);
	            os.write(data);
	            os.flush();
	            os.close();
	            return true;
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	            return false;
	        }catch (IOException e){
	            e.printStackTrace();
	            return false;
	        }

		} catch (Base64DecodingException e1) {
			// TODO Auto-generated catch block
            return false;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
            return false;
		}
    }






}
