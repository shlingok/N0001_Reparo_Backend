package com.hyperchain.contract;

import cn.hyperchain.sdk.rpc.Transaction.Transaction;
import cn.hyperchain.sdk.rpc.returns.CompileReturn;
import com.hyperchain.ESDKConnection;
import com.hyperchain.ESDKUtil;
import com.hyperchain.exception.ESDKException;
import com.hyperchain.test.base.SpringBaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * by chenxiaoyang on 2017/4/5.
 */
public class OrderContractTest extends SpringBaseTest{

    //买方公私钥
    String payerPublicKey = "dc3f0065386fcd8fe7efb4170cf1cb2a653be716";
    String payerPrivateKey = "{\"address\":\"dc3f0065386fcd8fe7efb4170cf1cb2a653be716\",\"encrypted\":\"5e0615268332c2c7576baf8518a14225993c77d0787c384c71599c08170b5449\",\"version\":\"2.0\",\"algo\":\"0x03\"}";

    //卖方公私钥
    String payeePublicKey = "5aad582bf2a8b1e266607ce63907c61bbcbf4904";
    String payeePrivateKey = "{\"address\":\"5aad582bf2a8b1e266607ce63907c61bbcbf4904\",\"encrypted\":\"efdf65aa522be9a638db43aa2f2d1bb324499aad19da20b71606e22a0150c0b2\",\"version\":\"2.0\",\"algo\":\"0x03\"}";

    @Test
    public void newAccount() throws Exception{

        List<String> keyInfos1 = ESDKUtil.newAccount();
        String publicKey1 = keyInfos1.get(0);
        String privateKey1 = keyInfos1.get(1);
        System.out.println("publicKey:"+publicKey1);
        System.out.println("privateKey:"+privateKey1);


        List<String> keyInfos2 = ESDKUtil.newAccount();
        String publicKey2 = keyInfos2.get(0);
        String privateKey2 = keyInfos2.get(1);
        System.out.println("publicKey:"+publicKey2);
        System.out.println("privateKey:"+privateKey2);
    }

    @Test
    public void addOrder() throws Exception {
        long unitPrice = 100;
        long prodNum = 100;
        long timeStamp = System.currentTimeMillis();

        java.util.Random random = new java.util.Random();

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");//设置日期格式
        String orderId = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

        orderId = "20170405205903000";
        String funcName = "createOrder";
        Object[] params = new Object[11];
        params[0] = orderId;
        params[1] = "5aad582bf2a8b1e266607ce63907c61bbcbf4904";
        params[2] = "product";
        params[3] = unitPrice; //单价
        params[4] = prodNum; //
        params[5] = unitPrice * prodNum; //
        params[6] = "ICBC";
        params[7] = "ii";
        params[8] = "ii";
        params[9] = 0;
        params[10] = timeStamp;


        Transaction transaction = ESDKUtil.getTxHash(payerPublicKey, funcName, params);
        transaction.sign(payerPrivateKey, null);

        String result = ESDKConnection.invokeContractMethod(transaction);
        Assert.assertNotNull(result);
        System.out.println("==================invoke result:================== " + result);
        //返回值解码
        List<Object> retDecode = ESDKUtil.retDecode(funcName, result);
        System.out.println("==================after decode result:==================" + retDecode);
    }

    @Test
    public void queryOrderDetailTest() throws Exception {
        String orderId = "20170405205903000";// new Date()为获取当前系统时间，也可使用当前时间戳

        String funcName = "queryOrderDetail";
        Object[] params = new Object[1];
        params[0] = orderId;

        Transaction transaction = ESDKUtil.getTxHash(payerPublicKey, funcName, params);
        transaction.sign(payerPrivateKey, null);

        String result = ESDKConnection.invokeContractMethod(transaction);
        Assert.assertNotNull(result);
        //System.out.println("==================invoke result:================== " + result);

        //返回值解码
        List<Object> retDecode = ESDKUtil.retDecode(funcName, result);
        System.out.println("==================after decode result:==================" + retDecode);

    }

    @Test
    public void queryAllOrderListForPayerTest() throws Exception {
        String orderId = "20170405205903840";// new Date()为获取当前系统时间，也可使用当前时间戳

        String funcName = "queryAllOrderListForPayer";
        Object[] params = new Object[1];
        params[0] = orderId;

        Transaction transaction = ESDKUtil.getTxHash(payerPublicKey, funcName, params);
        transaction.sign(payerPrivateKey, null);

        String result = ESDKConnection.invokeContractMethod(transaction);
        Assert.assertNotNull(result);
        //System.out.println("==================invoke result:================== " + result);

        //返回值解码
        List<Object> retDecode = ESDKUtil.retDecode(funcName, result);
        System.out.println("==================after decode result:==================" + retDecode);

    }


    @Test
    public void queryAllOrderListForPayeeTest() throws Exception {
        String orderId = "20170405205903840";// new Date()为获取当前系统时间，也可使用当前时间戳

        String funcName = "queryAllOrderListForPayee";
        Object[] params = new Object[1];
        params[0] = orderId;

        Transaction transaction = ESDKUtil.getTxHash(payeePublicKey, funcName, params);
        transaction.sign(payeePrivateKey, null);

        String result = ESDKConnection.invokeContractMethod(transaction);
        Assert.assertNotNull(result);
        //System.out.println("==================invoke result:================== " + result);

        //返回值解码
        List<Object> retDecode = ESDKUtil.retDecode(funcName, result);
        System.out.println("==================after decode result:==================" + retDecode);

    }


//
//    @Test //由买方进行确认时会返回22-订单仅允许卖方进行确认
//    public void orderConfirm() throws Exception {
//        String orderId = "20170405205903840";// new Date()为获取当前系统时间，也可使用当前时间戳
//
//        String funcName = "orderConfirm";
//        Object[] params = new Object[1];
//        params[0] = orderId; //+"92a87ad2c26d80705cf1f0d7c0c1f6ecb140459e";
//
//
//        Transaction transaction = ESDKUtil.getTxHash(payerPublicKey, funcName, params);
//        transaction.sign(payerPrivateKey, null);
//
//        String result = ESDKConnection.invokeContractMethod(transaction);
//        Assert.assertNotNull(result);
//        //System.out.println("==================invoke result:================== " + result);
//
//        //返回值解码
//        List<Object> retDecode = ESDKUtil.retDecode(funcName, result);
//        System.out.println("==================after decode result:==================" + retDecode);
//        Assert.assertEquals("22", retDecode.get(0));
//      }
//
//    @Test //由卖方进行确认时会返回0-成功
//    public void orderConfirm_2() throws Exception {
//        String orderId = "20170405205903840";// new Date()为获取当前系统时间，也可使用当前时间戳
//
//        String funcName = "orderConfirm";
//        Object[] params = new Object[1];
//        params[0] = orderId; //+"92a87ad2c26d80705cf1f0d7c0c1f6ecb140459e";
//
//
//        Transaction transaction = ESDKUtil.getTxHash(payeePublicKey, funcName, params);
//        transaction.sign(payeePrivateKey, null);
//
//        String result = ESDKConnection.invokeContractMethod(transaction);
//        Assert.assertNotNull(result);
//        //System.out.println("==================invoke result:================== " + result);
//
//        //返回值解码
//        List<Object> retDecode = ESDKUtil.retDecode(funcName, result);
//        System.out.println("==================after decode result:==================" + retDecode);
//        Assert.assertEquals("0", retDecode.get(0));
//    }
}
