package com.hyperchain.service.impl;

import cn.hyperchain.common.log.LogUtil;
import com.hyperchain.common.constant.Code;
import com.hyperchain.common.exception.ContractInvokeFailException;
import com.hyperchain.common.exception.PasswordIllegalParam;
import com.hyperchain.common.exception.ValueNullException;
import com.hyperchain.common.util.MoneyUtil;
import com.hyperchain.contract.ContractKey;
import com.hyperchain.contract.ContractResult;
import com.hyperchain.contract.ContractUtil;
import com.hyperchain.controller.vo.*;
import com.hyperchain.controller.vo.OperationRecordVo;
import com.hyperchain.dal.repository.UserEntityRepository;
import com.hyperchain.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liangyue on 2017/4/9.
 */
@Service
public class OrderServiceImpl implements OrderService {

    public static final String EMPTY_STRING = "x0000000000000000000000000000000000000000";
    public static final int GENERATED = 1;
    public static final int CONFIRMED = 2;
    public static final int COMPLETED = 3;
    public static final String CREATE_ORDER = "createOrder";
    public static final String QUERY_ORDER_DETAIL = "queryOrderDetail";
    public static final String TX_DETAIL = "txDetail";
    public static final String RECEIVE_OVER = "receOver";
    public static final String WAY_BILL_OVER = "wayBillOver";
    public static final String REPO_OVER = "repoOver";
    public static final String QUERY_ALL_ORDER_OVER_VIEW_INFO_LIST = "queryAllOrderOverViewInfoList";
    public static final String CONFIRM_ORDER = "confirmOrder";
    @Autowired
    private UserEntityRepository userEntityRepository;

    @Override
    public BaseResult<Object> createOrder(ContractKey contractKey, Object[] contractParams, String orderId) throws ContractInvokeFailException, ValueNullException, PasswordIllegalParam {
        String methodName = CREATE_ORDER;
        String[] resultMapKey = new String[]{};
        BaseResult result = new BaseResult();

        try {
            ContractResult contractResult = ContractUtil.invokeContract(contractKey, methodName, contractParams, resultMapKey, "OrderContract");
            Code code = contractResult.getCode();
            LogUtil.debug("调用合约 : OrderContract 方法: createOrder()返回结果：" + contractResult.toString());
            if (code == Code.SUCCESS) {
                result.returnWithValue(code, orderId);
            } else {
                result.returnWithoutValue(code);
            }

        } catch (ContractInvokeFailException e) {
            e.printStackTrace();
            throw new ContractInvokeFailException();
        } catch (ValueNullException e) {
            e.printStackTrace();
            throw new ValueNullException();
        } catch (PasswordIllegalParam passwordIllegalParam) {
            passwordIllegalParam.printStackTrace();
            throw new PasswordIllegalParam();

        }
        return result;
    }

