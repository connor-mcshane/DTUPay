package dk.dtu.fred.dtupay.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dtu.fred.dtupay.rest.ProcessBankTransactionService;

/**
 * 
 * Handler for business logic of handling transaction
 *
 */

public class TransactionHandler {
    private TransactionRequest transRequest;
    
    /**
     * @param model
     * 			Pass a TransactionRequest Model with valid parameters
     */
    public TransactionHandler(TransactionRequest model) {
        this.transRequest = model;
    }

    private InternalDatabase database = InternalDatabase.getInstance();

    /**
     * {@link org.apache.logging.log4j.core.Logger} for the class.
     */
    private static final Logger logger = LoggerFactory.getLogger(ProcessBankTransactionService.class);
    
    /**
     * 
     * @return message, replies the status of the transaction participants
     */

    public String verifyTransactionParticipants(){
        
        String customerCpr = database.getCprOfBarcode(transRequest.getUuid());

        if (customerCpr == null) {
            logger.info("The barcode is not in the internal database.");
            return "This barcode does not exist";

        } else if (database.getCustomer(customerCpr) == null)
        {
            logger.info("No DTUPay customer has this barcode.");
            return "This barcode does not belong to any of DTUPay users";
            
        } else if (database.getCustomer(transRequest.getMerchantCpr()) == null){
            
            logger.info("The merchant is not a DTUPay user.");
            return "This merchant is not a DTUPay user";
        }
        else return "Transaction participants are valid";
                
    }
    /**
     * generateTransaction generates a JMS message to invoke a bank transaction request to the Process Bank Transaction Bean
     * 
     * @return JMS message to invoke the ProcessBankTransaction bean, 
     * 
     */
    public String generateTransactionJMSRequest() {
        String customerCpr = database.getCprOfBarcode(transRequest.getUuid());
        return database.getCustomer(customerCpr).getBankId() + " " + database.getCustomer(transRequest.getMerchantCpr()).getBankId() + " " 
               + transRequest.getAmount() + " " + transRequest.getComment();
        
    }
    /**
     * completeTransaction 
     * 
     */
    public void completeTransaction() {
        String customerCpr = database.getCprOfBarcode(transRequest.getUuid());
        Transaction transaction = new Transaction(customerCpr, transRequest.getAmount());
        transaction.setID(transRequest.getUuid());
        database.addTransactions(customerCpr, transaction);
        database.addTransactions(transRequest.getMerchantCpr(), transaction);
        database.deleteBarcode(transRequest.getUuid());
        
    }
    
}
