package com.xzpx_zc.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

/**
 * 对称加密 RSA
* @author ZC  
* @date 2018年11月8日 上午11:07:20
* @version
 */
public class RSAUtil {
	//非对称密钥算法
    public static final String CHARSET = "UTF-8";
    public static final String RSA_ALGORITHM = "RSA";

    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";
    /**
     * 密钥长度，DH算法的默认密钥长度是1024
     * 密钥长度必须是64的倍数，在512到65536位之间
     */
    private static final int KEY_SIZE = 1024;
	 //公钥
    private static final String PUBLIC_KEY = "RSAPublicKey";

    //私钥
    private static final String PRIVATE_KEY = "RSAPrivateKey";
    
    public static Map<String, String> initKey() throws Exception {
    	//为RSA算法创建一个KeyPairGenerator对象
        KeyPairGenerator kpg;
        try{
            kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        }catch(NoSuchAlgorithmException e){
            throw new IllegalArgumentException("No such algorithm-->[" + RSA_ALGORITHM + "]");
        }

        //初始化KeyPairGenerator对象,密钥长度
        kpg.initialize(KEY_SIZE);
        //生成密匙对
        KeyPair keyPair = kpg.generateKeyPair();
        //得到公钥
        Key publicKey = keyPair.getPublic();
        String publicKeyStr = Base64.encodeBase64URLSafeString(publicKey.getEncoded());
        //得到私钥
        Key privateKey = keyPair.getPrivate();
        String privateKeyStr = Base64.encodeBase64URLSafeString(privateKey.getEncoded());
        Map<String, String> keyPairMap = new HashMap<String, String>();
        keyPairMap.put(PUBLIC_KEY, publicKeyStr);
        keyPairMap.put(PRIVATE_KEY, privateKeyStr);
        return keyPairMap;
    }
    
    
    
