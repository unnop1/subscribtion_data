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
                if (orderItem.optString("orderId") != null){
                    evenItem.setItemType(orderItem.getString("orderId"));
                }
                
                if (orderItem.optString("orderExecutionDate") != null){
                    evenItem.setEffectiveDate(orderItem.getString("orderExecutionDate"));
                }
                
                if (orderItem.optString("orderExecutionType") != null){
                    evenItem.setExecutionType(orderItem.getString("orderExecutionType"));
                }

                if (orderItem.optString("sourceEntity") != null){
                    evenItem.setSourceEntity(orderItem.getString("sourceEntity"));
                }

                if (orderItem.optString("userRole") != null){
                    evenItem.setUserRole(orderItem.getString("userRole"));
                }

                // OrderItem
                JSONObject productOffering = null;
                if (orderItem.optString("productOffering") != null){
                    productOffering = orderItem.getJSONObject("productOffering");
                }

                JSONObject subscriberInfo = null;
                if (orderItem.optString("subscriberInfo") != null){
                    subscriberInfo = orderItem.getJSONObject("subscriberInfo");
                }
                
                /*  
                 * Offer 
                 */
                List<Offer> offers = new ArrayList<Offer>();
                Offer offer = new Offer();

                String offeringId = null;
                if (productOffering != null){
                    if (productOffering.optString("offeringId") != null){
                        offeringId = productOffering.getString("offeringId");
                    }

                    OfferingSpecData ofrspec = catmfeService.getOfferingSpecByOfferingId(offeringId);
        
                    offer.setOfferingId(offeringId);

                    if (productOffering.optString("offeringType") != null){
                        offer.setOfferingType(productOffering.getString("offeringType"));
                    }
                    
                    if (productOffering.optString("actionFlag") != null){
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
                            if (subscriberInfo.optString("serviceType") != null){
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
                if (orderItem.optString("photo") != null){
                    JSONArray orderPhotos = orderItem.getJSONArray("photo");
                    for (int j = 0; j < orderPhotos.length(); j++){
                        JSONObject orderPhoto = orderPhotos.getJSONObject(j);
                        Photo photo = new Photo();
                        if (orderPhoto.optString("photoId") != null){
                            photo.setPhotoId(orderPhoto.getString("photoId"));
                        }

                        if (orderPhoto.optString("photoType") != null){
                            photo.setPhotoType(orderPhoto.getString("photoType"));
                        }

                        if (orderPhoto.optString("dummyPhotoFlag") != null){
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
                if (inputData.optString("sourceCustomerAccount") != null){
                    inputSourceCustomerAccount = inputData.getJSONObject("sourceCustomerAccount");
                }

                if (inputData.optString("southernContactAddress") != null){
                    inputSouthernContactAddress = inputData.getJSONObject("southernContactAddress");

                    /*
                    *  SouthernContactAddress
                    */
                    if (inputSouthernContactAddress.optString("building") != null){
                        southernContactAddress.setBuilding(inputSouthernContactAddress.getString("building"));
                    }

                    if (inputSouthernContactAddress.optString("country") != null){
                        southernContactAddress.setCountry(inputSouthernContactAddress.getString("country"));
                    }
                    
                    if (inputSouthernContactAddress.optString("houseNumber") != null){
                        southernContactAddress.setHouseNumber(inputSouthernContactAddress.getString("houseNumber"));
                    }

                    if (inputSouthernContactAddress.optString("khetAmphur") != null){
                        southernContactAddress.setKhetAmphur(inputSouthernContactAddress.getString("khetAmphur"));
                    }

                    if (inputSouthernContactAddress.optString("kwangTambon") != null){
                        southernContactAddress.setKwangTambon(inputSouthernContactAddress.getString("kwangTambon"));
                    }

                    if (inputSouthernContactAddress.optString("moo") != null){
                        southernContactAddress.setMoo(inputSouthernContactAddress.getString("moo"));
                    }

                    if (inputSouthernContactAddress.optString("postCode") != null){
                        southernContactAddress.setPostCode(inputSouthernContactAddress.getString("postCode"));
                    }

                    if (inputSouthernContactAddress.optString("province") != null){
                        southernContactAddress.setProvince(inputSouthernContactAddress.getString("province"));
                    }

                    if (inputSouthernContactAddress.optString("road") != null){
                        southernContactAddress.setRoad(inputSouthernContactAddress.getString("road"));
                    }

                    if (inputSouthernContactAddress.optString("troksoi") != null){
                        southernContactAddress.setTroksoi(inputSouthernContactAddress.getString("troksoi"));
                    }

                    if (inputSouthernContactAddress.optString("village") != null){
                        southernContactAddress.setVillage(inputSouthernContactAddress.getString("village"));
                    }
                }

                if (inputSourceCustomerAccount != null){
                    if (inputSourceCustomerAccount.optString("address") != null){
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

                    if (inputSourceCustomerAccount.optString("billingAccount") != null){
                        sourceCustomerAccountBillingAccount = inputSourceCustomerAccount.getJSONObject("billingAccount");
                    }

                    if (inputSourceCustomerAccount.optString("billDeliveryAddress") != null){
                        sourceCustomerAccountBillDeliveryAddress = inputSourceCustomerAccount.getJSONObject("billDeliveryAddress");
                    }

                    if (inputSourceCustomerAccount.optString("vatAddress") != null){
                        sourceCustomerAccountVatAddress = inputSourceCustomerAccount.getJSONObject("vatAddress");

                        // vat address
                        if (sourceCustomerAccountVatAddress.optString("building") != null){
                            vatAddress.setBuilding(sourceCustomerAccountVatAddress.getString("building"));
                        }

                        if (sourceCustomerAccountVatAddress.optString("country") != null){
                            vatAddress.setCountry(sourceCustomerAccountVatAddress.getString("country"));
                        }

                        if (sourceCustomerAccountVatAddress.optString("houseNumber") != null){
                            vatAddress.setHouseNumber(sourceCustomerAccountVatAddress.getString("houseNumber"));
                        }

                        if (sourceCustomerAccountVatAddress.optString("khetAmphur") != null){
                            vatAddress.setKhetAmphur(sourceCustomerAccountVatAddress.getString("khetAmphur"));
                        }

                        if (sourceCustomerAccountVatAddress.optString("kwangTambon") != null){
                            vatAddress.setKwangTambon(sourceCustomerAccountVatAddress.getString("kwangTambon"));
                        }

                        if (sourceCustomerAccountVatAddress.optString("moo") != null){
                            vatAddress.setMoo(sourceCustomerAccountVatAddress.getString("moo"));
                        }

                        if (sourceCustomerAccountVatAddress.optString("postCode") != null){
                            vatAddress.setPostCode(sourceCustomerAccountVatAddress.getString("postCode"));
                        }

                        if (sourceCustomerAccountVatAddress.optString("province") != null){
                            vatAddress.setProvince(sourceCustomerAccountVatAddress.getString("province"));
                        }

                        if (sourceCustomerAccountVatAddress.optString("road") != null){
                            vatAddress.setRoad(sourceCustomerAccountVatAddress.getString("road"));
                        }

                        if (sourceCustomerAccountVatAddress.optString("troksoi") != null){
                            vatAddress.setTroksoi(sourceCustomerAccountVatAddress.getString("troksoi"));
                        }

                        if (sourceCustomerAccountVatAddress.optString("village") != null){
                            vatAddress.setVillage(sourceCustomerAccountVatAddress.getString("village"));
                        }
                    }
                    
                    if (inputSourceCustomerAccount.optString("vatDeliveryAddress") != null){
                        sourceCustomerAccountVatDeliveryAddress = inputSourceCustomerAccount.getJSONObject("vatDeliveryAddress");

                        // vat delivery address
                        if (sourceCustomerAccountVatDeliveryAddress.optString("building") != null){
                            vatDeliveryAddress.setBuilding(sourceCustomerAccountVatDeliveryAddress.getString("building"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.optString("country") != null){
                            vatDeliveryAddress.setCountry(sourceCustomerAccountVatDeliveryAddress.getString("country"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.optString("houseNumber") != null){
                            vatDeliveryAddress.setHouseNumber(sourceCustomerAccountVatDeliveryAddress.getString("houseNumber"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.optString("khetAmphur") != null){
                            vatDeliveryAddress.setKhetAmphur(sourceCustomerAccountVatDeliveryAddress.getString("khetAmphur"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.optString("kwangTambon") != null){
                            vatDeliveryAddress.setKwangTambon(sourceCustomerAccountVatDeliveryAddress.getString("kwangTambon"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.optString("moo") != null){
                            vatDeliveryAddress.setMoo(sourceCustomerAccountVatDeliveryAddress.getString("moo"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.optString("postCode") != null){
                            vatDeliveryAddress.setPostCode(sourceCustomerAccountVatDeliveryAddress.getString("postCode"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.optString("province") != null){
                            vatDeliveryAddress.setProvince(sourceCustomerAccountVatDeliveryAddress.getString("province"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.optString("road") != null){
                            vatDeliveryAddress.setRoad(sourceCustomerAccountVatDeliveryAddress.getString("road"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.optString("troksoi") != null){
                            vatDeliveryAddress.setTroksoi(sourceCustomerAccountVatDeliveryAddress.getString("troksoi"));
                        }

                        if (sourceCustomerAccountVatDeliveryAddress.optString("village") != null){
                            vatDeliveryAddress.setVillage(sourceCustomerAccountVatDeliveryAddress.getString("village"));
                        }
                    }

                    // billing account
                    if (inputSourceCustomerAccount.optString("existingFlag") != null){
                        billingAccount.setExistingFlag(inputSourceCustomerAccount.getBoolean("existingFlag"));
                    }

                    if (inputSourceCustomerAccount.optString("billingAccountId") != null){
                        billingAccount.setBillingAccountId(inputSourceCustomerAccount.getString("billingAccountId")); // must validate
                    }

                    if (inputSourceCustomerAccount.optString("paymentProfile") != null){
                        billingAccount.setPaymentProfile(inputSourceCustomerAccount.getString("paymentProfile")); // must validate
                    }

                }

                
                if (sourceCustomerAccountBillingAccount != null){
                    if (sourceCustomerAccountBillingAccount.optString("billingInfo") != null){
                        sourceCustomerAccountBillingAccountBillingInfo = sourceCustomerAccountBillingAccount.getJSONObject("billingInfo");

                        // billing info
                        if (sourceCustomerAccountBillingAccountBillingInfo.optString("collectionUnit") != null){
                            billingInfo.setCollectionUnit(sourceCustomerAccountBillingAccountBillingInfo.getString("collectionUnit"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.optString("vat") != null){
                            billingInfo.setVat(sourceCustomerAccountBillingAccountBillingInfo.getString("vat"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.optString("billingPeriod") != null){
                            billingInfo.setBillingPeriod(sourceCustomerAccountBillingAccountBillingInfo.getString("billingPeriod"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.optString("billable") != null){
                            billingInfo.setBillable(sourceCustomerAccountBillingAccountBillingInfo.getString("billable"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.optString("billingGroup") != null){
                            billingInfo.setBillingGroup(sourceCustomerAccountBillingAccountBillingInfo.getString("billingGroup"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.optString("collectionTreatment") != null){
                            billingInfo.setCollectionTreatment(sourceCustomerAccountBillingAccountBillingInfo.getString("collectionTreatment"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.optString("dispatchMethod") != null){
                            billingInfo.setDispatchMethod(sourceCustomerAccountBillingAccountBillingInfo.getString("dispatchMethod"));
                        }

                        if (sourceCustomerAccountBillingAccountBillingInfo.optString("emailAddress") != null){
                            billingInfo.setEmailAddress(sourceCustomerAccountBillingAccountBillingInfo.getString("emailAddress"));
                        }
                    }
                }


                // billing delivery address
                if (sourceCustomerAccountBillDeliveryAddress != null){
                    if (sourceCustomerAccountBillDeliveryAddress.optString("building") != null){
                        billDeliveryAddress.setBuilding(sourceCustomerAccountBillDeliveryAddress.getString("building"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.optString("country") != null){
                        billDeliveryAddress.setCountry(sourceCustomerAccountBillDeliveryAddress.getString("country"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.optString("houseNumber") != null){
                        billDeliveryAddress.setHouseNumber(sourceCustomerAccountBillDeliveryAddress.getString("houseNumber"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.optString("khetAmphur") != null){
                        billDeliveryAddress.setKhetAmphur(sourceCustomerAccountBillDeliveryAddress.getString("khetAmphur"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.optString("kwangTambon") != null){
                        billDeliveryAddress.setKwangTambon(sourceCustomerAccountBillDeliveryAddress.getString("kwangTambon"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.optString("moo") != null){
                        billDeliveryAddress.setMoo(sourceCustomerAccountBillDeliveryAddress.getString("moo"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.optString("postCode") != null){
                        billDeliveryAddress.setPostCode(sourceCustomerAccountBillDeliveryAddress.getString("postCode"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.optString("province") != null){
                        billDeliveryAddress.setProvince(sourceCustomerAccountBillDeliveryAddress.getString("province"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.optString("road") != null){
                        billDeliveryAddress.setRoad(sourceCustomerAccountBillDeliveryAddress.getString("road"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.optString("troksoi") != null){
                        billDeliveryAddress.setTroksoi(sourceCustomerAccountBillDeliveryAddress.getString("troksoi"));
                    }

                    if (sourceCustomerAccountBillDeliveryAddress.optString("village") != null){
                        billDeliveryAddress.setVillage(sourceCustomerAccountBillDeliveryAddress.getString("village"));
                    }
                }

                // destinationCustomerAccount
                if (inputSourceCustomerAccount != null){

                    if (inputSourceCustomerAccount.optString("cardNumber") != null){
                        destinationCustomerAccount.setCardNumber(inputSourceCustomerAccount.getString("cardNumber"));
                    }

                    if (inputSourceCustomerAccount.optString("cardType") != null){
                        destinationCustomerAccount.setCardType(inputSourceCustomerAccount.getString("cardType"));
                    }

                    if (inputSourceCustomerAccount.optString("catEmployeeFlag") != null){
                        destinationCustomerAccount.setCatEmployeeFlag(inputSourceCustomerAccount.getString("catEmployeeFlag"));
                    }

                    if (inputSourceCustomerAccount.optString("companyBranchId") != null){
                        destinationCustomerAccount.setCompanyBranchId(inputSourceCustomerAccount.getString("companyBranchId"));
                    }

                    if (inputSourceCustomerAccount.optString("companyName") != null){
                        destinationCustomerAccount.setCompanyName(inputSourceCustomerAccount.getString("companyName"));
                    }

                    if (inputSourceCustomerAccount.optString("companyType") != null){
                        destinationCustomerAccount.setCompanyType(inputSourceCustomerAccount.getString("companyType"));
                    }

                    if (inputSourceCustomerAccount.optString("contactNumber") != null){
                        destinationCustomerAccount.setContactNumber(inputSourceCustomerAccount.getString("contactNumber"));
                    }

                    if (inputSourceCustomerAccount.optString("custAccountId") != null){
                        destinationCustomerAccount.setCustAccountId(inputSourceCustomerAccount.getString("custAccountId"));
                    }

                    if (inputSourceCustomerAccount.optString("customerFocus") != null){
                        destinationCustomerAccount.setCustomerFocus(inputSourceCustomerAccount.getString("customerFocus"));
                    }

                    if (inputSourceCustomerAccount.optString("customerGroup") != null){
                        destinationCustomerAccount.setCustomerGroup(inputSourceCustomerAccount.getString("customerGroup"));
                    }

                    if (inputSourceCustomerAccount.optString("customerId") != null){
                        destinationCustomerAccount.setCustomerId(inputSourceCustomerAccount.getString("customerId"));
                    }

                    if (inputSourceCustomerAccount.optString("customerInfoType") != null){
                        destinationCustomerAccount.setCustomerInfoType(inputSourceCustomerAccount.getString("customerInfoType"));
                    }

                    if (inputSourceCustomerAccount.optString("customerSegment") != null){
                        destinationCustomerAccount.setCustomerSegment(inputSourceCustomerAccount.getString("customerSegment"));
                    }

                    if (inputSourceCustomerAccount.optString("customerType") != null){
                        destinationCustomerAccount.setCustomerType(inputSourceCustomerAccount.getString("customerType"));
                    }

                    if (inputSourceCustomerAccount.optString("dob") != null){
                        destinationCustomerAccount.setDob(inputSourceCustomerAccount.getString("dob"));
                    }

                    if (inputSourceCustomerAccount.optString("documentNumber") != null){
                        destinationCustomerAccount.setDocumentNumber(inputSourceCustomerAccount.getString("documentNumber"));
                    }

                    if (inputSourceCustomerAccount.optString("documentType") != null){
                        destinationCustomerAccount.setDocumentType(inputSourceCustomerAccount.getString("documentType"));
                    }

                    if (inputSourceCustomerAccount.optString("emailAddress") != null){
                        destinationCustomerAccount.setEmailAddress(inputSourceCustomerAccount.getString("emailAddress"));
                    }

                    if (inputSourceCustomerAccount.optString("existingFlag") != null){
                        destinationCustomerAccount.setExistingFlag(inputSourceCustomerAccount.getBoolean("existingFlag"));
                    }

                    if (inputSourceCustomerAccount.optString("firstName") != null){
                        destinationCustomerAccount.setFirstName(inputSourceCustomerAccount.getString("firstName"));
                    }

                    if (inputSourceCustomerAccount.optString("gender") != null){
                        destinationCustomerAccount.setGender(inputSourceCustomerAccount.getString("gender"));
                    }

                    if (inputSourceCustomerAccount.optString("ivrLanguage") != null){
                        destinationCustomerAccount.setIvrLanguage(inputSourceCustomerAccount.getString("ivrLanguage"));
                    }

                    if (inputSourceCustomerAccount.optString("lastName") != null){
                        destinationCustomerAccount.setLastName(inputSourceCustomerAccount.getString("lastName"));
                    }

                    if (inputSourceCustomerAccount.optString("nationality") != null){
                        destinationCustomerAccount.setNationality(inputSourceCustomerAccount.getString("nationality"));
                    }

                    if (inputSourceCustomerAccount.optString("taxRegisterNumber") != null){
                        destinationCustomerAccount.setTaxRegisterNumber(inputSourceCustomerAccount.getString("taxRegisterNumber"));
                    }

                    if (inputSourceCustomerAccount.optString("title") != null){
                        destinationCustomerAccount.setTitle(inputSourceCustomerAccount.getString("title"));
                    }

                    if (inputSourceCustomerAccount.optString("writtenLanguage") != null){
                        destinationCustomerAccount.setWrittenLanguage(inputSourceCustomerAccount.getString("writtenLanguage"));
                    }
                }
                /*
                *  TopUp
                */
                if (inputData.optString("topUp") != null){
                    JSONObject inputTopUp = inputData.getJSONObject("topUp");

                    if (inputData.optString("serialNumber") != null){
                        topUp.setSerialNumber(inputTopUp.getString("serialNumber"));
                    }

                    if (inputData.optString("topupType") != null){
                        topUp.setTopupType(inputTopUp.getString("topupType"));
                    }

                    if (inputData.optString("rechargeAmount") != null){
                        topUp.setRechargeAmount(inputTopUp.getInt("rechargeAmount"));
                    }

                    if (inputData.optString("currencyId") != null){
                        topUp.setCurrencyId(inputTopUp.getInt("currencyId"));
                    }

                    if (inputData.optString("channelId") != null){
                        topUp.setChannelId(inputTopUp.getInt("channelId"));
                    }
                }

                /*
                *  Credit Limit
                */
                JSONObject inputCreditLimit = inputData.getJSONObject("creditLimit");
                creditLimit.setType(inputCreditLimit.getString("type"));
                creditLimit.setValue(inputCreditLimit.getString("value"));
                creditLimit.setActionType(inputCreditLimit.getString("actionType"));

                

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
                sourceSimInfo.setImsi("query from db"); // your code here query
                sourceSimInfo.setSimType(inputSourceSimInfo.getString("simType"));
                sourceSimInfo.setFrequency("query from db"); // your code here query
                sourceSimInfoList.add(sourceSimInfo);

                // Source sim info
                List<DestinationSimInfo> destinationSimInfoList = new ArrayList<DestinationSimInfo>();
                DestinationSimInfo destinationSimInfo = new DestinationSimInfo();
                
                destinationSimInfo.setIccid(inputDestinationSimInfo.getString("iccid"));
                destinationSimInfo.setIccid("query from db"); // your code here query
                destinationSimInfo.setSimType(inputDestinationSimInfo.getString("simType"));
                destinationSimInfo.setFrequency("query from db"); // your code here query
                

                destinationSubscriberInfo.setSourceSimInfo(sourceSimInfoList);
                destinationSubscriberInfo.setTouristSimFlag(inputDestinationSimInfo.getString("itouristSimFlag"));
                destinationSubscriberInfo.setSubscriberNumber(inputDestinationSimInfo.getString("subscriberNumber"));



                // Varieties service
                VarietyService varietyService = new VarietyService();
                varietyService.setVarietyType(orderItem.getString("varietyServices"));
                varietyService.setEnabledFlag(orderItem.getString("enabledFlag"));

                // Balance transfer info
                JSONObject orderItemBalanceTransferInfo = orderItem.getJSONObject("balanceTransferInfo");
                BalanceTransferInfo balanceTransferInfo = new BalanceTransferInfo();
                balanceTransferInfo.setTransferTotalFlag(orderItemBalanceTransferInfo.getString("transferTotalFlag"));
                balanceTransferInfo.setTransferType(orderItemBalanceTransferInfo.getString("transferType"));
                balanceTransferInfo.setTransferAmount(orderItemBalanceTransferInfo.getString("transferAmount"));

                // ExtendExpireInfo
                JSONObject orderItemExtendExpireInfo = orderItem.getJSONObject("extendExpireInfo");
                ExtendExpireInfo extendExpireInfo = new ExtendExpireInfo();
                extendExpireInfo.setBalanceAmount(orderItemExtendExpireInfo.getString("extendedDay"));
                extendExpireInfo.setExtendedDay(orderItemExtendExpireInfo.getString("transAmount"));

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
