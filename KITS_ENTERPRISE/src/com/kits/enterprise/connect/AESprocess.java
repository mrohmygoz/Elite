package com.kits.enterprise.connect;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class AESprocess {
	static byte[] iv = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};;
	static String key = "QUFBQUE5MThEOTMyOEJCQkJCQkJCODhFMTM3MURFREQ=";
	static String value = "7d9eb9ac449625d396fff50b229ee884d79bd2b67dadbcc9f1c575161d84ea0abcb6ea7a8deb1367da7a6d6e768c7ef671013dbb662b362ff238e09f1d1a444250dbda2952ef684738b55136c92e4769";
	
	public static void test(){
		byte[] result = DecryptAES(Base64.decode(key, Base64.DEFAULT), hexToBytes(value));
	}
	//AES�ѱK�A�a�Jbyte[]���A��16��^�ƲզX��r�B32��^�ƲզXKey�B�ݸѱK��r
	public static byte[] DecryptAES(byte[] key,byte[] text)
	{
		
	  try
	  {
	    AlgorithmParameterSpec mAlgorithmParameterSpec = new IvParameterSpec(iv);
	    SecretKeySpec mSecretKeySpec = new SecretKeySpec(key, "AES");
	    Cipher mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    mCipher.init(Cipher.DECRYPT_MODE, 
	                 mSecretKeySpec, 
	                 mAlgorithmParameterSpec);
	        
	    return mCipher.doFinal(text);
	  }
	  catch(Exception ex)
	  {
	    return null;
	  }
	}
	
	public static byte[] hexToBytes(String hexString) {

	    char[] hex = hexString.toCharArray();
	    //��rawData���״�b
	    int length = hex.length / 2;
	    byte[] rawData = new byte[length];
	    for (int i = 0; i < length; i++) {
	      //���Nhex�����10�i��ƭ�
	      int high = Character.digit(hex[i * 2], 16);
	      int low = Character.digit(hex[i * 2 + 1], 16);
	      //�N�Ĥ@�ӭȪ��G�i��ȥ�����4��,ex: 00001000 => 10000000 (8=>128)
	      //�M��P�ĤG�ӭȪ��G�i��ȧ@�p��ex: 10000000 | 00001100 => 10001100 (137)
	      int value = (high << 4) | low;
	      //�PFFFFFFFF�@�ɶ�
	      if (value > 127)
	        value -= 256;
	      //�̫���^byte�NOK
	      rawData[i] = (byte) value;
	    }
	    return rawData;
	}
}