    /**
     * 得到公钥对象
     * @param publicKey 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static RSAPublicKey getPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //通过X509编码的Key指令获得公钥对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));
        RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
        return key;
    }

    /**
     * 得到私钥对象
     * @param privateKey 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static RSAPrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //通过PKCS#8编码的Key指令获得私钥对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
        RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
        return key;
    }
    
    
    
    /**
     * 公钥加密
     * @param data
     * @param publicKey
     * @return
     */
    public static String publicEncrypt(String data, RSAPublicKey publicKey){
        try{
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.encodeBase64URLSafeString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), publicKey.getModulus().bitLength()));
        }catch(Exception e){
            throw new RuntimeException("加密字符串[" + data + "]时遇到异常", e);
        }
    }

    /**
     * 私钥解密
     * @param data
     * @param privateKey
     * @return
     */

    public static String privateDecrypt(String data, RSAPrivateKey privateKey){
        try{
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(data), privateKey.getModulus().bitLength()), CHARSET);
        }catch(Exception e){
            throw new RuntimeException("解密字符串[" + data + "]时遇到异常", e);
        }
    }

    /**
     * 私钥加密
     * @param data
     * @param privateKey
     * @return
     */

    public static String privateEncrypt(String data, RSAPrivateKey privateKey){
        try{
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return Base64.encodeBase64URLSafeString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), privateKey.getModulus().bitLength()));
        }catch(Exception e){
            throw new RuntimeException("加密字符串[" + data + "]时遇到异常", e);
        }
    }



    /**
     * 公钥解密
     * @param data
     * @param publicKey
     * @return
     */

    public static String publicDecrypt(String data, RSAPublicKey publicKey){
        try{
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(data), publicKey.getModulus().bitLength()), CHARSET);
        }catch(Exception e){
            throw new RuntimeException("解密字符串[" + data + "]时遇到异常", e);
        }
    }
    
    
    
    private static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas, int keySize) throws IOException{
		int maxBlock = 0;
		if (opmode == Cipher.DECRYPT_MODE) {
			maxBlock = keySize / 8;
		} else {
			maxBlock = keySize / 8 - 11;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] buff;
		int i = 0;
		try {
			while (datas.length > offSet) {
				if (datas.length - offSet > maxBlock) {
					buff = cipher.doFinal(datas, offSet, maxBlock);
				} else {
					buff = cipher.doFinal(datas, offSet, datas.length - offSet);
				}
				out.write(buff, 0, buff.length);
				i++;
				offSet = i * maxBlock;
			}
		} catch (Exception e) {
			throw new RuntimeException("加解密阀值为[" + maxBlock + "]的数据时发生异常", e);
		}
		byte[] resultDatas = out.toByteArray();
		out.close();
		return resultDatas;
    }

    /**
     * 签名
     * @param content
     * @param privateKey
     * @return
     * @throws Exception
     * @author ZC
     * @date 2018年11月19日 下午2:28:45
     */
    public static String sign(String content, String privateKey) throws Exception {
    	PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));   
        KeyFactory keyf = KeyFactory.getInstance(RSA_ALGORITHM);  
        PrivateKey priKey = keyf.generatePrivate(priPKCS8);  
        java.security.Signature signature = java.security.Signature.getInstance(SIGNATURE_ALGORITHM);  
        signature.initSign(priKey);  
        signature.update(content.getBytes());  
        byte[] signed = signature.sign();  
        return Base64.encodeBase64String(signed);  
    }

	public static boolean doCheck(String content, String sign, String publicKey) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
			byte[] encodedKey = Base64.decodeBase64(publicKey);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
			java.security.Signature signature = java.security.Signature.getInstance(SIGNATURE_ALGORITHM);
			signature.initVerify(pubKey);
			signature.update(content.getBytes());
			boolean bverify = signature.verify(Base64.decodeBase64(sign));
			return bverify;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}


	/**
	 * 将byte数组转成十六进制的字符串
	 * 
	 * @param b
	 *            byte[]
	 * @return String
	 */
	private static String bytes2HexString(byte[] b) {
		StringBuffer ret = new StringBuffer(b.length);
		String hex = "";
		for (int i = 0; i < b.length; i++) {
			hex = Integer.toHexString(b[i] & 0xFF);

			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret.append(hex);
		}
		return ret.toString();
	}
    
    public static void main(String[] args) throws Exception {
    String s = "{\"attach\":\"181119014114527_nihaoma_49_http://mgm567.cc:80\",\"commodityBody\":\"pay\",\"commodityDetail\":\"paydetail\",\"commodityRemark\":\"payremark\",\"currenciesTy" + 
    		"pe\":\"CNY\",\"defrayalType\":\"WECHAT_NATIVE\",\"memberOrderNumber\":\"181119014114527\",\"notifyUrl\":\"http://47.90.78.66:8885/payhc/payMsg_bfbpayReturnMessage.action\",\"orderTime\":\"20181119134204\",\"returnUrl\":\"http://mgm567.cc:80\",\"terminalIP\":\"116.93.34.50\",\"tradeAmount\":\"13500\",\"tradeCheckCycle\":\"T1\"},";
    
    System.out.println("签名串：" + s);		

    String bfbPrivateKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCr9xr/dG93I/gqWixCuNULeB4v" + 
    		"TdKgtcRiKrqLc1wfwphu4/6UAb9yhHy6qkqFk7RTEblytsKWKDPaBl9HAT8J8BLl" + 
    		"xNZDEoXrM8OPxaip7jIVTaKyInKhcmET0qmuXCBLcKQwfleP1wyl1qCu+a2z+hg3" + 
    		"bvMKghI91a7wWCniEQIDAQAB";
    String key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKv3Gv90b3cj+Cpa" + 
    		"LEK41Qt4Hi9N0qC1xGIquotzXB/CmG7j/pQBv3KEfLqqSoWTtFMRuXK2wpYoM9oG" + 
    		"X0cBPwnwEuXE1kMSheszw4/FqKnuMhVNorIicqFyYRPSqa5cIEtwpDB+V4/XDKXW" + 
    		"oK75rbP6GDdu8wqCEj3VrvBYKeIRAgMBAAECgYEAitcyvAeVK1smNYOicqhqkh0E" + 
    		"OesaYNkA2sVm4cpdGeNyUS3RUExs9xfS4J83FcwgbmVEFkNKrhN9cc1tRZXS/28b" + 
    		"yE9WrOrLBHHMQXXrtnHNYEwfTRCm4PYsNh3oDuBH+qdcRBpDYk63RQEscyi15MQl" + 
    		"uUg6qert568N4cqM36UCQQDX+dkW6rqeS/KE52lXSsAK9xG/oE1X3Ck6JaWe07EA" + 
    		"FHh5+f32GAZsSACBr0cf4RDPJ5IqDNO79j9EGR2jZu4nAkEAy9VWfoBpJidjEw3j" + 
    		"wsWkMR2Y+lx5VZPudCHYVMXmLHCPIZ0Bo+37hOByc66pe+zD3RZY2KQ7F2j//cXV" + 
    		"JHYJBwJAdFLra/5tGQlKy+5fvFZUbRN5ib5rKeE4i0rvk0XtVV+xK/FLqZpzCysU" + 
    		"qsSfCDqOdSSZAvD5sYkFtkXYwsOTjQJBAKIwD5G6tXAReQjpTWhmgP4/0cCsojMQ" + 
    		"8XTglVTm3v5PVeRmHK8GptKVERyxRtR/kV2y8WD4VLiM6NxRdQZ9ETMCQBGdutWj" + 
    		"gAakDLuA8UNpFTVi19riF0BTZUYpVhcuBhUi0HSrgOjhmWDeq9otSTjqMbMCZ9Vo" + 
    		"K5RsY7r76ohKvf4="	;
    String sign = 	sign(s, key)	;
    
    
    boolean isSign =  doCheck(s, sign, bfbPrivateKey);
    System.out.println(isSign);
//    	Map<String, String> keyMap = initKey();
//    	String publickey = keyMap.get(PUBLIC_KEY);
//    	String privatekey = keyMap.get(PRIVATE_KEY);
//    	System.out.println("publickey:" + publickey);
//    	System.out.println("privatekey:" + privatekey);
//    	
//    	
//    	  System.out.println("公钥加密——私钥解密");
//          String str = "站在大明门前守卫的禁卫军，事先没有接到\n" +
//                  "有关的命令，但看到大批盛装的官员来临，也就\n" +
//                  "以为确系举行大典，因而未加询问。进大明门即\n" +
//                  "为皇城。文武百官看到端门午门之前气氛平静，\n" +
//                  "城楼上下也无朝会的迹象，既无几案，站队点名\n" +
//                  "的御史和御前侍卫“大汉将军”也不见踪影，不免\n" +
//                  "心中揣测，互相询问：所谓午朝是否讹传？";
//          System.out.println("\r明文：" + str);
//          System.out.println("\r明文大小：" + str.getBytes().length);
//          String encodedData = publicEncrypt(str, getPublicKey(publickey));
//          System.out.println("密文：" + encodedData);
//          String decodedData = privateDecrypt(encodedData, getPrivateKey(privatekey));
//          System.out.println("解密后文字: " + decodedData);
	}
	
	
	
}