    @Override
    public BaseResult<Object> queryOrderDetail(ContractKey contractKey, Object[] contractParams) throws ContractInvokeFailException, ValueNullException, PasswordIllegalParam {

        String contractMethodName = QUERY_ORDER_DETAIL;
        String[] resultMapKey = new String[]{"address1[]", "bytesParams", "uintParams", "method", "txState"};

        // 利用（合约钥匙，合约方法名，合约方法参数，合约方法返回值名）获取调用合约结果
        ContractResult contractResult = null;
        Code code = null;
        try {
            contractResult = ContractUtil.invokeContract(contractKey, contractMethodName, contractParams, resultMapKey, "OrderContract");
            LogUtil.debug("调用合约 : OrderContract 方法: queryOrderDetail 返回结果：" + contractResult.toString());
            code = contractResult.getCode();
            if (code != Code.SUCCESS) {
                BaseResult result = new BaseResult();
                result.returnWithoutValue(code);
                return result;
            }
        } catch (ContractInvokeFailException e) {
            e.printStackTrace();
            throw new ContractInvokeFailException();
        } catch (ValueNullException e) {
            e.printStackTrace();
            throw new ValueNullException();
        } catch (PasswordIllegalParam passwordIllegalParam) {
            passwordIllegalParam.printStackTrace();
            throw new PasswordIllegalParam();

        }

        BaseResult<Object> result = new BaseResult<>();
        Map<String, Object> orderDetailMap = new HashMap();
        //将合约结果转化为接口返回数据
        List<String> addressList = (List<String>) contractResult.getValueMap().get(resultMapKey[0]);
        List<String> partParams1 = (List<String>) contractResult.getValueMap().get(resultMapKey[1]);
        List<String> partParams2 = (List<String>) contractResult.getValueMap().get(resultMapKey[2]);
        String payingMethodString = (String) contractResult.getValueMap().get(resultMapKey[3]);
        String txStateString = (String) contractResult.getValueMap().get(resultMapKey[4]);

        int payingMethodInt;
        if(payingMethodString.equals("")){
            payingMethodInt = 0;
        }else{
            payingMethodInt = Integer.parseInt(payingMethodString);
        }
        int txStateInt;
        if(txStateString.equals("")){
            txStateInt = 0;
        }else{
            txStateInt = Integer.parseInt(txStateString);
        }

        String payerAddress = addressList.get(0).substring(1);
        String payeeAddress = addressList.get(1).substring(1);
        String payerRepoAddress;
        if(addressList.get(2).equals("")){
            payerRepoAddress = "";
        }else{
            payerRepoAddress = addressList.get(2).substring(1);
        }
        String payeeRepoAddress;
        if(addressList.get(3).equals(EMPTY_STRING)){
            payeeRepoAddress = "";
        }else{
            payeeRepoAddress = addressList.get(3).substring(1);
        }

        String payerCompanyName = userEntityRepository.findByAddress(payerAddress).getCompanyName();
        String payeeCompanyName = userEntityRepository.findByAddress(payeeAddress).getCompanyName();
        String payerRepoName;
        if(payerRepoAddress.equals("")){
            payerRepoName = "";
        }else{
            payerRepoName = userEntityRepository.findByAddress(payerRepoAddress).getCompanyName();
        }
        String payeeRepoName;
        if(payeeRepoAddress.equals("")){
            payeeRepoName = "";
        }else{
            payeeRepoName = userEntityRepository.findByAddress(payeeRepoAddress).getCompanyName();
        }

        //获取交易信息
        String orderId = partParams1.get(0);
        String productName = partParams1.get(1);
        String payerBank = partParams1.get(2);
        String payerBankClass = partParams1.get(3);
        String payerAccount = partParams1.get(4);

        //以下为仓储详情
        String payerRepoBusinessNo = partParams1.get(5);//买家仓储流水号
        String payeeRepoBusinessNo = partParams1.get(6);//卖家仓储流水号
        String payerRepoCertNo = partParams1.get(7);    //买家仓单编号
        String payeeRepoCertNo = partParams1.get(8);    //卖家仓单编号

        long productUnitPrice = Long.parseLong(partParams2.get(0));
        long productQuantity = Long.parseLong(partParams2.get(1));
        long productTotalPrice = Long.parseLong(partParams2.get(2));
        long orderGenerateTime = Long.parseLong(partParams2.get(3));
        long orderConfirmTime;
        if(partParams2.get(4).equals("")){
            orderConfirmTime = 0;
        }else{
            orderConfirmTime = Long.parseLong(partParams2.get(4));
        }
        int payerRepoBusinessState;
        if(partParams2.get(5).equals("")){
            payerRepoBusinessState = 0;
        }else{
            payerRepoBusinessState = Integer.parseInt(partParams2.get(5));
        }
        int payeeRepoBusinessState;
        if(partParams2.get(6).equals("")){
            payeeRepoBusinessState = 0;
        }else{
            payeeRepoBusinessState = Integer.parseInt(partParams2.get(6));
        }

        //物流信息详情
        String wayBillNo = partParams1.get(9);//物流单号
        String logisticCompany = addressList.get(4);//物流公司

        long wayBillGenerateTime;
        if(partParams2.get(7).equals("")){
            wayBillGenerateTime = 0;
        }else{
            wayBillGenerateTime = Long.parseLong(partParams2.get(7));
        }
        int wayBillLatestStatus;
        if(partParams2.get(8).equals("")){
            wayBillLatestStatus = 0;
        }else{
            wayBillLatestStatus = Integer.parseInt(partParams2.get(8));
        }
        long wayBillUpdateTime;
        if(partParams2.get(9).equals("")){
            wayBillUpdateTime = 0;
        }else{
            wayBillUpdateTime = Long.parseLong(partParams2.get(9));
        }

        //以下为应收账款概要信息

        String receiveNo = partParams1.get(10);
        String receivingSide = partParams1.get(11);
        String payingSide = partParams1.get(12);
        long coupon;
        if(partParams1.get(13).equals("")){
            coupon = 0;
        }else{
            coupon = Long.parseLong(partParams1.get(13));
        }
        long receiveGenerateTime;
        if(partParams2.get(10).equals("")){
            receiveGenerateTime = 0;
        }else{
            receiveGenerateTime = Long.parseLong(partParams2.get(10));
        }
        long receiveAmount;
        if(partParams2.get(11).equals("")){
            receiveAmount = 0;
        }else{
            receiveAmount = Long.parseLong(partParams2.get(11));
        }
        int receiveLatestStatus;
        if(partParams2.get(12).equals("")){
            receiveLatestStatus = 0;
        }else{
            receiveLatestStatus = Integer.parseInt(partParams2.get(12));
        }
        long receiveUpdateTime;
        if(partParams2.get(13).equals("")){
            receiveUpdateTime = 0;
        }else{
            receiveUpdateTime = Long.parseLong(partParams2.get(13));
        }
        long dueDate;
        if(partParams2.get(14).equals("")){
            dueDate = 0;
        }else{
            dueDate = Long.parseLong(partParams2.get(14));
        }
        long orderCompleteTime;
        if(partParams2.get(15).equals("")){
            orderCompleteTime = 0;
        }else{
            orderCompleteTime = Long.parseLong(partParams2.get(15));
        }

        TransactionDetailVo txDetailVo = new TransactionDetailVo();
        List<OperationRecordVo> txRecordList = new ArrayList<>();


        txRecordList.add(new OperationRecordVo(GENERATED, orderGenerateTime));
        if (txStateInt == CONFIRMED) {
            txRecordList.add(new OperationRecordVo(txStateInt, orderConfirmTime));
        } else if (txStateInt == COMPLETED) {
            txRecordList.add(new OperationRecordVo(CONFIRMED, orderConfirmTime));
            txRecordList.add(new OperationRecordVo(txStateInt, orderCompleteTime));
        }

        txDetailVo.setPayerCompanyName(payerCompanyName);
        txDetailVo.setPayeeCompanyName(payeeCompanyName);
        txDetailVo.setPayingMethod(payingMethodInt);
        txDetailVo.setProductUnitPrice(MoneyUtil.convertCentToYuan(productUnitPrice));
        txDetailVo.setProductQuantity(productQuantity);
        txDetailVo.setProductTotalPrice(MoneyUtil.convertCentToYuan(productTotalPrice));
        txDetailVo.setOrderId(orderId);
        txDetailVo.setOperationRecordVoList(txRecordList);
        txDetailVo.setProductName(productName);
        txDetailVo.setPayerBank(payerBank);
        txDetailVo.setPayerBankClss(payerBankClass);
        txDetailVo.setPayerAccount(payerAccount);
        txDetailVo.setPayeeRepo(payeeRepoName);
        txDetailVo.setPayerRepo(payerRepoName);
        txDetailVo.setPayeeRepoBusinessNo(payeeRepoBusinessNo);
        txDetailVo.setPayerRepoBusinessNo(payerRepoBusinessNo);
        txDetailVo.setPayeeRepoCertNo(payeeRepoCertNo);
        txDetailVo.setPayerRepoCertNo(payerRepoCertNo);

        ReceOverVo receOverVo = new ReceOverVo();
        receOverVo.setReceNo(receiveNo);
        receOverVo.setReceivingSide(receivingSide);
        receOverVo.setPayingSide(payingSide);
        receOverVo.setDueDate(dueDate);
        receOverVo.setReceGenerateTime(receiveGenerateTime);
        receOverVo.setReceAmount(MoneyUtil.convertCentToYuan(receiveAmount));
        receOverVo.setCoupon(coupon);
        receOverVo.setReceLatestStatus(receiveLatestStatus);
        receOverVo.setReceUpdateTime(receiveUpdateTime);

        WayBillOverInfo wayBillOverInfo = new WayBillOverInfo();
        wayBillOverInfo.setLogisticCompany(logisticCompany);
        wayBillOverInfo.setWayBillGenerateTime(wayBillGenerateTime);
        wayBillOverInfo.setWayBillLatestStatus(wayBillLatestStatus);
        wayBillOverInfo.setWayBillNo(wayBillNo);
        wayBillOverInfo.setWayBillUpdateTime(wayBillUpdateTime);

        RepoOverVo repoOverVo = new RepoOverVo();
        repoOverVo.setPayerRepoCertNo(payerRepoCertNo);
        repoOverVo.setPayeeRepoCertNo(payeeRepoCertNo);
        repoOverVo.setPayerRepoBusinessNo(payerRepoBusinessNo);
        repoOverVo.setPayeeRepoBusinessNo(payeeRepoBusinessNo);
        repoOverVo.setInApplyTime(orderGenerateTime);
        repoOverVo.setOutApplyTime(orderConfirmTime);
        repoOverVo.setPayeeRepoBusiState(payeeRepoBusinessState);
        repoOverVo.setPayerRepoBusiState(payerRepoBusinessState);

        orderDetailMap.put(TX_DETAIL, txDetailVo);
        orderDetailMap.put(RECEIVE_OVER, receOverVo);
        orderDetailMap.put(WAY_BILL_OVER, wayBillOverInfo);
        orderDetailMap.put(REPO_OVER, repoOverVo);

        result.returnWithValue(code, orderDetailMap);
        return result;
    }

