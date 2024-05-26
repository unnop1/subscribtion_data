package com.nt.subscribtion_data.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.Destination;

import org.json.JSONArray;
import org.json.JSONObject;

// import org.springframework.amqp.rabbit.annotation.Exchange;
// import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.subscribtion_data.client.CATMFEClient;
import com.nt.subscribtion_data.model.dao.CATMFE.OfferingSpecData;
import com.nt.subscribtion_data.model.dao.DataModel.TriggerMessageData;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventData;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.CreditLimit;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.EventItem;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.Offer;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.Photo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.SouthernContactAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.TopUp;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.Address;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.DestinationCustomerAccount;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount.BillDeliveryAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount.BillingAccount;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount.BillingAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount.BillingInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount.VatAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationCustomerAccount.BillingAccount.VatDeliveryAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationSubscriberInfo.DestinationSimInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationSubscriberInfo.DestinationSubscriberInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationSubscriberInfo.SourceSimInfo;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.OrderHeaderData;
import com.nt.subscribtion_data.model.dto.ReceiveOMDataType;
import com.nt.subscribtion_data.model.dto.ReceiveTopUpDataType;
import com.nt.subscribtion_data.service.database.CATMFEService;
import com.nt.subscribtion_data.service.database.OMMYFRONTService;

@Service
public class RabbitMqConsumerService {

    @Autowired
    private DistributeService distributeService;

    @Autowired
    private OMMYFRONTService ommyfrontService;

    @Autowired
    private CATMFEService catmfeService;

