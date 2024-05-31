package com.nt.subscribtion_data.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nt.subscribtion_data.client.CATMFEClient;
import com.nt.subscribtion_data.model.dao.CATMFE.OfferingSpecData;
import com.nt.subscribtion_data.model.dao.DataModel.Data;
import com.nt.subscribtion_data.model.dao.DataModel.TriggerMessageData;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventData;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.SaleInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.BalanceTransferInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.ContractInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.CreditLimit;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.EventItem;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.Expire;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.ExtendExpireInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.Offer;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.Photo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.SouthernContactAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.TopUp;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.VarietyService;
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
import com.nt.subscribtion_data.model.dao.INVUSER.INVMappingData;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMasterData;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.IMSIOfferingConfig;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.OrderHeaderData;
import com.nt.subscribtion_data.model.dao.OMUSER.TransManageContractDTLData;
import com.nt.subscribtion_data.model.dto.ReceiveExpiredDataType;
import com.nt.subscribtion_data.model.dto.ReceiveOMDataType;
import com.nt.subscribtion_data.model.dto.ReceiveTopUpDataType;
import com.nt.subscribtion_data.service.database.CATMFEService;
import com.nt.subscribtion_data.service.database.INVUSERService;
import com.nt.subscribtion_data.service.database.OMMYFRONTService;
import com.nt.subscribtion_data.service.database.OMUSERService;
import com.nt.subscribtion_data.util.DateTime;

@Service
public class RabbitMqConsumerService {

    @Autowired
    private DistributeService distributeService;

    @Autowired
    private OMMYFRONTService ommyfrontService;
    
    @Autowired
    private OMUSERService omuserService;

    @Autowired
    private INVUSERService invuserService;

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