    @Override
    public BaseResult<Object> queryAllOrderOverViewInfoList(ContractKey contractKey, Object[] contractParams) throws ContractInvokeFailException, ValueNullException, PasswordIllegalParam {
        String contractMethodName = QUERY_ALL_ORDER_OVER_VIEW_INFO_LIST;
        String[] resultMapKey = new String[]{"partList1", "partList2", "partList3", "methodList", "stateList"};
        ContractResult contractResult = null;
        Code code = null;
        try {
            contractResult = ContractUtil.invokeContract(contractKey, contractMethodName, contractParams, resultMapKey, "OrderContract");
            LogUtil.debug("调用合约 : OrderContract 方法: queryAllOrderOverViewInfoList 返回结果：" + contractResult.toString());
            code = contractResult.getCode();
            if (code != Code.SUCCESS) {
                BaseResult result = new BaseResult();
                result.returnWithoutValue(code);
                return result;
            }
        } catch (ContractInvokeFailException e) {
            e.printStackTrace();
            throw new ContractInvokeFailException();
        } catch (ValueNullException e) {
            e.printStackTrace();
            throw new ValueNullException();
        } catch (PasswordIllegalParam passwordIllegalParam) {
            passwordIllegalParam.printStackTrace();
            throw new PasswordIllegalParam();

        }

        List<String> partList1 = (List<String>) contractResult.getValueMap().get(resultMapKey[0]);
        List<String> partList2 = (List<String>) contractResult.getValueMap().get(resultMapKey[1]);
        List<String> partList3 = (List<String>) contractResult.getValueMap().get(resultMapKey[2]);
        List<String> methodList = (List<String>) contractResult.getValueMap().get(resultMapKey[3]);
        List<String> stateList = (List<String>) contractResult.getValueMap().get(resultMapKey[4]);
        int length = methodList.size();
        List<OrderOverVo> orderOverVoList = new ArrayList<>();

        for (int i = length - 1; i >= 0; i--) {
            OrderOverVo orderOverVo = new OrderOverVo();
            orderOverVo.setOrderNo(partList1.get(i * 4));
            orderOverVo.setProductName(partList1.get(i * 4 + 1));
            orderOverVo.setPayerBank(partList1.get(i * 4 + 2));
            orderOverVo.setPayerBankAccount(partList1.get(i * 4 + 3));

            String payerAddress = partList2.get(i * 4).substring(1);
            String payeeAddress = partList2.get(i * 4 + 1).substring(1);
            String payerRepoAddress;
            if(partList2.get(i * 4 + 2).equals(EMPTY_STRING)){
                payerRepoAddress = "";
            }else{
                payerRepoAddress = partList2.get(i * 4 + 2).substring(1);
            }
            String payeeRepoAddress;
            if(partList2.get(i * 4 + 3).equals(EMPTY_STRING)){
                payeeRepoAddress = "";
            }else{
                payeeRepoAddress = partList2.get(i * 4 + 3).substring(1);
            }

            String payerCompanyName = userEntityRepository.findByAddress(payerAddress).getCompanyName();
            String payeeCompanyName = userEntityRepository.findByAddress(payeeAddress).getCompanyName();
            String payerRepoName;
            if(payerRepoAddress.equals("")){
                payerRepoName = "";
            }else{
                payerRepoName = userEntityRepository.findByAddress(payerRepoAddress).getCompanyName();
            }
            String payeeRepoName;
            if(payeeRepoAddress.equals("")){
                payeeRepoName = "";
            }else{
                payeeRepoName = userEntityRepository.findByAddress(payeeRepoAddress).getCompanyName();
            }

            orderOverVo.setPayerCompanyName(payerCompanyName);
            orderOverVo.setPayeeCompanyName(payeeCompanyName);
            orderOverVo.setPayerRepoName(payerRepoName);
            orderOverVo.setPayeeRepoName(payeeRepoName);

            orderOverVo.setProductQuantity(Long.parseLong(partList3.get(i * 5)));
            orderOverVo.setProductUnitPrice(MoneyUtil.convertCentToYuan(Long.parseLong(partList3.get(i * 5 + 1))));

            long orderConfirmTime;
            if(partList3.get(i * 5 + 4).equals("")){
                orderConfirmTime = 0;
            }else{
                orderConfirmTime = Long.parseLong(partList3.get(i * 5 + 4));
            }
            orderOverVo.setProductTotalPrice(MoneyUtil.convertCentToYuan(Long.parseLong(partList3.get(i * 5 + 2))));
            orderOverVo.setOrderGenerateTime(Long.parseLong(partList3.get(i * 5 + 3)));
            orderOverVo.setOrderConfirmTime(orderConfirmTime);

            int txState;
            if(stateList.get(i * 4).equals("")){
                txState = 0;
            }else{
                txState = Integer.parseInt(stateList.get(i * 4));
            }
            int repoState;
            if(stateList.get(i * 4 + 1).equals("")){
                repoState = 0;
            }else{
                repoState = Integer.parseInt(stateList.get(i * 4 + 1));
            }
            int wayState;
            if(stateList.get(i * 4 + 2).equals("")){
                wayState = 0;
            }else{
                wayState = Integer.parseInt(stateList.get(i * 4 + 2));
            }
            int receiveState;
            if(stateList.get(i * 4 + 3).equals("")){
                receiveState = 0;
            }else{
                receiveState = Integer.parseInt(stateList.get(i * 4 + 3));
            }
            orderOverVo.setReceStatus(receiveState);
            orderOverVo.setRepoStatus(repoState);
            orderOverVo.setTransactionStatus(txState);
            orderOverVo.setWayBillStatus(wayState);
            int payingMethod;
            if(methodList.get(i).equals("")){
                payingMethod = 0;
            }else{
                payingMethod = Integer.parseInt(methodList.get(i));
            }
            orderOverVo.setPayingMethod(payingMethod);
            orderOverVoList.add(orderOverVo);
        }
        BaseResult<Object> result = new BaseResult<>();
        result.returnWithValue(code, orderOverVoList);

        return result;
    }

    @Override
    public BaseResult<Object> confirmOrder(ContractKey contractKey, Object[] contractParams) throws ContractInvokeFailException, ValueNullException, PasswordIllegalParam {
        String methodName = CONFIRM_ORDER;
        String[] resultMapKey = new String[]{};
        BaseResult result = new BaseResult();
        try {
            ContractResult contractResult = ContractUtil.invokeContract(contractKey, methodName, contractParams, resultMapKey, "OrderContract");
            LogUtil.debug("调用合约 : OrderContract 方法: confirmOrder 返回结果：" + contractResult.toString());
            Code code = contractResult.getCode();
            result.returnWithoutValue(code);
        } catch (ContractInvokeFailException e) {
            e.printStackTrace();
            throw new ContractInvokeFailException();
        } catch (ValueNullException e) {
            e.printStackTrace();
            throw new ValueNullException();
        } catch (PasswordIllegalParam passwordIllegalParam) {
            passwordIllegalParam.printStackTrace();
            throw new PasswordIllegalParam();
        }
        return result;

    }

}