    @RabbitListener(queues = {"RedsRechargeQ", "RedsOrderQ", "RedsPackageExpireQ"})
    public void receiveMessage(@Payload String message, @Headers Map<String, Object> headers) throws JsonMappingException, JsonProcessingException {
        try{
            String queueName = (String) headers.get("amqp_receivedRoutingKey");
            System.out.println("Received message from queue " + queueName + ": " + message);
            // Process the message based on the queue name
            switch (queueName) {
                case "RedsRechargeQ":
                    processTopUpType(message);
                    break;
                case "RedsOrderQ":
                    processOMType(message);
                    break;
                case "RedsPackageExpireQ":
                    processExpiredType(message);
                    break;
                default:
                    System.out.println("Unknown queue: " + queueName);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processOMType(String message) throws JsonMappingException, JsonProcessingException, SQLException {
        // Process for new_order_type
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveOMDataType receivedData = objectMapper.readValue(message, ReceiveOMDataType.class);
        
        // Mapping DataType
        MappingOMData(receivedData);

        
        TriggerMessageData triggerMsg = new TriggerMessageData();
        distributeService.CreateTriggerMessage(triggerMsg);
    }

    private void processTopUpType(String message) throws JsonMappingException, JsonProcessingException, SQLException {
        // Process for new_order_type
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveOMDataType receivedData = objectMapper.readValue(message, ReceiveOMDataType.class);
        
        // Mapping DataType
        MappingTopUpData(receivedData);

        
        TriggerMessageData triggerMsg = new TriggerMessageData();
        distributeService.CreateTriggerMessage(triggerMsg);
    }

    private void processExpiredType(String message) throws JsonMappingException, JsonProcessingException, SQLException {
        // Process for new_order_type
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveOMDataType receivedData = objectMapper.readValue(message, ReceiveOMDataType.class);
        
        // Mapping DataType
        MappingExpiredData(receivedData);

        
        TriggerMessageData triggerMsg = new TriggerMessageData();
        distributeService.CreateTriggerMessage(triggerMsg);
    }

    private void MappingOMData(ReceiveOMDataType receivedData ){
        System.out.println(receivedData.toString());
        EventData omEv = new EventData();
        // Get orderid from database OMMYFRONT at table order_header
        System.out.println("Get header from database OMMYFRONT");
        OrderHeaderData odheader = ommyfrontService.getOrderHeaderDataByOrderID(receivedData.getOrderId());
        
        System.out.println(odheader.getInputData());
        JSONObject inputData = new JSONObject(odheader.getInputData().toString());
        System.out.println("=================================");
        
        
        if (inputData.optString("orderId") != null){
            omEv.setRefTransId(inputData.getString("orderId"));
        }

        if (inputData.optString("bulkId") != null){
            omEv.setBulkOrderId(inputData.getString("bulkId"));
        }

        if (inputData.optString("channel") != null){
            omEv.setChannel(inputData.getString("channel"));
        }

        if (inputData.optString("highPriorityOrderType") != null){
            omEv.setEventType(inputData.getString("highPriorityOrderType"));
        }

        if (inputData.optString("submitedDate") != null){
            omEv.setSubmitedDate(odheader.getCreateDate());
        }

        if (inputData.optString("completedDate") != null){
            omEv.setCompletedDate(odheader.getUpdateDate());
        }

        if (inputData.optString("isProvisionRequired") != null){
            omEv.setIsProvisionRequired(inputData.getBoolean("isProvisionRequired"));
        }

        if (inputData.optString("rerunRevisionNumber") != null){
            omEv.setRerunRevisionNumber(inputData.getInt("rerunRevisionNumber"));
        }

        if (inputData.optString("orderItem") != null){
            List<EventItem> evenItems = new ArrayList<>();
            JSONArray orderItems = inputData.getJSONArray("orderItem");
            for (int i = 0; i < orderItems.length(); i++){
                JSONObject orderItem = orderItems.getJSONObject(i);
                EventItem evenItem = new EventItem();

                // EventItem
                evenItem.setItemType(orderItem.getString("orderType"));
                
                evenItem.setEffectiveDate(orderItem.getString("orderExecutionDate"));
                
                evenItem.setExecutionType(orderItem.getString("orderExecutionType"));

                evenItem.setSourceEntity(orderItem.getString("sourceEntity"));

                evenItem.setUserRole(orderItem.getString("userRole"));

                // OrderItem
                JSONObject productOffering = orderItem.getJSONObject("productOffering");
                JSONObject subscriberInfo = orderItem.getJSONObject("subscriberInfo");
                
                


                /*  
                 * Offer 
                 */
                List<Offer> offers = new ArrayList<Offer>();
                Offer offer = new Offer();


                String offeringId = productOffering.getString("offeringId");
                OfferingSpecData ofrspec = catmfeService.getOfferingSpecByOfferingId(offeringId);
        
                offer.setOfferingId(offeringId);

                offer.setOfferingType(productOffering.getString("offeringType"));

                offer.setActionFlag(productOffering.getString("actionFlag"));

                offer.setOfferingNameTh(ofrspec.getOfferingnameTH());

                offer.setOfferingNameEn(ofrspec.getOfferingnameEN());

                offer.setPackageId(ofrspec.getPackageID());

                offer.setPackageName(ofrspec.getPackageName());

                offer.setDescriptionTh(ofrspec.getDescTH());

                offer.setDescriptionEn(ofrspec.getDescEN());

                offer.setServiceType(subscriberInfo.getInt("serviceType"));

                offer.setOcsOfferingName(ofrspec.getOcsofferingname());

                offer.setRcAmount(ofrspec.getRcamount());

                offer.setRcVatAmount(ofrspec.getRcvatamount());

                offer.setPeriod(ofrspec.getPeriod());

                offer.setUnitPeriod(ofrspec.getUnitperiod());

                offer.setSaleStartDate(ofrspec.getSalestartdate());

                offer.setSaleEndDate(ofrspec.getSaleenddate());

                offer.setMaxDayAfterActiveDate(ofrspec.getMaxdayafteractivedate());

                offer.setNiceNumberFlag(ofrspec.getNicenumberflag());

                offer.setNiceNumberLevel(ofrspec.getNicenumberlevel());

                offer.setConTractFlag(ofrspec.getContractflag());

                offer.setContractUnitPeriod(ofrspec.getContractunitperiod());

                offer.setCatEmpFlag(ofrspec.getCatempflag());

                offer.setCatRetireEmpFlag(ofrspec.getRetiredcatempflag());

                // offer.multisimFlag your code here with logic

                offer.setTopupSimFlag(ofrspec.getTopupsimflag());

                offer.setTouristSimFlag(ofrspec.getTouristsimflag());

                offer.setChangePoUssdCode(ofrspec.getChangepoussdcode());

                offer.setAddSoUssdCode(ofrspec.getAddsoussdcode());

                offer.setDeleteSoUssdCode(ofrspec.getDeletesoussdcode());

                // offer.frequency your code here with logic

                offer.setCanSwapPoFlag(ofrspec.getCanswappoflag());

                offers.add(offer);

                evenItem.setOffer(offers);
                /*  
                 * End offer
                 */

                /*
                 * Photo
                 */
                List<Photo> photos = new ArrayList<Photo>();
                JSONArray orderPhotos = orderItem.getJSONArray("photo");
                for (int j = 0; j < orderPhotos.length(); j++){
                    JSONObject orderPhoto = orderPhotos.getJSONObject(j);
                    Photo photo = new Photo();
                    photo.setPhotoId(orderPhoto.getString("photoId"));
                    photo.setPhotoType(orderPhoto.getString("photoType"));
                    photo.setDummyPhotoFlag(orderPhoto.getBoolean("dummyPhotoFlag"));
                    photos.add(photo);
                }
                evenItem.setPhoto(photos);









                evenItems.add(evenItem);

                
                
            }
        }


        /*
        * destinationCustomerAccount
        */

        DestinationCustomerAccount destinationCustomerAccount = new DestinationCustomerAccount();
        Address address = new Address();
        BillingAccount billingAccount = new BillingAccount();
        BillingInfo billingInfo = new BillingInfo();
        BillingAddress billingAddress = new BillingAddress();
        BillDeliveryAddress billDeliveryAddress = new BillDeliveryAddress();
        VatAddress vatAddress = new VatAddress();
        VatDeliveryAddress vatDeliveryAddress = new VatDeliveryAddress();
        TopUp topUp = new TopUp();
        CreditLimit creditLimit = new CreditLimit();
        SouthernContactAddress southernContactAddress = new SouthernContactAddress();
        DestinationSubscriberInfo destinationSubscriberInfo = new DestinationSubscriberInfo();

        JSONObject inputSourceCustomerAccount = inputData.getJSONObject("sourceCustomerAccount");
        JSONObject inputSouthernContactAddress = inputData.getJSONObject("southernContactAddress");
        JSONObject inputSourceCustomerAccountAddress = inputSourceCustomerAccount.getJSONObject("address");

        JSONObject sourceCustomerAccountBillingAccount = inputSourceCustomerAccount.getJSONObject("billingAccount");
        JSONObject sourceCustomerAccountBillDeliveryAddress = inputSourceCustomerAccount.getJSONObject("billDeliveryAddress");
        JSONObject sourceCustomerAccountBillingAccountBillingInfo = sourceCustomerAccountBillingAccount.getJSONObject("billingInfo");
        JSONObject sourceCustomerAccountVatAddress = inputSourceCustomerAccount.getJSONObject("vatAddress");
        JSONObject sourceCustomerAccountVatDeliveryAddress = inputSourceCustomerAccount.getJSONObject("vatDeliveryAddress");
        


        // Destination Address
        address.setBuilding(inputSourceCustomerAccountAddress.getString("building"));

        address.setCountry(inputSourceCustomerAccountAddress.getString("country"));

        address.setHouseNumber(inputSourceCustomerAccountAddress.getString("houseNumber"));

        address.setKhetAmphur(inputSourceCustomerAccountAddress.getString("khetAmphur"));

        address.setKwangTambon(inputSourceCustomerAccountAddress.getString("kwangTambon"));

        address.setMoo(inputSourceCustomerAccountAddress.getString("moo"));

        address.setPostCode(inputSourceCustomerAccountAddress.getString("postCode"));

        address.setProvince(inputSourceCustomerAccountAddress.getString("province"));

        address.setRoad(inputSourceCustomerAccountAddress.getString("road"));

        address.setTroksoi(inputSourceCustomerAccountAddress.getString("troksoi"));

        address.setVillage(inputSourceCustomerAccountAddress.getString("village"));

        // billing account
        billingAccount.setExistingFlag(inputSourceCustomerAccount.getBoolean("existingFlag"));

        billingAccount.setBillingAccountId(inputSourceCustomerAccount.getString("billingAccountId")); // must validate

        billingAccount.setPaymentProfile(inputSourceCustomerAccount.getString("paymentProfile")); // must validate


        // billing info
        billingInfo.setCollectionUnit(sourceCustomerAccountBillingAccountBillingInfo.getString("collectionUnit"));

        billingInfo.setVat(sourceCustomerAccountBillingAccountBillingInfo.getString("vat"));

        billingInfo.setBillingPeriod(sourceCustomerAccountBillingAccountBillingInfo.getString("billingPeriod"));

        billingInfo.setBillable(sourceCustomerAccountBillingAccountBillingInfo.getString("billable"));

        billingInfo.setBillingGroup(sourceCustomerAccountBillingAccountBillingInfo.getString("billingGroup"));

        billingInfo.setCollectionTreatment(sourceCustomerAccountBillingAccountBillingInfo.getString("collectionTreatment"));

        billingInfo.setDispatchMethod(sourceCustomerAccountBillingAccountBillingInfo.getString("dispatchMethod"));

        billingInfo.setEmailAddress(sourceCustomerAccountBillingAccountBillingInfo.getString("emailAddress"));

        // billing address
        billingAddress.setBuilding(inputSourceCustomerAccountAddress.getString("building"));

        billingAddress.setCountry(inputSourceCustomerAccountAddress.getString("country"));

        billingAddress.setHouseNumber(inputSourceCustomerAccountAddress.getString("houseNumber"));

        billingAddress.setKhetAmphur(inputSourceCustomerAccountAddress.getString("khetAmphur"));

        billingAddress.setKwangTambon(inputSourceCustomerAccountAddress.getString("kwangTambon"));

        billingAddress.setMoo(inputSourceCustomerAccountAddress.getString("moo"));

        billingAddress.setPostCode(inputSourceCustomerAccountAddress.getString("postCode"));

        billingAddress.setProvince(inputSourceCustomerAccountAddress.getString("province"));

        billingAddress.setRoad(inputSourceCustomerAccountAddress.getString("road"));

        billingAddress.setTroksoi(inputSourceCustomerAccountAddress.getString("troksoi"));

        billingAddress.setVillage(inputSourceCustomerAccountAddress.getString("village"));

        // billing delivery address
        billDeliveryAddress.setBuilding(sourceCustomerAccountBillDeliveryAddress.getString("building"));

        billDeliveryAddress.setCountry(sourceCustomerAccountBillDeliveryAddress.getString("country"));

        billDeliveryAddress.setHouseNumber(sourceCustomerAccountBillDeliveryAddress.getString("houseNumber"));

        billDeliveryAddress.setKhetAmphur(sourceCustomerAccountBillDeliveryAddress.getString("khetAmphur"));

        billDeliveryAddress.setKwangTambon(sourceCustomerAccountBillDeliveryAddress.getString("kwangTambon"));

        billDeliveryAddress.setMoo(sourceCustomerAccountBillDeliveryAddress.getString("moo"));

        billDeliveryAddress.setPostCode(sourceCustomerAccountBillDeliveryAddress.getString("postCode"));

        billDeliveryAddress.setProvince(sourceCustomerAccountBillDeliveryAddress.getString("province"));

        billDeliveryAddress.setRoad(sourceCustomerAccountBillDeliveryAddress.getString("road"));

        billDeliveryAddress.setTroksoi(sourceCustomerAccountBillDeliveryAddress.getString("troksoi"));

        billDeliveryAddress.setVillage(sourceCustomerAccountBillDeliveryAddress.getString("village"));


        // vat address
        vatAddress.setBuilding(sourceCustomerAccountVatAddress.getString("building"));

        vatAddress.setCountry(sourceCustomerAccountVatAddress.getString("country"));

        vatAddress.setHouseNumber(sourceCustomerAccountVatAddress.getString("houseNumber"));

        vatAddress.setKhetAmphur(sourceCustomerAccountVatAddress.getString("khetAmphur"));

        vatAddress.setKwangTambon(sourceCustomerAccountVatAddress.getString("kwangTambon"));

        vatAddress.setMoo(sourceCustomerAccountVatAddress.getString("moo"));

        vatAddress.setPostCode(sourceCustomerAccountVatAddress.getString("postCode"));

        vatAddress.setProvince(sourceCustomerAccountVatAddress.getString("province"));

        vatAddress.setRoad(sourceCustomerAccountVatAddress.getString("road"));

        vatAddress.setTroksoi(sourceCustomerAccountVatAddress.getString("troksoi"));

        vatAddress.setVillage(sourceCustomerAccountVatAddress.getString("village"));

        // vat delivery address
        vatDeliveryAddress.setBuilding(sourceCustomerAccountVatDeliveryAddress.getString("building"));

        vatDeliveryAddress.setCountry(sourceCustomerAccountVatDeliveryAddress.getString("country"));

        vatDeliveryAddress.setHouseNumber(sourceCustomerAccountVatDeliveryAddress.getString("houseNumber"));

        vatDeliveryAddress.setKhetAmphur(sourceCustomerAccountVatDeliveryAddress.getString("khetAmphur"));

        vatDeliveryAddress.setKwangTambon(sourceCustomerAccountVatDeliveryAddress.getString("kwangTambon"));

        vatDeliveryAddress.setMoo(sourceCustomerAccountVatDeliveryAddress.getString("moo"));

        vatDeliveryAddress.setPostCode(sourceCustomerAccountVatDeliveryAddress.getString("postCode"));

        vatDeliveryAddress.setProvince(sourceCustomerAccountVatDeliveryAddress.getString("province"));

        vatDeliveryAddress.setRoad(sourceCustomerAccountVatDeliveryAddress.getString("road"));

        vatDeliveryAddress.setTroksoi(sourceCustomerAccountVatDeliveryAddress.getString("troksoi"));

        vatDeliveryAddress.setVillage(sourceCustomerAccountVatDeliveryAddress.getString("village"));

        // destinationCustomerAccount
        destinationCustomerAccount.setCardNumber(inputSourceCustomerAccount.getString("cardNumber"));
        destinationCustomerAccount.setCardType(inputSourceCustomerAccount.getString("cardType"));
        destinationCustomerAccount.setCatEmployeeFlag(inputSourceCustomerAccount.getString("catEmployeeFlag"));
        destinationCustomerAccount.setCompanyBranchId(inputSourceCustomerAccount.getString("companyBranchId"));
        destinationCustomerAccount.setCompanyName(inputSourceCustomerAccount.getString("companyName"));
        destinationCustomerAccount.setCompanyType(inputSourceCustomerAccount.getString("companyType"));
        destinationCustomerAccount.setContactNumber(inputSourceCustomerAccount.getString("contactNumber"));
        destinationCustomerAccount.setCustAccountId(inputSourceCustomerAccount.getString("custAccountId"));
        destinationCustomerAccount.setCustomerFocus(inputSourceCustomerAccount.getString("customerFocus"));
        destinationCustomerAccount.setCustomerGroup(inputSourceCustomerAccount.getString("customerGroup"));
        destinationCustomerAccount.setCustomerId(inputSourceCustomerAccount.getString("customerId"));
        destinationCustomerAccount.setCustomerInfoType(inputSourceCustomerAccount.getString("customerInfoType"));
        destinationCustomerAccount.setCustomerSegment(inputSourceCustomerAccount.getString("customerSegment"));
        destinationCustomerAccount.setCustomerType(inputSourceCustomerAccount.getString("customerType"));
        destinationCustomerAccount.setDob(inputSourceCustomerAccount.getString("dob"));
        destinationCustomerAccount.setDocumentNumber(inputSourceCustomerAccount.getString("documentNumber"));
        destinationCustomerAccount.setDocumentType(inputSourceCustomerAccount.getString("documentType"));
        destinationCustomerAccount.setEmailAddress(inputSourceCustomerAccount.getString("emailAddress"));
        destinationCustomerAccount.setExistingFlag(inputSourceCustomerAccount.getBoolean("existingFlag"));
        destinationCustomerAccount.setFirstName(inputSourceCustomerAccount.getString("firstName"));
        destinationCustomerAccount.setGender(inputSourceCustomerAccount.getString("gender"));
        destinationCustomerAccount.setIvrLanguage(inputSourceCustomerAccount.getString("ivrLanguage"));
        destinationCustomerAccount.setLastName(inputSourceCustomerAccount.getString("lastName"));
        destinationCustomerAccount.setNationality(inputSourceCustomerAccount.getString("nationality"));
        destinationCustomerAccount.setTaxRegisterNumber(inputSourceCustomerAccount.getString("taxRegisterNumber"));
        destinationCustomerAccount.setTitle(inputSourceCustomerAccount.getString("title"));
        destinationCustomerAccount.setWrittenLanguage(inputSourceCustomerAccount.getString("writtenLanguage"));

        /*
        *  TopUp
        */

        JSONObject inputTopUp = inputData.getJSONObject("topUp");

        topUp.setSerialNumber(inputTopUp.getString("serialNumber"));
        topUp.setTopupType(inputTopUp.getString("topupType"));
        topUp.setRechargeAmount(inputTopUp.getInt("rechargeAmount"));
        topUp.setCurrencyId(inputTopUp.getInt("currencyId"));
        topUp.setChannelId(inputTopUp.getInt("channelId"));

        /*
        *  Credit Limit
        */
        JSONObject inputCreditLimit = inputData.getJSONObject("creditLimit");
        creditLimit.setType(inputCreditLimit.getString("type"));
        creditLimit.setValue(inputCreditLimit.getString("value"));
        creditLimit.setActionType(inputCreditLimit.getString("actionType"));

        /*
        *  SouthernContactAddress
        */
        southernContactAddress.setBuilding(inputSouthernContactAddress.getString("building"));

        southernContactAddress.setCountry(inputSouthernContactAddress.getString("country"));

        southernContactAddress.setHouseNumber(inputSouthernContactAddress.getString("houseNumber"));

        southernContactAddress.setKhetAmphur(inputSouthernContactAddress.getString("khetAmphur"));

        southernContactAddress.setKwangTambon(inputSouthernContactAddress.getString("kwangTambon"));

        southernContactAddress.setMoo(inputSouthernContactAddress.getString("moo"));

        southernContactAddress.setPostCode(inputSouthernContactAddress.getString("postCode"));

        southernContactAddress.setProvince(inputSouthernContactAddress.getString("province"));

        southernContactAddress.setRoad(inputSouthernContactAddress.getString("road"));

        southernContactAddress.setTroksoi(inputSouthernContactAddress.getString("troksoi"));

        southernContactAddress.setVillage(inputSouthernContactAddress.getString("village"));

        /*
        *  destinationSubscriberInfo
        */
        JSONObject inputSubscriberInfo = inputData.getJSONObject("subscriberInfo");
        JSONObject inputSourceSimInfo = inputSubscriberInfo.getJSONObject("sourceSimInfo");
        JSONObject inputDestinationSimInfo = inputSubscriberInfo.getJSONObject("destinationSimInfo");
        
        destinationSubscriberInfo.setMsisdn(inputSubscriberInfo.getString("msisdn"));
        destinationSubscriberInfo.setServiceType(inputSubscriberInfo.getString("serviceType"));

        // Source sim info
        List<SourceSimInfo> sourceSimInfoList = new ArrayList<SourceSimInfo>();
        SourceSimInfo sourceSimInfo = new SourceSimInfo();
        sourceSimInfo.setIccid(inputSourceSimInfo.getString("iccid"));
        sourceSimInfo.setImsi(inputSourceSimInfo.getString("imsi")); // your code here query
        sourceSimInfo.setSimType(inputSourceSimInfo.getString("simType"));
        sourceSimInfo.setFrequency(inputSourceSimInfo.getString("frequency")); // your code here query
        sourceSimInfoList.add(sourceSimInfo);

        destinationSubscriberInfo.setSourceSimInfo(sourceSimInfoList);
        destinationSubscriberInfo.setTouristSimFlag();

        




        
    }

    private void MappingTopUpData(ReceiveOMDataType receivedData ){
        System.out.println(receivedData.toString());
    }

    private void MappingExpiredData(ReceiveOMDataType receivedData ){
        System.out.println(receivedData.toString());
    }
}