    private Data processOMType(String message) throws JsonMappingException, JsonProcessingException, SQLException {
        // Process for new_order_type
        Data sendData = null;
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            ReceiveOMDataType receivedData = objectMapper.readValue(message, ReceiveOMDataType.class);
            

            // Mapping DataType
            sendData = MappingOMData(receivedData);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
            String jsonString = mapper.writeValueAsString(sendData);

            System.out.println("sendData:"+jsonString);
            TriggerMessageData triggerMsg = new TriggerMessageData();
            /* Not finish */
            triggerMsg.setMESSAGE_IN(message);
            triggerMsg.setDATE_MODEL(message);
            triggerMsg.setIS_STATUS(0);
            triggerMsg.setORDERID("");
            triggerMsg.setOrderType_Name("");
            triggerMsg.setOrderType_id(1L);
            triggerMsg.setPHONENUMBER("");
            triggerMsg.setPUBLISH_CHANNEL("");
            triggerMsg.setRECEIVE_DATE(DateTime.getTimestampNowUTC());
            triggerMsg.setSA_CHANNEL_CONNECT_ID(1L);
            triggerMsg.setSEND_DATE(DateTime.getTimestampNowUTC());
            distributeService.CreateTriggerMessage(triggerMsg);

            distributeService.CreateTriggerMessage(triggerMsg);

            return sendData;
        }catch (Exception e){
            return sendData;
        }
    }

    private Data processTopUpType(String message) throws JsonMappingException, JsonProcessingException, SQLException {
        // Process for new_order_type
        Data sendData = null;
        // try{
            ObjectMapper objectMapper = new ObjectMapper();
            ReceiveTopUpDataType receivedData = objectMapper.readValue(message, ReceiveTopUpDataType.class);
            
            // Mapping DataType
            sendData = MappingTopUpData(receivedData);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
            String jsonString = mapper.writeValueAsString(sendData);

            System.out.println("sendData:"+jsonString);

            
            TriggerMessageData triggerMsg = new TriggerMessageData();
            /* Not finish */
            triggerMsg.setMESSAGE_IN(message);
            triggerMsg.setDATE_MODEL(message);
            triggerMsg.setIS_STATUS(0);
            triggerMsg.setORDERID("");
            triggerMsg.setOrderType_Name("");
            triggerMsg.setOrderType_id(1L);
            triggerMsg.setPHONENUMBER("");
            triggerMsg.setPUBLISH_CHANNEL("");
            triggerMsg.setRECEIVE_DATE(DateTime.getTimestampNowUTC());
            triggerMsg.setSA_CHANNEL_CONNECT_ID(1L);
            triggerMsg.setSEND_DATE(DateTime.getTimestampNowUTC());
            distributeService.CreateTriggerMessage(triggerMsg);

            return sendData;
        // }catch (Exception e){
        //     return sendData;
        // }
    }

    private Data processExpiredType(String message) throws JsonMappingException, JsonProcessingException, SQLException {
        // Process for new_order_type
        Data sendData = null;
        // try{
            ObjectMapper objectMapper = new ObjectMapper();
            ReceiveExpiredDataType receivedData = objectMapper.readValue(message, ReceiveExpiredDataType.class);
            
            // Mapping DataType
            sendData = MappingExpiredData(receivedData);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
            String jsonString = mapper.writeValueAsString(sendData);

            System.out.println("sendData:"+jsonString);

            
            TriggerMessageData triggerMsg = new TriggerMessageData();
            /* Not finish */
            triggerMsg.setMESSAGE_IN(message);
            triggerMsg.setDATE_MODEL(message);
            triggerMsg.setIS_STATUS(0);
            triggerMsg.setORDERID("");
            triggerMsg.setOrderType_Name("");
            triggerMsg.setOrderType_id(1L);
            triggerMsg.setPHONENUMBER("");
            triggerMsg.setPUBLISH_CHANNEL("");
            triggerMsg.setRECEIVE_DATE(DateTime.getTimestampNowUTC());
            triggerMsg.setSA_CHANNEL_CONNECT_ID(1L);
            triggerMsg.setSEND_DATE(DateTime.getTimestampNowUTC());
            distributeService.CreateTriggerMessage(triggerMsg);

            distributeService.CreateTriggerMessage(triggerMsg);

            return sendData;
        // }catch (Exception e){
        //     return sendData;
        // }
    }

    private Data MappingOMData(ReceiveOMDataType receivedData ){
        System.out.println(receivedData.toString());

        String triggerDate = DateTime.getTimeStampNowStr();

        Data sendData = new Data();
        
        EventData omEv = new EventData();
        // Get orderid from database OMMYFRONT at table order_header
        System.out.println("Get header from database OMMYFRONT");
        OrderHeaderData odheader = ommyfrontService.getOrderHeaderDataByOrderID(receivedData.getOrderId());
        
        String externalId = odheader.getMsisdn();
        System.out.println("externalId: "+externalId);
        INVMappingData invMappingData = invuserService.getInvMappingData(externalId);
        System.out.println("inv getImsi: "+invMappingData.getImsi());
        List<IMSIOfferingConfig> imsiOfferConfigList = ommyfrontService.getImsiOfferingConfigList();

        IMSIOfferingConfig imsiConfigData = getImsiConfigByImsi(invMappingData.getImsi(), imsiOfferConfigList);

        // TransManageContractDTLData transmanageData = omuserService.getTransManageContractDTLData("");

        
        // System.out.println(odheader.getInputData());
        JSONObject inputData = new JSONObject(odheader.getInputData().toString());
        System.out.println("=================================");
        
        if (inputData.has("orderId")){
            omEv.setRefTransId(inputData.getString("orderId"));
        }

        if (inputData.has("bulkId")){
            omEv.setBulkOrderId(inputData.getString("bulkId"));
        }

        if (inputData.has("channel")){
            omEv.setChannel(inputData.getString("channel"));
        }

        if (inputData.has("highPriorityOrderType")){
            omEv.setEventType(inputData.getString("highPriorityOrderType"));
        }

        omEv.setSubmitedDate(odheader.getCreateDate());
        omEv.setCompletedDate(odheader.getUpdateDate());

        if (inputData.has("isProvisionRequired")){
            omEv.setIsProvisionRequired(inputData.getBoolean("isProvisionRequired"));
        }

        if (inputData.has("rerunRevisionNumber")){
            omEv.setRerunRevisionNumber(inputData.getInt("rerunRevisionNumber"));
        }

        // eventItem
        if (inputData.has("orderItem")){
            List<EventItem> evenItems = new ArrayList<>();
            JSONArray orderItems = inputData.getJSONArray("orderItem");
            for (int i = 0; i < orderItems.length(); i++){
                JSONObject orderItem = orderItems.getJSONObject(i);
                EventItem evenItem = new EventItem();

                // EventItem
                if (orderItem.has("orderType")){
                    evenItem.setItemType(orderItem.getString("orderType"));
                }
                
                if (orderItem.has("orderExecutionDate")){
                    evenItem.setEffectiveDate(orderItem.getString("orderExecutionDate"));
                }
                
                if (orderItem.has("orderExecutionType")){
                    evenItem.setExecutionType(orderItem.getString("orderExecutionType"));
                }

                if (orderItem.has("sourceEntity")){
                    evenItem.setSourceEntity(orderItem.getString("sourceEntity"));
                }

                if (orderItem.has("userRole")){
                    evenItem.setUserRole(orderItem.getString("userRole"));
                }

                // OrderItem
                JSONArray productOfferingList = new JSONArray();
                if (orderItem.has("productOffering")){
                    productOfferingList = orderItem.getJSONArray("productOffering");
                }

                JSONObject subscriberInfo = new JSONObject();
                if (orderItem.has("subscriberInfo")){
                    subscriberInfo = orderItem.getJSONObject("subscriberInfo");
                }
                
                /*  
                 * Offer 
                 */
                List<Offer> offers = new ArrayList<Offer>();
                Offer offer = new Offer();
                

                List<String> offeringIdList = new ArrayList<>();
                if (productOfferingList.length() > 0){
                    for(int j=0;j<productOfferingList.length();j++){
                        JSONObject productOffering = productOfferingList.getJSONObject(j);
                        if (productOffering.has("offeringId")){
                            String offeringId = productOffering.getString("offeringId");
                            offeringIdList.add(offeringId);
                        }
                    }
                }
                    
                for(int j=0;j<offeringIdList.size();j++){
                    JSONObject productOffering = productOfferingList.getJSONObject(j);
                    String offeringId = offeringIdList.get(j);

                    OfferingSpecData ofrspec = catmfeService.getOfferingSpecByOfferingId(offeringId);

                    offer.setOfferingId(offeringId);

                    if (productOffering != null ){
                        if (productOffering.has("offeringType")){
                            offer.setOfferingType(productOffering.getString("offeringType"));
                        }
                        
                        if (productOffering.has("actionFlag")){
                            offer.setActionFlag(productOffering.getString("actionFlag"));
                        }
                    }
                    
                    if (ofrspec != null){

                        offer.setOfferingNameTh(ofrspec.getOfferingnameTH());

                        offer.setOfferingNameEn(ofrspec.getOfferingnameEN());

                        offer.setPackageId(ofrspec.getPackageID());

                        offer.setPackageName(ofrspec.getPackageName());

                        offer.setDescriptionTh(ofrspec.getDescTH());

                        offer.setDescriptionEn(ofrspec.getDescEN());

                        if (subscriberInfo != null){
                            if (subscriberInfo.has("serviceType")){
                                offer.setServiceType(subscriberInfo.getInt("serviceType"));
                            }
                        }    

                        offer.setOcsOfferingName(ofrspec.getOcsofferingname());

                        BigDecimal rcamount = new BigDecimal(ofrspec.getRcamount());
                        offer.setRcAmount(rcamount);

                        BigDecimal rcvatamount = new BigDecimal(ofrspec.getRcvatamount());
                        offer.setRcVatAmount(rcvatamount);

                        offer.setPeriod(ofrspec.getPeriod());

                        offer.setUnitPeriod(ofrspec.getUnitperiod());

                        offer.setSaleStartDate(ofrspec.getSalestartdate());

                        offer.setSaleEndDate(ofrspec.getSaleenddate());

                        BigDecimal maxdayafteractivedate = new BigDecimal(ofrspec.getMaxdayafteractivedate());
                        offer.setMaxDayAfterActiveDate(maxdayafteractivedate);

                        offer.setNiceNumberFlag(ofrspec.getNicenumberflag());

                        BigDecimal nicenumberlevel = new BigDecimal(ofrspec.getNicenumberlevel());
                        offer.setNiceNumberLevel(nicenumberlevel);

                        offer.setConTractFlag(ofrspec.getContractflag());

                        BigDecimal contractunitperiod = new BigDecimal(ofrspec.getContractunitperiod());
                        offer.setContractUnitPeriod(contractunitperiod);

                        offer.setCatEmpFlag(ofrspec.getCatempflag());

                        offer.setCatRetireEmpFlag(ofrspec.getRetiredcatempflag());

                        // offer.multisimFlag your code here with logic
                        offer.setMultisimFlag(String.valueOf(invMappingData.getMultisimFlag()));

                        offer.setTopupSimFlag(ofrspec.getTopupsimflag());

                        offer.setTouristSimFlag(ofrspec.getTouristsimflag());

                        offer.setChangePoUssdCode(ofrspec.getChangepoussdcode());

                        offer.setAddSoUssdCode(ofrspec.getAddsoussdcode());

                        offer.setDeleteSoUssdCode(ofrspec.getDeletesoussdcode());

                        // offer.frequency your code here with logic
                        String imsiMapping = invMappingData.getImsi();
                        String imsiFrequency = "";
                        for (IMSIOfferingConfig config : imsiOfferConfigList) {
                            String prefix = config.getImsiPrefix();
                            if (imsiMapping.startsWith(prefix)) {
                                imsiMapping = prefix;
                                imsiFrequency = config.getFrequency();
                                break;
                            }
                        }

                        offer.setFrequency(imsiFrequency);

                        offer.setCanSwapPoFlag(ofrspec.getCanswappoflag());

                        offers.add(offer);
                    }
                }

                evenItem.setOffer(offers);
                /*  
                * End offer
                */
                
                

                

                /*
                 * Photo
                 */
                List<Photo> photos = new ArrayList<Photo>();
                if (orderItem.has("photo")){
                    JSONArray orderPhotos = orderItem.getJSONArray("photo");
                    for (int j = 0; j < orderPhotos.length(); j++){
                        JSONObject orderPhoto = orderPhotos.getJSONObject(j);
                        Photo photo = new Photo();
                        if (orderPhoto.has("photoId")){
                            photo.setPhotoId(orderPhoto.getString("photoId"));
                        }

                        if (orderPhoto.has("photoType")){
                            photo.setPhotoType(Integer.valueOf(orderPhoto.getInt("photoType")));
                        }

                        if (orderPhoto.has("dummyPhotoFlag")){
                            photo.setDummyPhotoFlag(orderPhoto.getBoolean("dummyPhotoFlag"));
                        }

                        photos.add(photo);
                    }
                    evenItem.setPhoto(photos);
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

                JSONObject inputSourceCustomerAccount = null;
                JSONObject inputSouthernContactAddress = null;
                JSONObject inputSourceCustomerAccountAddress = null;
                JSONObject sourceCustomerAccountBillingAccount = null;
                JSONObject sourceCustomerAccountBillDeliveryAddress = null;
                JSONObject sourceCustomerAccountBillingAccountBillingInfo = null;
                JSONObject sourceCustomerAccountVatAddress = null;
                JSONObject sourceCustomerAccountVatDeliveryAddress = null;
                
                if (inputData.has("sourceCustomerAccount")){
                    inputSourceCustomerAccount = inputData.getJSONObject("sourceCustomerAccount");

                    if (inputSourceCustomerAccount != null){
                        if (inputSourceCustomerAccount.has("address")){
                            inputSourceCustomerAccountAddress = inputSourceCustomerAccount.getJSONObject("address");
    
                            // DestinationCustomerAccount Address
                            if (inputSourceCustomerAccountAddress.has("building")){
                                address.setBuilding(inputSourceCustomerAccountAddress.getString("building"));
                            }
    
                            if (inputSourceCustomerAccountAddress.has("country")){
                                address.setCountry(inputSourceCustomerAccountAddress.getString("country"));
                            }
                            
                            if (inputSourceCustomerAccountAddress.has("houseNumber")){
                                address.setHouseNumber(inputSourceCustomerAccountAddress.getString("houseNumber"));
                            }
    
                            if (inputSourceCustomerAccountAddress.has("khetAmphur")){
                                address.setKhetAmphur(inputSourceCustomerAccountAddress.getString("khetAmphur"));
                            }

                            if (inputSourceCustomerAccountAddress.has("kwangTambon")){
                                address.setKwangTambon(inputSourceCustomerAccountAddress.getString("kwangTambon"));
                            }
    
                            if (inputSourceCustomerAccountAddress.has("moo")){
                                address.setMoo(inputSourceCustomerAccountAddress.getString("moo"));
                            }
    
                            if (inputSourceCustomerAccountAddress.has("postCode")){
                                address.setPostCode(inputSourceCustomerAccountAddress.getString("postCode"));
                            }
                            
                            if (inputSourceCustomerAccountAddress.has("province")){
                                address.setProvince(inputSourceCustomerAccountAddress.getString("province"));
                            }
    
                            if (inputSourceCustomerAccountAddress.has("road")){
                                address.setRoad(inputSourceCustomerAccountAddress.getString("road"));
                            }
    
                            if (inputSourceCustomerAccountAddress.has("troksoi")){
                                address.setTroksoi(inputSourceCustomerAccountAddress.getString("troksoi"));
                            }
                            
                            if (inputSourceCustomerAccountAddress.has("village")){
                                address.setVillage(inputSourceCustomerAccountAddress.getString("village"));
                            }

                            // Billing Account
                            if (inputSourceCustomerAccount.has("billingAccount")){

                                sourceCustomerAccountBillingAccount = inputSourceCustomerAccount.getJSONObject("billingAccount");

                                if (sourceCustomerAccountBillingAccount != null){
                                    if (inputSourceCustomerAccount.has("existingFlag")){
                                        billingAccount.setExistingFlag(inputSourceCustomerAccount.getBoolean("existingFlag"));
                                    }

                                    if (inputSourceCustomerAccount.has("billingAccountId")){
                                        billingAccount.setBillingAccountId(inputSourceCustomerAccount.getString("billingAccountId"));
                                    }

                                    if (inputSourceCustomerAccount.has("paymentProfile")){
                                        billingAccount.setPaymentProfile(inputSourceCustomerAccount.getString("paymentProfile"));
                                    }

                                    
                                    // Billing info
                                    if (sourceCustomerAccountBillingAccount.has("billingInfo")){
                                        sourceCustomerAccountBillingAccountBillingInfo = sourceCustomerAccountBillingAccount.getJSONObject("billingInfo");
                
                                        
                                        if (sourceCustomerAccountBillingAccountBillingInfo.has("collectionUnit")){
                                            billingInfo.setCollectionUnit(sourceCustomerAccountBillingAccountBillingInfo.getString("collectionUnit"));
                                        }
                
                                        if (sourceCustomerAccountBillingAccountBillingInfo.has("vat")){
                                            billingInfo.setVat(sourceCustomerAccountBillingAccountBillingInfo.getInt("vat"));
                                        }
                
                                        if (sourceCustomerAccountBillingAccountBillingInfo.has("billingPeriod")){
                                            billingInfo.setBillingPeriod(sourceCustomerAccountBillingAccountBillingInfo.getString("billingPeriod"));
                                        }
                
                                        if (sourceCustomerAccountBillingAccountBillingInfo.has("billable")){
                                            billingInfo.setBillable(sourceCustomerAccountBillingAccountBillingInfo.getInt("billable"));
                                        }
                
                                        if (sourceCustomerAccountBillingAccountBillingInfo.has("billingGroup")){
                                            billingInfo.setBillingGroup(sourceCustomerAccountBillingAccountBillingInfo.getString("billingGroup"));
                                        }
                
                                        if (sourceCustomerAccountBillingAccountBillingInfo.has("collectionTreatment")){
                                            billingInfo.setCollectionTreatment(sourceCustomerAccountBillingAccountBillingInfo.getInt("collectionTreatment"));
                                        }
                
                                        if (sourceCustomerAccountBillingAccountBillingInfo.has("dispatchMethod")){
                                            billingInfo.setDispatchMethod(sourceCustomerAccountBillingAccountBillingInfo.getInt("dispatchMethod"));
                                        }
                
                                        if (sourceCustomerAccountBillingAccountBillingInfo.has("emailAddress")){
                                            billingInfo.setEmailAddress(sourceCustomerAccountBillingAccountBillingInfo.getString("emailAddress"));
                                        }
                                    }

                                    // billing address
                                    if (inputSourceCustomerAccountAddress.has("building")){
                                        billingAddress.setBuilding(inputSourceCustomerAccountAddress.getString("building"));
                                    }
                                    
                                    if (inputSourceCustomerAccountAddress.has("country")){
                                        billingAddress.setCountry(inputSourceCustomerAccountAddress.getString("country"));
                                    }

                                    if (inputSourceCustomerAccountAddress.has("houseNumber")){
                                        billingAddress.setHouseNumber(inputSourceCustomerAccountAddress.getString("houseNumber"));
                                    }

                                    if (inputSourceCustomerAccountAddress.has("khetAmphur")){
                                        billingAddress.setKhetAmphur(inputSourceCustomerAccountAddress.getString("khetAmphur"));
                                    }

                                    if (inputSourceCustomerAccountAddress.has("kwangTambon")){
                                        billingAddress.setKwangTambon(inputSourceCustomerAccountAddress.getString("kwangTambon"));
                                    }

                                    if (inputSourceCustomerAccountAddress.has("moo")){
                                        billingAddress.setMoo(inputSourceCustomerAccountAddress.getString("moo"));
                                    }
            
                                    if (inputSourceCustomerAccountAddress.has("postCode")){
                                        billingAddress.setPostCode(inputSourceCustomerAccountAddress.getString("postCode"));
                                    }
                                    
                                    if (inputSourceCustomerAccountAddress.has("province")){
                                        billingAddress.setProvince(inputSourceCustomerAccountAddress.getString("province"));
                                    }
                                    
                                    if (inputSourceCustomerAccountAddress.has("road")){
                                        billingAddress.setRoad(inputSourceCustomerAccountAddress.getString("road"));
                                    }
                                    
                                    if (inputSourceCustomerAccountAddress.has("troksoi")){
                                        billingAddress.setTroksoi(inputSourceCustomerAccountAddress.getString("troksoi"));
                                    }
            
                                    if (inputSourceCustomerAccountAddress.has("village")){
                                        billingAddress.setVillage(inputSourceCustomerAccountAddress.getString("village"));
                                    }
                                }
                            }

    
                        }
    
                        
                        
                        // billing delivery address
                        if (inputSourceCustomerAccount.has("billDeliveryAddress")){
                            sourceCustomerAccountBillDeliveryAddress = inputSourceCustomerAccount.getJSONObject("billDeliveryAddress");

                            
                            if (sourceCustomerAccountBillDeliveryAddress.has("building")){
                                billDeliveryAddress.setBuilding(sourceCustomerAccountBillDeliveryAddress.getString("building"));
                            }else{
                                billDeliveryAddress.setBuilding(address.getBuilding());
                            }

                            if (sourceCustomerAccountBillDeliveryAddress.has("country")){
                                billDeliveryAddress.setCountry(sourceCustomerAccountBillDeliveryAddress.getString("country"));
                            }else{
                                billDeliveryAddress.setCountry(address.getCountry());
                            }

                            if (sourceCustomerAccountBillDeliveryAddress.has("houseNumber")){
                                billDeliveryAddress.setHouseNumber(sourceCustomerAccountBillDeliveryAddress.getString("houseNumber"));
                            }else{
                                billDeliveryAddress.setHouseNumber(address.getHouseNumber());
                            }

                            if (sourceCustomerAccountBillDeliveryAddress.has("khetAmphur")){
                                billDeliveryAddress.setKhetAmphur(sourceCustomerAccountBillDeliveryAddress.getString("khetAmphur"));
                            }else{
                                billDeliveryAddress.setKhetAmphur(address.getKhetAmphur());
                            }

                            if (sourceCustomerAccountBillDeliveryAddress.has("kwangTambon")){
                                billDeliveryAddress.setKwangTambon(sourceCustomerAccountBillDeliveryAddress.getString("kwangTambon"));
                            }else{
                                billDeliveryAddress.setKwangTambon(address.getKwangTambon());
                            }

                            if (sourceCustomerAccountBillDeliveryAddress.has("moo")){
                                billDeliveryAddress.setMoo(sourceCustomerAccountBillDeliveryAddress.getString("moo"));
                            }else{
                                billDeliveryAddress.setMoo(address.getMoo());
                            }

                            if (sourceCustomerAccountBillDeliveryAddress.has("postCode")){
                                billDeliveryAddress.setPostCode(sourceCustomerAccountBillDeliveryAddress.getString("postCode"));
                            }else{
                                billDeliveryAddress.setPostCode(address.getPostCode());
                            }

                            if (sourceCustomerAccountBillDeliveryAddress.has("province")){
                                billDeliveryAddress.setProvince(sourceCustomerAccountBillDeliveryAddress.getString("province"));
                            }else{
                                billDeliveryAddress.setProvince(address.getProvince());
                            }

                            if (sourceCustomerAccountBillDeliveryAddress.has("road")){
                                billDeliveryAddress.setRoad(sourceCustomerAccountBillDeliveryAddress.getString("road"));
                            }else{
                                billDeliveryAddress.setRoad(address.getRoad());
                            }

                            if (sourceCustomerAccountBillDeliveryAddress.has("troksoi")){
                                billDeliveryAddress.setTroksoi(sourceCustomerAccountBillDeliveryAddress.getString("troksoi"));
                            }else{
                                billDeliveryAddress.setTroksoi(address.getTroksoi());
                            }

                            if (sourceCustomerAccountBillDeliveryAddress.has("village")){
                                billDeliveryAddress.setVillage(sourceCustomerAccountBillDeliveryAddress.getString("village"));
                            }else{
                                billDeliveryAddress.setVillage(address.getVillage());
                            }
                        }

                        // vat address
                        if (inputSourceCustomerAccount.has("vatAddress")){
                            sourceCustomerAccountVatAddress = inputSourceCustomerAccount.getJSONObject("vatAddress");

                            
                            if (sourceCustomerAccountVatAddress.has("building")){
                                vatAddress.setBuilding(sourceCustomerAccountVatAddress.getString("building"));
                            }else{
                                vatAddress.setBuilding(address.getBuilding());
                            }

                            if (sourceCustomerAccountVatAddress.has("country")){
                                vatAddress.setCountry(sourceCustomerAccountVatAddress.getString("country"));
                            }else{
                                vatAddress.setCountry(address.getCountry());
                            }

                            if (sourceCustomerAccountVatAddress.has("houseNumber")){
                                vatAddress.setHouseNumber(sourceCustomerAccountVatAddress.getString("houseNumber"));
                            }else{
                                vatAddress.setHouseNumber(address.getHouseNumber());
                            }

                            if (sourceCustomerAccountVatAddress.has("khetAmphur")){
                                vatAddress.setKhetAmphur(sourceCustomerAccountVatAddress.getString("khetAmphur"));
                            }else{
                                vatAddress.setKhetAmphur(address.getKhetAmphur());
                            }

                            if (sourceCustomerAccountVatAddress.has("kwangTambon")){
                                vatAddress.setKwangTambon(sourceCustomerAccountVatAddress.getString("kwangTambon"));
                            }else{
                                vatAddress.setKwangTambon(address.getKwangTambon());
                            }

                            if (sourceCustomerAccountVatAddress.has("moo")){
                                vatAddress.setMoo(sourceCustomerAccountVatAddress.getString("moo"));
                            }else{
                                vatAddress.setMoo(address.getMoo());
                            }

                            if (sourceCustomerAccountVatAddress.has("postCode")){
                                vatAddress.setPostCode(sourceCustomerAccountVatAddress.getString("postCode"));
                            }else{
                                vatAddress.setPostCode(address.getPostCode());
                            }

                            if (sourceCustomerAccountVatAddress.has("province")){
                                vatAddress.setProvince(sourceCustomerAccountVatAddress.getString("province"));
                            }else{
                                vatAddress.setProvince(address.getProvince());
                            }

                            if (sourceCustomerAccountVatAddress.has("road")){
                                vatAddress.setRoad(sourceCustomerAccountVatAddress.getString("road"));
                            }else{
                                vatAddress.setRoad(address.getRoad());
                            }

                            if (sourceCustomerAccountVatAddress.has("troksoi")){
                                vatAddress.setTroksoi(sourceCustomerAccountVatAddress.getString("troksoi"));
                            }else{
                                vatAddress.setTroksoi(address.getTroksoi());
                            }

                            if (sourceCustomerAccountVatAddress.has("village")){
                                vatAddress.setVillage(sourceCustomerAccountVatAddress.getString("village"));
                            }else{
                                vatAddress.setVillage(address.getVillage());
                            }
                        }

                        // vat delivery address
                        if (inputSourceCustomerAccount.has("vatDeliveryAddress")){
                            sourceCustomerAccountVatDeliveryAddress = inputSourceCustomerAccount.getJSONObject("vatDeliveryAddress");

                            
                            if (sourceCustomerAccountVatDeliveryAddress.has("building")){
                                vatDeliveryAddress.setBuilding(sourceCustomerAccountVatDeliveryAddress.getString("building"));
                            }else{
                                vatDeliveryAddress.setBuilding(address.getBuilding());
                            }

                            if (sourceCustomerAccountVatDeliveryAddress.has("country")){
                                vatDeliveryAddress.setCountry(sourceCustomerAccountVatDeliveryAddress.getString("country"));
                            }else{
                                vatDeliveryAddress.setCountry(address.getCountry());
                            }

                            if (sourceCustomerAccountVatDeliveryAddress.has("houseNumber")){
                                vatDeliveryAddress.setHouseNumber(sourceCustomerAccountVatDeliveryAddress.getString("houseNumber"));
                            }else{
                                vatDeliveryAddress.setHouseNumber(address.getHouseNumber());
                            }

                            if (sourceCustomerAccountVatDeliveryAddress.has("khetAmphur")){
                                vatDeliveryAddress.setKhetAmphur(sourceCustomerAccountVatDeliveryAddress.getString("khetAmphur"));
                            }else{
                                vatDeliveryAddress.setKhetAmphur(address.getKhetAmphur());
                            }

                            if (sourceCustomerAccountVatDeliveryAddress.has("kwangTambon")){
                                vatDeliveryAddress.setKwangTambon(sourceCustomerAccountVatDeliveryAddress.getString("kwangTambon"));
                            }else{
                                vatDeliveryAddress.setKwangTambon(address.getKwangTambon());
                            }

                            if (sourceCustomerAccountVatDeliveryAddress.has("moo")){
                                vatDeliveryAddress.setMoo(sourceCustomerAccountVatDeliveryAddress.getString("moo"));
                            }else{
                                vatDeliveryAddress.setMoo(address.getMoo());
                            }

                            if (sourceCustomerAccountVatDeliveryAddress.has("postCode")){
                                vatDeliveryAddress.setPostCode(sourceCustomerAccountVatDeliveryAddress.getString("postCode"));
                            }else{
                                vatDeliveryAddress.setPostCode(address.getPostCode());
                            }

                            if (sourceCustomerAccountVatDeliveryAddress.has("province")){
                                vatDeliveryAddress.setProvince(sourceCustomerAccountVatDeliveryAddress.getString("province"));
                            }else{
                                vatDeliveryAddress.setProvince(address.getProvince());
                            }

                            if (sourceCustomerAccountVatDeliveryAddress.has("road")){
                                vatDeliveryAddress.setRoad(sourceCustomerAccountVatDeliveryAddress.getString("road"));
                            }else{
                                vatDeliveryAddress.setRoad(address.getRoad());
                            }

                            if (sourceCustomerAccountVatDeliveryAddress.has("troksoi")){
                                vatDeliveryAddress.setTroksoi(sourceCustomerAccountVatDeliveryAddress.getString("troksoi"));
                            }else{
                                vatDeliveryAddress.setTroksoi(address.getTroksoi());
                            }

                            if (sourceCustomerAccountVatDeliveryAddress.has("village")){
                                vatDeliveryAddress.setVillage(sourceCustomerAccountVatDeliveryAddress.getString("village"));
                            }else{
                                vatDeliveryAddress.setVillage(address.getVillage());
                            }
                        }
    
                        // billing account
                        if (inputSourceCustomerAccount.has("existingFlag")){
                            billingAccount.setExistingFlag(inputSourceCustomerAccount.getBoolean("existingFlag"));
                        }
    
                        if (inputSourceCustomerAccount.has("billingAccountId")){
                            billingAccount.setBillingAccountId(inputSourceCustomerAccount.getString("billingAccountId")); // must validate
                        }
    
                        if (inputSourceCustomerAccount.has("paymentProfile")){
                            billingAccount.setPaymentProfile(inputSourceCustomerAccount.getString("paymentProfile")); // must validate
                        }
                        
                        billingAccount.setBillingInfo(billingInfo);
                        billingAccount.setBillingAddress(billingAddress);
                        billingAccount.setBillDeliveryAddress(billDeliveryAddress);
                        billingAccount.setVatAddress(vatAddress);
                        billingAccount.setVatDeliveryAddress(vatDeliveryAddress);
    
    
                    }
                    
                }

                

                // destinationCustomerAccount
                if (inputSourceCustomerAccount != null){

                    if (inputSourceCustomerAccount.has("cardNumber")){
                        destinationCustomerAccount.setCardNumber(inputSourceCustomerAccount.getString("cardNumber"));
                    }

                    if (inputSourceCustomerAccount.has("cardType")){
                        destinationCustomerAccount.setCardType(inputSourceCustomerAccount.getInt("cardType"));
                    }

                    if (inputSourceCustomerAccount.has("catEmployeeFlag")){
                        destinationCustomerAccount.setCatEmployeeFlag(inputSourceCustomerAccount.getInt("catEmployeeFlag"));
                    }

                    if (inputSourceCustomerAccount.has("companyBranchId")){
                        destinationCustomerAccount.setCompanyBranchId(inputSourceCustomerAccount.getString("companyBranchId"));
                    }

                    if (inputSourceCustomerAccount.has("companyName")){
                        destinationCustomerAccount.setCompanyName(inputSourceCustomerAccount.getString("companyName"));
                    }

                    if (inputSourceCustomerAccount.has("companyType")){
                        destinationCustomerAccount.setCompanyType(inputSourceCustomerAccount.getInt("companyType"));
                    }

                    if (inputSourceCustomerAccount.has("contactNumber")){
                        destinationCustomerAccount.setContactNumber(inputSourceCustomerAccount.getString("contactNumber"));
                    }

                    if (inputSourceCustomerAccount.has("custAccountId")){
                        destinationCustomerAccount.setCustAccountId(inputSourceCustomerAccount.getString("custAccountId"));
                    }

                    if (inputSourceCustomerAccount.has("customerFocus")){
                        destinationCustomerAccount.setCustomerFocus(inputSourceCustomerAccount.getString("customerFocus"));
                    }

                    if (inputSourceCustomerAccount.has("customerGroup")){
                        destinationCustomerAccount.setCustomerGroup(inputSourceCustomerAccount.getInt("customerGroup"));
                    }

                    if (inputSourceCustomerAccount.has("customerId")){
                        destinationCustomerAccount.setCustomerId(inputSourceCustomerAccount.getString("customerId"));
                    }

                    if (inputSourceCustomerAccount.has("customerInfoType")){
                        destinationCustomerAccount.setCustomerInfoType(inputSourceCustomerAccount.getInt("customerInfoType"));
                    }

                    if (inputSourceCustomerAccount.has("customerSegment")){
                        destinationCustomerAccount.setCustomerSegment(inputSourceCustomerAccount.getInt("customerSegment"));
                    }

                    if (inputSourceCustomerAccount.has("customerType")){
                        destinationCustomerAccount.setCustomerType(inputSourceCustomerAccount.getInt("customerType"));
                    }

                    if (inputSourceCustomerAccount.has("dob")){
                        destinationCustomerAccount.setDob(inputSourceCustomerAccount.getString("dob"));
                    }

                    if (inputSourceCustomerAccount.has("documentNumber")){
                        destinationCustomerAccount.setDocumentNumber(inputSourceCustomerAccount.getString("documentNumber"));
                    }

                    if (inputSourceCustomerAccount.has("documentType")){
                        destinationCustomerAccount.setDocumentType(inputSourceCustomerAccount.getInt("documentType"));
                    }

                    if (inputSourceCustomerAccount.has("emailAddress")){
                        destinationCustomerAccount.setEmailAddress(inputSourceCustomerAccount.getString("emailAddress"));
                    }

                    if (inputSourceCustomerAccount.has("existingFlag")){
                        destinationCustomerAccount.setExistingFlag(inputSourceCustomerAccount.getBoolean("existingFlag"));
                    }

                    if (inputSourceCustomerAccount.has("firstName")){
                        destinationCustomerAccount.setFirstName(inputSourceCustomerAccount.getString("firstName"));
                    }

                    if (inputSourceCustomerAccount.has("gender")){
                        destinationCustomerAccount.setGender(inputSourceCustomerAccount.getInt("gender"));
                    }

                    if (inputSourceCustomerAccount.has("ivrLanguage")){
                        destinationCustomerAccount.setIvrLanguage(inputSourceCustomerAccount.getString("ivrLanguage"));
                    }

                    if (inputSourceCustomerAccount.has("lastName")){
                        destinationCustomerAccount.setLastName(inputSourceCustomerAccount.getString("lastName"));
                    }

                    if (inputSourceCustomerAccount.has("nationality")){
                        destinationCustomerAccount.setNationality(inputSourceCustomerAccount.getString("nationality"));
                    }

                    if (inputSourceCustomerAccount.has("taxRegisterNumber")){
                        destinationCustomerAccount.setTaxRegisterNumber(inputSourceCustomerAccount.getString("taxRegisterNumber"));
                    }

                    if (inputSourceCustomerAccount.has("title")){
                        destinationCustomerAccount.setTitle(inputSourceCustomerAccount.getInt("title"));
                    }

                    if (inputSourceCustomerAccount.has("writtenLanguage")){
                        destinationCustomerAccount.setWrittenLanguage(inputSourceCustomerAccount.getString("writtenLanguage"));
                    }
                }

                destinationCustomerAccount.setAddress(address);
                destinationCustomerAccount.setBillingAccount(billingAccount);
                evenItem.setDestinationCustomerAccount(destinationCustomerAccount);



                /*
                *  TopUp
                */
                if (inputData.has("topUp")){
                    JSONObject inputTopUp = inputData.getJSONObject("topUp");

                    if (inputData.has("serialNumber")){
                        topUp.setSerialNumber(inputTopUp.getString("serialNumber"));
                    }

                    if (inputData.has("topupType")){
                        topUp.setTopupType(inputTopUp.getInt("topupType"));
                    }

                    if (inputData.has("rechargeAmount")){
                        topUp.setRechargeAmount(inputTopUp.getString("rechargeAmount"));
                    }

                    if (inputData.has("currencyId")){
                        topUp.setCurrencyId(inputTopUp.getInt("currencyId"));
                    }

                    if (inputData.has("channelId")){
                        topUp.setChannelId(inputTopUp.getInt("channelId"));
                    }
                    evenItem.setTopUp(topUp);
                }

                /*
                *  Credit Limit
                */
                JSONObject inputCreditLimit = new JSONObject();
                if (inputData.has("creditLimit")){
                    inputCreditLimit = inputData.getJSONObject("creditLimit");

                    if (inputCreditLimit.has("type")){
                        creditLimit.setType(inputCreditLimit.getString("type"));
                    }

                    if (inputCreditLimit.has("value")){
                        creditLimit.setValue(inputCreditLimit.getString("value"));
                    }

                    if (inputCreditLimit.has("actionType")){
                        creditLimit.setActionType(inputCreditLimit.getString("actionType"));
                    }
                    evenItem.setCreditLimit(creditLimit);
                }

                if (inputData.has("southernContactAddress")){
                    inputSouthernContactAddress = inputData.getJSONObject("southernContactAddress");

                    /*
                    *  SouthernContactAddress
                    */
                    if (inputSouthernContactAddress.has("building")){
                        southernContactAddress.setBuilding(inputSouthernContactAddress.getString("building"));
                    }

                    if (inputSouthernContactAddress.has("country")){
                        southernContactAddress.setCountry(inputSouthernContactAddress.getString("country"));
                    }
                    
                    if (inputSouthernContactAddress.has("houseNumber")){
                        southernContactAddress.setHouseNumber(inputSouthernContactAddress.getString("houseNumber"));
                    }

                    if (inputSouthernContactAddress.has("khetAmphur")){
                        southernContactAddress.setKhetAmphur(inputSouthernContactAddress.getString("khetAmphur"));
                    }

                    if (inputSouthernContactAddress.has("kwangTambon")){
                        southernContactAddress.setKwangTambon(inputSouthernContactAddress.getString("kwangTambon"));
                    }

                    if (inputSouthernContactAddress.has("moo")){
                        southernContactAddress.setMoo(inputSouthernContactAddress.getString("moo"));
                    }

                    if (inputSouthernContactAddress.has("postCode")){
                        southernContactAddress.setPostCode(inputSouthernContactAddress.getString("postCode"));
                    }

                    if (inputSouthernContactAddress.has("province")){
                        southernContactAddress.setProvince(inputSouthernContactAddress.getString("province"));
                    }

                    if (inputSouthernContactAddress.has("road")){
                        southernContactAddress.setRoad(inputSouthernContactAddress.getString("road"));
                    }

                    if (inputSouthernContactAddress.has("troksoi")){
                        southernContactAddress.setTroksoi(inputSouthernContactAddress.getString("troksoi"));
                    }

                    if (inputSouthernContactAddress.has("village")){
                        southernContactAddress.setVillage(inputSouthernContactAddress.getString("village"));
                    }
                    evenItem.setSouthernContactAddress(southernContactAddress);
                }
                

                

                /*
                *  destinationSubscriberInfo
                */
                JSONObject inputSubscriberInfo = new JSONObject();
                if (inputData.has("subscriberInfo")){
                    inputSubscriberInfo = inputData.getJSONObject("subscriberInfo");

                    if (inputSubscriberInfo.has("msisdn")){
                        destinationSubscriberInfo.setMsisdn(inputSubscriberInfo.getString("msisdn"));
                    }

                    if (inputSubscriberInfo.has("serviceType")){
                        destinationSubscriberInfo.setServiceType(inputSubscriberInfo.getString("serviceType"));
                    }

                    JSONObject inputSourceSimInfo = new JSONObject();
                    if (inputData.has("sourceSimInfo")){
                        inputSourceSimInfo = inputSubscriberInfo.getJSONObject("sourceSimInfo");

                        // Source sim info
                        List<SourceSimInfo> sourceSimInfoList = new ArrayList<SourceSimInfo>();
                        SourceSimInfo sourceSimInfo = new SourceSimInfo();
                        sourceSimInfo.setIccid(inputSourceSimInfo.getString("iccid"));
                        sourceSimInfo.setImsi(imsiConfigData.getImsiPrefix()); // query imsi prefix
                        sourceSimInfo.setSimType(inputSourceSimInfo.getString("simType"));
                        sourceSimInfo.setFrequency(imsiConfigData.getFrequency()); // query frequency
                        sourceSimInfoList.add(sourceSimInfo);

                        JSONObject inputDestinationSimInfo = new JSONObject();
                        if (inputData.has("destinationSimInfo")){
                            inputDestinationSimInfo = inputSubscriberInfo.getJSONObject("destinationSimInfo");
                            
                            // Source sim info
                            List<DestinationSimInfo> destinationSimInfoList = new ArrayList<DestinationSimInfo>();
                            DestinationSimInfo destinationSimInfo = new DestinationSimInfo();
                            
                            if (inputDestinationSimInfo.has("iccid")){
                                String iccid = inputDestinationSimInfo.getString("iccid");
                                destinationSimInfo.setIccid(iccid);

                                OrderHeaderData destinationOdheader = ommyfrontService.getOrderHeaderDataByICCID(iccid);
                                if (destinationOdheader != null){
                                    destinationSimInfo.setImsi(destinationOdheader.getImsi()); // search from iccid
                                }
                            }                            
                            destinationSimInfo.setSimType(null); // Fix null

                            destinationSimInfo.setFrequency(null); // Fix null
                            destinationSimInfoList.add(destinationSimInfo);

                            if (inputDestinationSimInfo.has("itouristSimFlag")){
                                destinationSubscriberInfo.setTouristSimFlag(inputDestinationSimInfo.getString("itouristSimFlag"));
                            }

                            if (inputDestinationSimInfo.has("subscriberNumber")){
                                destinationSubscriberInfo.setSubscriberNumber(inputDestinationSimInfo.getString("subscriberNumber"));
                            }

                            destinationSubscriberInfo.setDestinationSimInfo(destinationSimInfoList);
                        }
                        destinationSubscriberInfo.setSourceSimInfo(sourceSimInfoList);
                    }
                    evenItem.setDestinationSubscriberInfo(destinationSubscriberInfo);
                    
                }
                


                // Varieties service
                List<VarietyService> varietyServices = new ArrayList<VarietyService>();
                VarietyService varietyService = new VarietyService();
                if (orderItem.has("varietyServices")){
                    varietyService.setVarietyType(orderItem.getString("varietyServices"));
                }

                if (orderItem.has("enabledFlag")){
                    varietyService.setEnabledFlag(orderItem.getString("enabledFlag"));
                }
                varietyServices.add(varietyService);
                evenItem.setVarietyServices(varietyServices);


                // Balance transfer info
                JSONObject orderItemBalanceTransferInfo = new JSONObject();
                if (orderItem.has("balanceTransferInfo")){
                    orderItemBalanceTransferInfo = orderItem.getJSONObject("balanceTransferInfo");
                
                    BalanceTransferInfo balanceTransferInfo = new BalanceTransferInfo();
                    if (orderItemBalanceTransferInfo.has("transferTotalFlag")){
                        balanceTransferInfo.setTransferTotalFlag(orderItemBalanceTransferInfo.getString("transferTotalFlag"));
                    }

                    if (orderItemBalanceTransferInfo.has("transferType")){
                        balanceTransferInfo.setTransferType(orderItemBalanceTransferInfo.getString("transferType"));
                    }

                    if (orderItemBalanceTransferInfo.has("transferAmount")){
                        balanceTransferInfo.setTransferAmount(orderItemBalanceTransferInfo.getString("transferAmount"));
                    }
                    evenItem.setBalanceTransferInfo(balanceTransferInfo);
                }

                // ExtendExpireInfo
                JSONObject orderItemExtendExpireInfo = new JSONObject();
                if (orderItem.has("extendExpireInfo")){
                    orderItemExtendExpireInfo = orderItem.getJSONObject("extendExpireInfo");
                    ExtendExpireInfo extendExpireInfo = new ExtendExpireInfo();

                    if (orderItemExtendExpireInfo.has("extendedDay")){
                        extendExpireInfo.setBalanceAmount(orderItemExtendExpireInfo.getString("extendedDay"));
                    }

                    if (orderItemExtendExpireInfo.has("transAmount")){
                        extendExpireInfo.setExtendedDay(orderItemExtendExpireInfo.getString("transAmount"));
                    }
                    evenItem.setExtendExpireInfo(extendExpireInfo);
                }

                // contractInfo 
                /* Not Query ???? Unknown field for query */
                TransManageContractDTLData tMCDTLData = new TransManageContractDTLData();
                ContractInfo contractInfo = new ContractInfo();
                contractInfo.setSubscrNo(tMCDTLData.getSubscrNo());
                contractInfo.setContractId(tMCDTLData.getContractId());
                contractInfo.setRefDocumentId(tMCDTLData.getRefDocumentId());
                contractInfo.setContractCode(tMCDTLData.getContractCode());
                contractInfo.setRefDocumentId(tMCDTLData.getRefDocumentId());
                contractInfo.setContractCode(tMCDTLData.getContractCode());
                contractInfo.setContractType(tMCDTLData.getContractType());
                contractInfo.setContractDesc(tMCDTLData.getContractDesc());
                contractInfo.setContractMonth(tMCDTLData.getContractMonth());
                contractInfo.setContractStart(tMCDTLData.getContractStart());
                contractInfo.setContractEnd(tMCDTLData.getContractEnd());
                contractInfo.setContractValue(tMCDTLData.getContractValue());
                contractInfo.setBypassBy(tMCDTLData.getBypassBy());
                contractInfo.setBypassApproveBy(tMCDTLData.getBypassApproveBy());
                contractInfo.setBypassFee(tMCDTLData.getBypassFee());
                contractInfo.setBypassReason(tMCDTLData.getBypassReason());
                contractInfo.setBypassDate(tMCDTLData.getBypassDate());
                contractInfo.setRequestBypassDate(tMCDTLData.getRequestBypassDate());
                contractInfo.setApproveBypassDate(tMCDTLData.getApproveBypassDate());
                contractInfo.setBillRefNo(tMCDTLData.getBillRefNo());
                contractInfo.setBillRefDate(tMCDTLData.getBillRefDate());
                contractInfo.setBillRefAmount(tMCDTLData.getBillRefAmount());
                contractInfo.setManageContractType(tMCDTLData.getManageContractType());
                contractInfo.setRemark(tMCDTLData.getRemark());
                evenItem.setContractInfo(contractInfo);



                // append eventItem
                evenItems.add(evenItem);

                
                
            }
            // EventItem
            omEv.setEventItems(evenItems);
        }

        
        

        // saleInfo
        if(inputData.has("saleInfo")){
            JSONObject inputSaleInfo = inputData.getJSONObject("saleInfo");
            SaleInfo saleInfo = new SaleInfo();
            saleInfo.setSaleEmpId(inputSaleInfo.getString("saleEmpId"));
            saleInfo.setSapCode(inputSaleInfo.getString("sapCode"));
            saleInfo.setDealerCode(inputSaleInfo.getString("dealerCode"));
            saleInfo.setRegisterBySellerName(inputSaleInfo.getString("registerBySellerName"));
            saleInfo.setSaleRole(inputSaleInfo.getString("saleRole"));
            saleInfo.setRegisterProvince(inputSaleInfo.getString("registerProvince"));
            saleInfo.setTerritoryName(inputSaleInfo.getString("territoryName"));
            saleInfo.setSaleRepEmpId(inputSaleInfo.getString("saleRepEmpId"));
            saleInfo.setSaleRepSapCode(inputSaleInfo.getString("saleRepSapCode"));
            omEv.setSaleInfo(saleInfo);
        }

        // writtenLanguage
        if(inputData.has("writtenLanguage")){
            omEv.setWrittenLanguage(inputData.getString("writtenLanguage"));
        }

        // ivrLanguage
        if(inputData.has("ivrLanguage")){
            omEv.setIvrLanguage(inputData.getString("ivrLanguage"));
        }

        // orderStatus
        if(inputData.has("orderHeader")){
            JSONObject orderHeader = inputData.getJSONObject("orderHeader");
            if(orderHeader.has("orderStatus")){
                omEv.setOrderStatus(orderHeader.getString("orderStatus"));
            }
        }

        sendData.setTriggerDate(triggerDate);
        sendData.setPublishChannel("OM-MFE");
        sendData.setOrderType(receivedData.getOrderType().toUpperCase());
        sendData.setMsisdn(String.format("0", odheader.getMsisdn())); // your code here
        sendData.setEventData(omEv);

        return sendData;
        
    }

    private Data MappingTopUpData(ReceiveTopUpDataType receivedData ){
        System.out.println(receivedData.toString());
        String effectiveDate = DateTime.getTriggerTimeStampNow();
        String triggerDate = DateTime.getTimeStampNowStr();

        Data sendData = new Data();

        EventData topUpEv = new EventData();
        topUpEv.setEventType("TOPUP_RECHARGE");

        List<EventItem> eventItems = new ArrayList<EventItem>();
        EventItem eventItem = new EventItem();
        eventItem.setItemType("Topup");
        eventItem.setExecutionType("Now");
        eventItem.setEffectiveDate(effectiveDate);

        // EventItem TopUp 
        TopUp topUp = new TopUp();
        topUp.setTopupType(receivedData.getRechargeType());
        topUp.setRechargeAmount(String.valueOf(receivedData.getRechargeAmount()));
        topUp.setCurrencyId(receivedData.getCurrencyId());
        topUp.setChannelId(receivedData.getChannelId());
        topUp.setRechargeDate(receivedData.getRechargeDate());
        topUp.setNotiMsgSeq(receivedData.getNotiMsgSeq());
        topUp.setRechargeLogId(receivedData.getRechargeLogId());
        eventItem.setTopUp(topUp);
        eventItems.add(eventItem);
        
        topUpEv.setEventItems(eventItems);

        sendData.setTriggerDate(triggerDate);
        sendData.setPublishChannel("Topup-GW");
        sendData.setOrderType("TOPUP_RECHARGE");
        sendData.setMsisdn(String.format("0%s", receivedData.getMsisdn()));
        sendData.setEventData(topUpEv);

        return sendData;
    }

    private Data MappingExpiredData(ReceiveExpiredDataType receivedData ){
        System.out.println(receivedData.toString());

        String effectiveDate = DateTime.getTriggerTimeStampNow();
        String triggerDate = DateTime.getTimeStampNowStr();

        Data sendData = new Data();

        EventData expiredEv = new EventData();

        OrderHeaderData odheader = ommyfrontService.getOrderHeaderDataByPoID(receivedData.getPoId());
        
        String externalId = odheader.getMsisdn();
        System.out.println("externalId: "+externalId);
        INVMappingData invMappingData = invuserService.getInvMappingData(externalId);
        System.out.println("inv getImsi: "+invMappingData.getImsi());
        List<IMSIOfferingConfig> imsiOfferConfigList = ommyfrontService.getImsiOfferingConfigList();

        IMSIOfferingConfig imsiConfigData = getImsiConfigByImsi(invMappingData.getImsi(), imsiOfferConfigList);

        // TransManageContractDTLData transmanageData = omuserService.getTransManageContractDTLData("");

        
        // System.out.println(odheader.getInputData());
        JSONObject inputData = new JSONObject(odheader.getInputData().toString());

        expiredEv.setEventType("PACKAGE_EXPIRE");

        List<EventItem> eventItems = new ArrayList<EventItem>();
        EventItem eventItem = new EventItem();
        eventItem.setItemType("PackageExpire"); // Fix
        eventItem.setExecutionType("Now");
        eventItem.setEffectiveDate(effectiveDate);

        
        // eventItem
        if (inputData.has("orderItem")){
            JSONArray orderItems = inputData.getJSONArray("orderItem");
            for (int i = 0; i < orderItems.length(); i++){
                JSONObject orderItem = orderItems.getJSONObject(i);
                EventItem evenItem = new EventItem();

                // Expire data
                Expire expire = new Expire();
                expire.setMsisdn(receivedData.getMsisdn());
                expire.setExpireOfferId(receivedData.getExpireOfferId());
                expire.setExpireOfferInstId(receivedData.getExpireOfferInstId());
                expire.setExpireOfferName(receivedData.getExpireOfferName());
                expire.setExpireDate(receivedData.getExpireDate());
                expire.setOfferPerchaseSeq(receivedData.getOfferPerchaseSeq());
                expire.setNotiMsgSeq(receivedData.getNotiMsgSeq());
                expire.setPoId(receivedData.getPoId());
                expire.setPoName(receivedData.getPoName());
                expire.setBrandId(receivedData.getBrandId());
                expire.setBrandName(receivedData.getBrandName());
                eventItem.setExpire(expire);


                /*  
                * Offer 
                */

                // OrderItem
                JSONArray productOfferingList = new JSONArray();
                if (orderItem.has("productOffering")){
                    productOfferingList = orderItem.getJSONArray("productOffering");
                }

                JSONObject subscriberInfo = new JSONObject();
                if (orderItem.has("subscriberInfo")){
                    subscriberInfo = orderItem.getJSONObject("subscriberInfo");
                }

                List<Offer> offers = new ArrayList<Offer>();
                Offer offer = new Offer();
                
                List<String> offeringIdList = new ArrayList<>();
                if (productOfferingList.length() > 0){
                    for(int j=0;j<productOfferingList.length();j++){
                        JSONObject productOffering = productOfferingList.getJSONObject(j);
                        if (productOffering.has("offeringId")){
                            String offeringId = productOffering.getString("offeringId");
                            offeringIdList.add(offeringId);
                        }
                    }
                }
                    
                for(int j=0;j<offeringIdList.size();j++){
                    JSONObject productOffering = productOfferingList.getJSONObject(j);
                    String offeringId = offeringIdList.get(j);

                    OfferingSpecData ofrspec = catmfeService.getOfferingSpecByOfferingId(offeringId);

                    offer.setOfferingId(offeringId);

                    if (productOffering != null ){
                        if (productOffering.has("offeringType")){
                            offer.setOfferingType(productOffering.getString("offeringType"));
                        }
                        
                        if (productOffering.has("actionFlag")){
                            offer.setActionFlag(productOffering.getString("actionFlag"));
                        }
                    }
                    
                    if (ofrspec != null){

                        offer.setOfferingNameTh(ofrspec.getOfferingnameTH());

                        offer.setOfferingNameEn(ofrspec.getOfferingnameEN());

                        offer.setPackageId(ofrspec.getPackageID());

                        offer.setPackageName(ofrspec.getPackageName());

                        offer.setDescriptionTh(ofrspec.getDescTH());

                        offer.setDescriptionEn(ofrspec.getDescEN());

                        if (subscriberInfo != null){
                            if (subscriberInfo.has("serviceType")){
                                offer.setServiceType(subscriberInfo.getInt("serviceType"));
                            }
                        }    

                        offer.setOcsOfferingName(ofrspec.getOcsofferingname());

                        BigDecimal rcamount = new BigDecimal(ofrspec.getRcamount());
                        offer.setRcAmount(rcamount);

                        BigDecimal rcvatamount = new BigDecimal(ofrspec.getRcvatamount());
                        offer.setRcVatAmount(rcvatamount);

                        offer.setPeriod(ofrspec.getPeriod());

                        offer.setUnitPeriod(ofrspec.getUnitperiod());

                        offer.setSaleStartDate(ofrspec.getSalestartdate());

                        offer.setSaleEndDate(ofrspec.getSaleenddate());

                        BigDecimal maxdayafteractivedate = new BigDecimal(ofrspec.getMaxdayafteractivedate());
                        offer.setMaxDayAfterActiveDate(maxdayafteractivedate);

                        offer.setNiceNumberFlag(ofrspec.getNicenumberflag());

                        BigDecimal nicenumberlevel = new BigDecimal(ofrspec.getNicenumberlevel());
                        offer.setNiceNumberLevel(nicenumberlevel);

                        offer.setConTractFlag(ofrspec.getContractflag());

                        BigDecimal contractunitperiod = new BigDecimal(ofrspec.getContractunitperiod());
                        offer.setContractUnitPeriod(contractunitperiod);

                        offer.setCatEmpFlag(ofrspec.getCatempflag());

                        offer.setCatRetireEmpFlag(ofrspec.getRetiredcatempflag());

                        // offer.multisimFlag your code here with logic
                        offer.setMultisimFlag(String.valueOf(invMappingData.getMultisimFlag()));

                        offer.setTopupSimFlag(ofrspec.getTopupsimflag());

                        offer.setTouristSimFlag(ofrspec.getTouristsimflag());

                        offer.setChangePoUssdCode(ofrspec.getChangepoussdcode());

                        offer.setAddSoUssdCode(ofrspec.getAddsoussdcode());

                        offer.setDeleteSoUssdCode(ofrspec.getDeletesoussdcode());

                        // offer.frequency your code here with logic
                        String imsiMapping = invMappingData.getImsi();
                        String imsiFrequency = "";
                        for (IMSIOfferingConfig config : imsiOfferConfigList) {
                            String prefix = config.getImsiPrefix();
                            if (imsiMapping.startsWith(prefix)) {
                                imsiMapping = prefix;
                                imsiFrequency = config.getFrequency();
                                break;
                            }
                        }

                        offer.setFrequency(imsiFrequency);

                        offer.setCanSwapPoFlag(ofrspec.getCanswappoflag());

                        offers.add(offer);
                    }
                }

                evenItem.setOffer(offers);
                /*  
                * End offer
                */
            }
            eventItems.add(eventItem);
        }

        expiredEv.setEventItems(eventItems);

        

        sendData.setTriggerDate(triggerDate);
        sendData.setPublishChannel("OM-MFE");
        sendData.setOrderType("PACKAGE_EXPIRE");
        sendData.setMsisdn(String.format("0%s", receivedData.getMsisdn()));
        sendData.setEventData(expiredEv);

        return sendData;
    }


    private IMSIOfferingConfig getImsiConfigByImsi(String imsi, List<IMSIOfferingConfig> imsiConfigs){
        for (IMSIOfferingConfig config : imsiConfigs) {
            String prefix = config.getImsiPrefix();
            if (imsi.startsWith(prefix)) {
                return config;
            }
        }
        return null;
    }
}
