package com.adbest.smsmarketingfront;

import com.adbest.smsmarketingentity.MessageRecord;
import com.adbest.smsmarketingfront.util.EncryptTools;
import com.adbest.smsmarketingfront.util.twilio.param.PreSendMsg;
import com.adbest.smsmarketingfront.util.twilio.TwilioUtil;
import com.twilio.rest.api.v2010.account.Message;
import net.authorize.Environment;
import net.authorize.api.contract.v1.CreateTransactionRequest;
import net.authorize.api.contract.v1.CreateTransactionResponse;
import net.authorize.api.contract.v1.CreditCardType;
import net.authorize.api.contract.v1.MerchantAuthenticationType;
import net.authorize.api.contract.v1.MessageTypeEnum;
import net.authorize.api.contract.v1.PaymentType;
import net.authorize.api.contract.v1.TransactionRequestType;
import net.authorize.api.contract.v1.TransactionResponse;
import net.authorize.api.contract.v1.TransactionTypeEnum;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.base.ApiOperationBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsMarketingFrontApplicationTests {
    
    @Autowired
    TwilioUtil twilioUtil;
    @Autowired
    ResourceBundle res;
    @Autowired
    EncryptTools encryptTools;
    
    @Test
    public void contextLoads() {
//        QContacts qContacts = QContacts.contacts;
//        BooleanBuilder builder = new BooleanBuilder();
//        builder.and(qContacts.customerId.eq(2L));
        PreSendMsg preSendMsg = new PreSendMsg();
        MessageRecord record = new MessageRecord();
        record.setContactsNumber("+12144051403");
        record.setCustomerNumber("+16782758458");
        record.setContent("From twilio -- test message");
        preSendMsg.setRecord(record);
        Message message = twilioUtil.sendMessage(preSendMsg);
        System.out.println(message);
    }
    
    @Test
    public void testBundle() throws UnsupportedEncodingException {
        System.out.println(res.getLocale());
        System.out.println(res.getString("lang-test"));
        System.out.println(new String(res.getString("lang-test").getBytes(StandardCharsets.ISO_8859_1), "GBK"));
    }
    
    
    @Test
    public void testGenerateSendMsgThread() {
//        messageTask.distributeSendMsgJob();
        System.out.println("encrypt: " + encryptTools.encrypt("123123")); // ecc44937642cd28e9491f10756e7df39
    }
    
    @Test
    public void testAuthorizeNet(){
        //Common code to set for all requests
        ApiOperationBase.setEnvironment(Environment.SANDBOX);
    
        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType() ;
        merchantAuthenticationType.setName("");
        merchantAuthenticationType.setTransactionKey("");
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);
    
        // Populate the payment data
        PaymentType paymentType = new PaymentType();
        CreditCardType creditCard = new CreditCardType();
        creditCard.setCardNumber("4242424242424242");
        creditCard.setExpirationDate("0822");
        paymentType.setCreditCard(creditCard);
    
        // Create the payment transaction request
        TransactionRequestType txnRequest = new TransactionRequestType();
        txnRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
        txnRequest.setPayment(paymentType);
        txnRequest.setAmount(new BigDecimal(500.00));
    
        // Make the API Request
        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(txnRequest);
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();
    
    
        CreateTransactionResponse response = controller.getApiResponse();
    
        if (response!=null) {
        
            // If API Response is ok, go ahead and check the transaction response
            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
            
                TransactionResponse result = response.getTransactionResponse();
                if (result.getResponseCode().equals("1")) {
                    System.out.println(result.getResponseCode());
                    System.out.println("Successful Credit Card Transaction");
                    System.out.println(result.getAuthCode());
                    System.out.println(result.getTransId());
                }
                else
                {
                    System.out.println("Failed Transaction"+result.getResponseCode());
                }
            }
            else
            {
                System.out.println("Failed Transaction:  "+response.getMessages().getResultCode());
            }
        }
    }
    
}
