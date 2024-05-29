package com.nt.subscribtion_data.service;

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
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveOMDataType receivedData = objectMapper.readValue(message, ReceiveOMDataType.class);
        
        // Mapping DataType
        Data sendData = MappingOMData(receivedData);

        
        TriggerMessageData triggerMsg = new TriggerMessageData();
        distributeService.CreateTriggerMessage(triggerMsg);

        return sendData;
    }

    private Data processTopUpType(String message) throws JsonMappingException, JsonProcessingException, SQLException {
        // Process for new_order_type
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveTopUpDataType receivedData = objectMapper.readValue(message, ReceiveTopUpDataType.class);
        
        // Mapping DataType
        Data sendData = MappingTopUpData(receivedData);

        
        TriggerMessageData triggerMsg = new TriggerMessageData();
        distributeService.CreateTriggerMessage(triggerMsg);

        return sendData;
    }

    private Data processExpiredType(String message) throws JsonMappingException, JsonProcessingException, SQLException {
        // Process for new_order_type
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveExpiredDataType receivedData = objectMapper.readValue(message, ReceiveExpiredDataType.class);
        
        // Mapping DataType
        Data sendData = MappingExpiredData(receivedData);

        
        TriggerMessageData triggerMsg = new TriggerMessageData();
        distributeService.CreateTriggerMessage(triggerMsg);

        return sendData;
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
        INVMappingData invMappingData = invuserService.getInvMappingData(externalId);

        List<IMSIOfferingConfig> imsiOfferConfigList = ommyfrontService.getImsiOfferingConfigList();

        TransManageContractDTLData transmanageData = omuserService.getTransManageContractDTLData("");

        
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

        if (inputData.has("orderItem")){
            List<EventItem> evenItems = new ArrayList<>();
            JSONArray orderItems = inputData.getJSONArray("orderItem");
            for (int i = 0; i < orderItems.length(); i++){
                JSONObject orderItem = orderItems.getJSONObject(i);
                EventItem evenItem = new EventItem();

                // EventItem
                if (orderItem.has("orderId")){
                    evenItem.setItemType(orderItem.getString("orderId"));
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
                JSONObject productOffering = null;
                if (orderItem.has("productOffering")){
                    productOffering = orderItem.getJSONObject("productOffering");
                }

                JSONObject subscriberInfo = null;
                if (orderItem.has("subscriberInfo")){
                    subscriberInfo = orderItem.getJSONObject("subscriberInfo");
                }
                
                /*  
                 * Offer 
                 */
                List<Offer> offers = new ArrayList<Offer>();
                Offer offer = new Offer();

                String offeringId = null;
                if (productOffering != null ){
                    if (productOffering.has("offeringId")){
                        offeringId = productOffering.getString("offeringId");
                    }

                    OfferingSpecData ofrspec = catmfeService.getOfferingSpecByOfferingId(offeringId);
        
                    offer.setOfferingId(offeringId);

                    if (productOffering.has("offeringType")){
                        offer.setOfferingType(productOffering.getString("offeringType"));
                    }
                    
                    if (productOffering.has("actionFlag")){
                        offer.setActionFlag(productOffering.getString("actionFlag"));
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

                        evenItem.setOffer(offers);
                        /*  
                        * End offer
                        */
                    }
                }

                

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
                            photo.setPhotoType(orderPhoto.getString("photoType"));
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
                }

                if (inputSourceCustomerAccount != null){
                    if (inputSourceCustomerAccount.has("address")){
                        inputSourceCustomerAccountAddress = inputSourceCustomerAccount.getJSONObject("address");

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

                    }

                    if (inputSourceCustomerAccount.has("billingAccount")){
                        sourceCustomerAccountBillingAccount = inputSourceCustomerAccount.getJSONObject("billingAccount");
                    }

                    if (inputSourceCustomerAccount.has("billDeliveryAddress")){
                        sourceCustomerAccountBillDeliveryAddress = inputSourceCustomerAccount.getJSONObject("billDeliveryAddress");
                    }

                    if (inputSourceCustomerAccount.has("vatAddress")){
                        sourceCustomerAccountVatAddress = inputSourceCustomerAccount.getJSONObject("vatAddress");

                        // vat address
                        if (sourceCustomerAccountVatAddress.has("building")){
                            vatAddress.setBuilding(sourceCustomerAccountVatAddress.getString("building"));
                        }

                        if (sourceCustomerAccountVatAddress.has("country")){
                            vatAddress.setCountry(sourceCustomerAccountVatAddress.getString("country"));
                        }

                        if (sourceCustomerAccountVatAddress.has("houseNumber")){
                            vatAddress.setHouseNumber(sourceCustomerAccountVatAddress.getString("houseNumber"));
                        }

                        if (sourceCustomerAccountVatAddress.has("khetAmphur")){
                            vatAddress.setKhetAmphur(sourceCustomerAccountVatAddress.getString("khetAmphur"));
                        }

                        if (sourceCustomerAccountVatAddress.has("kwangTambon")){
                            vatAddress.setKwangTambon(sourceCustomerAccountVatAddress.getString("kwangTambon"));
                        }

                        if (sourceCustomerAccountVatAddress.has("moo")){
                            vatAddress.setMoo(sourceCustomerAccountVatAddress.getString("moo"));
                        }

                        if (sourceCustomerAccountVatAddress.has("postCode")){
                            vatAddress.setPostCode(sourceCustomerAccountVatAddress.getString("postCode"));
                        }

                        if (sourceCustomerAccountVatAddress.has("province")){
                            vatAddress.setProvince(sourceCustomerAccountVatAddress.getString("province"));
                        }

                        if (sourceCustomerAccountVatAddress.has("road")){
                            vatAddress.setRoad(sourceCustomerAccountVatAddress.getString("road"));
                        }

                        if (sourceCustomerAccountVatAddress.has("troksoi")){
                            vatAddress.setTroksoi(sourceCustomerAccountVatAddress.getString("troksoi"));
                        }

                        if (sourceCustomerAccountVatAddress.has("village")){
                            vatAddress.setVillage(sourceCustomerAccountVatAddress.getString("village"));
                        }
                    }
                    
                    if (inputSourceCustomerAccount.has("vatDeliveryAddress")){
                        sourceCustomerAccountVatDeliveryAddress = inputSourceCustomerAccount.getJSONObject("vatDeliveryAddress");

                        // vat delivery address
                        if (sourceCustomerAccountVatDeliveryAddress.has("building")){
                            vatDeliveryAddress.setBuilding(sourceCustomerAccountVatDeliveryAddress.getString("building"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.has("country")){
                            vatDeliveryAddress.setCountry(sourceCustomerAccountVatDeliveryAddress.getString("country"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.has("houseNumber")){
                            vatDeliveryAddress.setHouseNumber(sourceCustomerAccountVatDeliveryAddress.getString("houseNumber"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.has("khetAmphur")){
                            vatDeliveryAddress.setKhetAmphur(sourceCustomerAccountVatDeliveryAddress.getString("khetAmphur"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.has("kwangTambon")){
                            vatDeliveryAddress.setKwangTambon(sourceCustomerAccountVatDeliveryAddress.getString("kwangTambon"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.has("moo")){
                            vatDeliveryAddress.setMoo(sourceCustomerAccountVatDeliveryAddress.getString("moo"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.has("postCode")){
                            vatDeliveryAddress.setPostCode(sourceCustomerAccountVatDeliveryAddress.getString("postCode"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.has("province")){
                            vatDeliveryAddress.setProvince(sourceCustomerAccountVatDeliveryAddress.getString("province"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.has("road")){
                            vatDeliveryAddress.setRoad(sourceCustomerAccountVatDeliveryAddress.getString("road"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.has("troksoi")){
                            vatDeliveryAddress.setTroksoi(sourceCustomerAccountVatDeliveryAddress.getString("troksoi"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.has("village")){
                            vatDeliveryAddress.setVillage(sourceCustomerAccountVatDeliveryAddress.getString("village"));
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


                }

                
                if (sourceCustomerAccountBillingAccount != null){
                    if (sourceCustomerAccountBillingAccount.has("billingInfo")){
                        sourceCustomerAccountBillingAccountBillingInfo = sourceCustomerAccountBillingAccount.getJSONObject("billingInfo");

                        // billing info
                        if (sourceCustomerAccountBillingAccountBillingInfo.has("collectionUnit")){
                            billingInfo.setCollectionUnit(sourceCustomerAccountBillingAccountBillingInfo.getString("collectionUnit"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.has("vat")){
                            billingInfo.setVat(sourceCustomerAccountBillingAccountBillingInfo.getString("vat"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.has("billingPeriod")){
                            billingInfo.setBillingPeriod(sourceCustomerAccountBillingAccountBillingInfo.getString("billingPeriod"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.has("billable")){
                            billingInfo.setBillable(sourceCustomerAccountBillingAccountBillingInfo.getString("billable"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.has("billingGroup")){
                            billingInfo.setBillingGroup(sourceCustomerAccountBillingAccountBillingInfo.getString("billingGroup"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.has("collectionTreatment")){
                            billingInfo.setCollectionTreatment(sourceCustomerAccountBillingAccountBillingInfo.getString("collectionTreatment"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.has("dispatchMethod")){
                            billingInfo.setDispatchMethod(sourceCustomerAccountBillingAccountBillingInfo.getString("dispatchMethod"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.has("emailAddress")){
                            billingInfo.setEmailAddress(sourceCustomerAccountBillingAccountBillingInfo.getString("emailAddress"));
                        }
                    }
                }


                // billing delivery address
                if (sourceCustomerAccountBillDeliveryAddress != null){
                    if (sourceCustomerAccountBillDeliveryAddress.has("building")){
                        billDeliveryAddress.setBuilding(sourceCustomerAccountBillDeliveryAddress.getString("building"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.has("country")){
                        billDeliveryAddress.setCountry(sourceCustomerAccountBillDeliveryAddress.getString("country"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.has("houseNumber")){
                        billDeliveryAddress.setHouseNumber(sourceCustomerAccountBillDeliveryAddress.getString("houseNumber"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.has("khetAmphur")){
                        billDeliveryAddress.setKhetAmphur(sourceCustomerAccountBillDeliveryAddress.getString("khetAmphur"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.has("kwangTambon")){
                        billDeliveryAddress.setKwangTambon(sourceCustomerAccountBillDeliveryAddress.getString("kwangTambon"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.has("moo")){
                        billDeliveryAddress.setMoo(sourceCustomerAccountBillDeliveryAddress.getString("moo"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.has("postCode")){
                        billDeliveryAddress.setPostCode(sourceCustomerAccountBillDeliveryAddress.getString("postCode"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.has("province")){
                        billDeliveryAddress.setProvince(sourceCustomerAccountBillDeliveryAddress.getString("province"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.has("road")){
                        billDeliveryAddress.setRoad(sourceCustomerAccountBillDeliveryAddress.getString("road"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.has("troksoi")){
                        billDeliveryAddress.setTroksoi(sourceCustomerAccountBillDeliveryAddress.getString("troksoi"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.has("village")){
                        billDeliveryAddress.setVillage(sourceCustomerAccountBillDeliveryAddress.getString("village"));
                    }
                }
                

                // destinationCustomerAccount
                if (inputSourceCustomerAccount != null){

                    if (inputSourceCustomerAccount.has("cardNumber")){
                        destinationCustomerAccount.setCardNumber(inputSourceCustomerAccount.getString("cardNumber"));
                    }

                    if (inputSourceCustomerAccount.has("cardType")){
                        destinationCustomerAccount.setCardType(inputSourceCustomerAccount.getString("cardType"));
                    }

                    if (inputSourceCustomerAccount.has("catEmployeeFlag")){
                        destinationCustomerAccount.setCatEmployeeFlag(inputSourceCustomerAccount.getString("catEmployeeFlag"));
                    }

                    if (inputSourceCustomerAccount.has("companyBranchId")){
                        destinationCustomerAccount.setCompanyBranchId(inputSourceCustomerAccount.getString("companyBranchId"));
                    }

                    if (inputSourceCustomerAccount.has("companyName")){
                        destinationCustomerAccount.setCompanyName(inputSourceCustomerAccount.getString("companyName"));
                    }

                    if (inputSourceCustomerAccount.has("companyType")){
                        destinationCustomerAccount.setCompanyType(inputSourceCustomerAccount.getString("companyType"));
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
                        destinationCustomerAccount.setCustomerGroup(inputSourceCustomerAccount.getString("customerGroup"));
                    }

                    if (inputSourceCustomerAccount.has("customerId")){
                        destinationCustomerAccount.setCustomerId(inputSourceCustomerAccount.getString("customerId"));
                    }

                    if (inputSourceCustomerAccount.has("customerInfoType")){
                        destinationCustomerAccount.setCustomerInfoType(inputSourceCustomerAccount.getString("customerInfoType"));
                    }

                    if (inputSourceCustomerAccount.has("customerSegment")){
                        destinationCustomerAccount.setCustomerSegment(inputSourceCustomerAccount.getString("customerSegment"));
                    }

                    if (inputSourceCustomerAccount.has("customerType")){
                        destinationCustomerAccount.setCustomerType(inputSourceCustomerAccount.getString("customerType"));
                    }

                    if (inputSourceCustomerAccount.has("dob")){
                        destinationCustomerAccount.setDob(inputSourceCustomerAccount.getString("dob"));
                    }

                    if (inputSourceCustomerAccount.has("documentNumber")){
                        destinationCustomerAccount.setDocumentNumber(inputSourceCustomerAccount.getString("documentNumber"));
                    }

                    if (inputSourceCustomerAccount.has("documentType")){
                        destinationCustomerAccount.setDocumentType(inputSourceCustomerAccount.getString("documentType"));
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
                        destinationCustomerAccount.setGender(inputSourceCustomerAccount.getString("gender"));
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
                        destinationCustomerAccount.setTitle(inputSourceCustomerAccount.getString("title"));
                    }

                    if (inputSourceCustomerAccount.has("writtenLanguage")){
                        destinationCustomerAccount.setWrittenLanguage(inputSourceCustomerAccount.getString("writtenLanguage"));
                    }
                }

                billingAccount.setBillingAccountId(offeringId);
                billingAccount.setExistingFlag(null);
                billingAccount.setPaymentProfile(externalId);
                billingAccount.setBillingAccountId("");
                billingAccount.setBillingInfo(billingInfo);
                billingAccount.setVatAddress(vatAddress);
                billingAccount.setVatDeliveryAddress(vatDeliveryAddress);
                billingAccount.setBillDeliveryAddress(billDeliveryAddress);
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
                        topUp.setTopupType(inputTopUp.getString("topupType"));
                    }

                    if (inputData.has("rechargeAmount")){
                        topUp.setRechargeAmount(inputTopUp.getInt("rechargeAmount"));
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
                JSONObject inputCreditLimit = null;
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
                

                

                /*
                *  destinationSubscriberInfo
                */
                JSONObject inputSubscriberInfo = null;
                if (inputData.has("subscriberInfo")){
                    inputSubscriberInfo = inputData.getJSONObject("subscriberInfo");

                    JSONObject inputSourceSimInfo = null;
                    if (inputData.has("sourceSimInfo")){
                        inputSourceSimInfo = inputSubscriberInfo.getJSONObject("sourceSimInfo");

                        // Source sim info
                        List<SourceSimInfo> sourceSimInfoList = new ArrayList<SourceSimInfo>();
                        SourceSimInfo sourceSimInfo = new SourceSimInfo();
                        sourceSimInfo.setIccid(inputSourceSimInfo.getString("iccid"));
                        sourceSimInfo.setImsi("query from db"); // your code here query
                        sourceSimInfo.setSimType(inputSourceSimInfo.getString("simType"));
                        sourceSimInfo.setFrequency("query from db"); // your code here query
                        sourceSimInfoList.add(sourceSimInfo);

                        JSONObject inputDestinationSimInfo = null;
                        if (inputData.has("destinationSimInfo")){
                            inputDestinationSimInfo = inputSubscriberInfo.getJSONObject("destinationSimInfo");
                            
                            // Source sim info
                            List<DestinationSimInfo> destinationSimInfoList = new ArrayList<DestinationSimInfo>();
                            DestinationSimInfo destinationSimInfo = new DestinationSimInfo();
                            
                            if (inputDestinationSimInfo.has("iccid")){
                                destinationSimInfo.setIccid(inputDestinationSimInfo.getString("iccid"));
                                destinationSimInfo.setImsi("imsi"); // search and query
                            }                            
                            destinationSimInfo.setSimType(null);

                            destinationSimInfo.setFrequency(null); // Fix null
                            destinationSimInfoList.add(destinationSimInfo);

                            destinationSubscriberInfo.setDestinationSimInfo(destinationSimInfoList);
                            destinationSubscriberInfo.setSourceSimInfo(sourceSimInfoList);

                            if (inputDestinationSimInfo.has("itouristSimFlag")){
                                destinationSubscriberInfo.setTouristSimFlag(inputDestinationSimInfo.getString("itouristSimFlag"));
                            }

                            if (inputDestinationSimInfo.has("subscriberNumber")){
                                destinationSubscriberInfo.setSubscriberNumber(inputDestinationSimInfo.getString("subscriberNumber"));
                            }

                        }
                        evenItem.setDestinationSubscriberInfo(destinationSubscriberInfo);
                    }
    
                    
                    
                    if (inputSubscriberInfo.has("msisdn")){
                        destinationSubscriberInfo.setMsisdn(inputSubscriberInfo.getString("msisdn"));
                    }

                    if (inputSubscriberInfo.has("serviceType")){
                        destinationSubscriberInfo.setServiceType(inputSubscriberInfo.getString("serviceType"));
                    }
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
                
                JSONObject orderItemBalanceTransferInfo = null;
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
                JSONObject orderItemExtendExpireInfo = null;
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
        }

        // saleInfo
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
        omEv.setWrittenLanguage(inputData.getString("writtenLanguage"));
        omEv.setIvrLanguage(inputData.getString("ivrLanguage"));

        // Order Header
        JSONObject orderHeader = inputData.getJSONObject("orderHeader");
        omEv.setOrderStatus(orderHeader.getString("orderStatus"));

        

        sendData.setTriggerDate(triggerDate);
        sendData.setPublishChannel("OM-MFE");
        sendData.setOrderType(receivedData.getOrderType().toUpperCase());
        sendData.setMsisdn(String.format("0", "query")); // your code here
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
        topUp.setTopupType(String.valueOf(receivedData.getRechargeType()));
        topUp.setRechargeAmount(receivedData.getRechargeAmount());
        topUp.setCurrencyId(receivedData.getCurrencyId());
        topUp.setChannelId(receivedData.getChannelId());
        topUp.setRechargeDate(receivedData.getRechargeDate());
        topUp.setNotiMsgSeq(receivedData.getNotiMsgSeq());
        topUp.setRechargeLogId(receivedData.getRechargeLogId());
        eventItem.setTopUp(topUp);
        eventItems.add(eventItem);

        sendData.setTriggerDate(triggerDate);
        sendData.setPublishChannel("Topup-GW");
        sendData.setOrderType("TOPUP_RECHARGE");
        sendData.setMsisdn(String.format("0", receivedData.getMsisdn()));
        sendData.setEventData(topUpEv);

        return sendData;
    }

    private Data MappingExpiredData(ReceiveExpiredDataType receivedData ){
        System.out.println(receivedData.toString());

        String effectiveDate = DateTime.getTriggerTimeStampNow();
        String triggerDate = DateTime.getTimeStampNowStr();

        Data sendData = new Data();

        EventData expiredEv = new EventData();
        expiredEv.setEventType("PACKAGE_EXPIRE");

        List<EventItem> eventItems = new ArrayList<EventItem>();
        EventItem eventItem = new EventItem();
        eventItem.setItemType("PackageExpire"); // Fix
        eventItem.setExecutionType("Now");
        eventItem.setEffectiveDate(effectiveDate);

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


        // Offer data
        List<Offer> offers = new ArrayList<Offer>();
        Offer offer = new Offer();
        offer.setOfferingId(receivedData.getPoId());
        eventItem.setOffer(offers);


        eventItems.add(eventItem);

        sendData.setTriggerDate(triggerDate);
        sendData.setPublishChannel("OM-MFE");
        sendData.setOrderType("PACKAGE_EXPIRE");
        sendData.setMsisdn(String.format("0", receivedData.getMsisdn()));
        sendData.setEventData(expiredEv);

        return sendData;
    }
}
