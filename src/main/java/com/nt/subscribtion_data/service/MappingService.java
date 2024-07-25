package com.nt.subscribtion_data.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogFile;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.subscribtion_data.component.CacheUpdater;
import com.nt.subscribtion_data.entity.OrderTypeEntity;
import com.nt.subscribtion_data.entity.SaChannelConEntity;
import com.nt.subscribtion_data.entity.TriggerMessageEntity;
import com.nt.subscribtion_data.log.LogFlie;
import com.nt.subscribtion_data.model.dao.CATMFE.OfferingSpecClientResp;
import com.nt.subscribtion_data.model.dao.CATMFE.OfferingSpecData;
import com.nt.subscribtion_data.model.dao.DataModel.Data;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.Address;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.DestinationCustomerAccount;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.SourceCustomerAccount;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventData;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.SaleInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.BillingAccount.BillDeliveryAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.BillingAccount.BillingAccount;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.BillingAccount.BillingAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.BillingAccount.BillingInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.BillingAccount.VatAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.BillingAccount.VatDeliveryAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.BalanceTransferInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.ContractInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.CreditLimit;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.EventItem;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.Expire;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.ExtendExpireInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.Offer;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.Photo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.SouthernContactAddress;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.SubPropertyCode;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.TopUp;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.VarietyService;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationSubscriberInfo.EvDestinationSimInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.EventItem.DestinationSubscriberInfo.EvDestinationSubscriberInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.SubscriberInfo.DestinationSimInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.SubscriberInfo.SourceSimInfo;
import com.nt.subscribtion_data.model.dao.DataModel.EventData.SubscriberInfo.SubscriberInfo;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMappingClientResp;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMappingData;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.IMSIOfferingConfig;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.ListIMSIOfferingConfigClientResp;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.OrderHeaderClientResp;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.OrderHeaderData;
import com.nt.subscribtion_data.model.dao.OMUSER.TransManageContractDTLClientResp;
import com.nt.subscribtion_data.model.dao.OMUSER.TransManageContractDTLData;
import com.nt.subscribtion_data.model.dao.OMUSER.TransNumberDTLClientResp;
import com.nt.subscribtion_data.model.dao.OMUSER.TransNumberDTLData;
import com.nt.subscribtion_data.model.dto.ReceiveExpiredDataType;
import com.nt.subscribtion_data.model.dto.ReceiveOMDataType;
import com.nt.subscribtion_data.model.dto.ReceiveTopUpDataType;
import com.nt.subscribtion_data.service.database.CATMFEService;
import com.nt.subscribtion_data.service.database.INVUSERService;
import com.nt.subscribtion_data.service.database.OMMYFRONTService;
import com.nt.subscribtion_data.service.database.OMUSERService;
import com.nt.subscribtion_data.util.Constant;
import com.nt.subscribtion_data.util.DateTime;

@Service
public class MappingService {
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

    @Autowired
    private CacheUpdater cacheUpdater;

    // @Autowired
    private ExpireTriggerMessageService expireTriggerMessageService;

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    public Data doOrderDataType(String message, Boolean isSaveDataModel) throws SQLException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveOMDataType receivedData = objectMapper.readValue(message, ReceiveOMDataType.class);
        String orderType = getOrderTypeNamePattern(receivedData.getOrderType().toUpperCase());
        switch (orderType) {
            case "CONTRACTMANAGEMENT":
                return processContractManagementType(orderType, message, receivedData, isSaveDataModel);
            case "VARIETYSERVICE":
                return processVarietyServiceServiceType(orderType, message, receivedData, isSaveDataModel);
            default:
                return processDefaultType(orderType, message, receivedData, isSaveDataModel);
        }
    }

    public Data processVarietyServiceServiceType(String orderType, String message, ReceiveOMDataType receivedData, Boolean isSaveDataModel) throws SQLException, IOException {
       // Process for new_order_type
       Data sendData = null;
       Timestamp receiveDataTimestamp = DateTime.getTimestampNowUTC();
       String publishChannelType = String.format("OM-%s", receivedData.getChannel().toUpperCase());
       String channelConnectName = Constant.OM_CHANNEL_CONNECT;

       List<OrderTypeEntity> orderTypes = cacheUpdater.getOrderTypeListCache();
       if (orderTypes == null){
           orderTypes = distributeService.LisOrderTypes();
       }

       OrderTypeEntity orderTypeInfo = getOrderTypeInfoFromList(orderType, orderTypes);
       if(orderTypeInfo == null){
           // No save log if ordertype not found
           return null;
       }else{
           if(orderTypeInfo.getIs_Enable().equals(0)){
               // No save log if ordertype is closed
               return null;
           }
       }

       List<SaChannelConEntity> saChannels = cacheUpdater.getSaChannelConnectListCache();
       if (saChannels == null){
           saChannels = distributeService.ListChannelConnect();
       }
       
       SaChannelConEntity saChannelConInfo = getSaChannelInfoFromList(channelConnectName, saChannels);
       if(saChannelConInfo == null){
           TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
           triggerMsg.setMESSAGE_IN(message);
           triggerMsg.setOrderType_Name(orderType);
           triggerMsg.setORDERID(receivedData.getOrderId());
           triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
           triggerMsg.setIS_STATUS(0);
           triggerMsg.setREMARK("NOT FOUND saChannelConnect : "+ channelConnectName);
           distributeService.CreateTriggerMessage(triggerMsg);
           return null;
       }

       try{
           
           OrderHeaderClientResp odheaderResp = ommyfrontService.getOrderHeaderDataByOrderID(receivedData.getOrderId());


           if (odheaderResp.getData() == null){
               String orderHeaderErr = "NOT FOUND OrderHeader data";
               TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
               triggerMsg.setMESSAGE_IN(message);
               triggerMsg.setOrderType_Name(orderType);
               triggerMsg.setORDERID(receivedData.getOrderId());
               triggerMsg.setOrderType_id(orderTypeInfo.getID());
               triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
               triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
               triggerMsg.setIS_STATUS(0);
               triggerMsg.setREMARK(orderHeaderErr);
               if (odheaderResp.getErr() != null){
                   triggerMsg.setREMARK(String.format("%s cause %s", orderHeaderErr, odheaderResp.getErr()));
               }
               distributeService.CreateTriggerMessage(triggerMsg);
               return null;
           }else{
               OrderHeaderData odheader = odheaderResp.getData();
               try{
                   sendData = MappingVarietyServiceData(odheader, orderType);
                   sendData.setOrderID(receivedData.getOrderId());
               }catch(Exception e){
                   TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                   triggerMsg.setOrderType_Name(orderType);
                   triggerMsg.setOrderType_id(orderTypeInfo.getID());
                   triggerMsg.setORDERID(receivedData.getOrderId());
                   triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                   triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                   triggerMsg.setORDERID(receivedData.getOrderId());
                   triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                   triggerMsg.setIS_STATUS(0);
                   triggerMsg.setREMARK("error execption in mapping :"+e.getMessage());
                   distributeService.CreateTriggerMessage(triggerMsg);
                   return null;
               }
               try{
                   ObjectMapper mapper = new ObjectMapper();
                   // mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
                   String jsonString = mapper.writeValueAsString(sendData);
                   if(orderTypeInfo.getIs_Enable().equals(1)){
                       // Send to kafka server
                       TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                       triggerMsg.setMESSAGE_IN(message);
                       if(isSaveDataModel){
                           triggerMsg.setDATA_MODEL(jsonString);
                       }
                       triggerMsg.setIS_STATUS(1);
                       triggerMsg.setORDERID(receivedData.getOrderId());
                       triggerMsg.setOrderType_Name(sendData.getOrderType());
                       triggerMsg.setOrderType_id(orderTypeInfo.getID());
                       triggerMsg.setPHONENUMBER(sendData.getMsisdn());
                       triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                       triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                       triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                       triggerMsg.setSEND_DATE(DateTime.getTimestampNowUTC());
                       distributeService.CreateTriggerMessage(triggerMsg);

                       // System log
                       LogFlie.logMessage(
                        "subscribtion_data",
                        String.format("trigger/%s/%s",LogFlie.dateFolderName(), orderType),
                        sendData
                    );

                        // System expired log
                        LogFlie.logMessage(
                            "subscribtion_data",
                            String.format("message-expire/%s/%s",LogFlie.dateFolderName(), orderType),
                            sendData
                        );      

                       return sendData;
                   }else{
                       return null;
                   }
               } catch(Exception e){
                   TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                   triggerMsg.setOrderType_Name(orderType);
                   triggerMsg.setOrderType_id(orderTypeInfo.getID());
                   triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                   triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                   triggerMsg.setORDERID(receivedData.getOrderId());
                   triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                   triggerMsg.setIS_STATUS(0);
                   triggerMsg.setREMARK("error execption in objectmapper :"+e.getMessage());
                   distributeService.CreateTriggerMessage(triggerMsg);
                   return null;
               }
           }
       }catch (Exception e){
           String errClob = "";
           TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
           try{
               triggerMsg.setMESSAGE_IN(message);
           } catch (Exception clobE){
               clobE.printStackTrace();
               errClob = clobE.getMessage();
           }
           triggerMsg.setOrderType_Name(orderType);
           // triggerMsg.setOrderType_id(orderTypeInfo.getID());
           triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
           triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
           triggerMsg.setORDERID(receivedData.getOrderId());
           triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
           triggerMsg.setIS_STATUS(0);
           triggerMsg.setREMARK("error execption :"+e.getMessage()+" cloberr:"+errClob);
           distributeService.CreateTriggerMessage(triggerMsg);
           return null;
       }
    }

    public Data processContractManagementType(String orderType, String message, ReceiveOMDataType receivedData, Boolean isSaveDataModel) throws SQLException, IOException {
          // Process for new_order_type
          Data sendData = null;
          Timestamp receiveDataTimestamp = DateTime.getTimestampNowUTC();
          String publishChannelType = String.format("OM-%s", receivedData.getChannel().toUpperCase());
          String channelConnectName = Constant.OM_CHANNEL_CONNECT;
  
          List<OrderTypeEntity> orderTypes = cacheUpdater.getOrderTypeListCache();
          if (orderTypes == null){
              orderTypes = distributeService.LisOrderTypes();
          }
  
          OrderTypeEntity orderTypeInfo = getOrderTypeInfoFromList(orderType, orderTypes);
          if(orderTypeInfo == null){
              // No save log if ordertype not found
              return null;
          }else{
              if(orderTypeInfo.getIs_Enable().equals(0)){
                  // No save log if ordertype is closed
                  return null;
              }
          }
  
          List<SaChannelConEntity> saChannels = cacheUpdater.getSaChannelConnectListCache();
          if (saChannels == null){
              saChannels = distributeService.ListChannelConnect();
          }
          
          SaChannelConEntity saChannelConInfo = getSaChannelInfoFromList(channelConnectName, saChannels);
          if(saChannelConInfo == null){
              TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
              triggerMsg.setMESSAGE_IN(message);
              triggerMsg.setOrderType_Name(orderType);
              triggerMsg.setORDERID(receivedData.getOrderId());
              triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
              triggerMsg.setIS_STATUS(0);
              triggerMsg.setREMARK("NOT FOUND saChannelConnect : "+ channelConnectName);
              distributeService.CreateTriggerMessage(triggerMsg);
              return null;
          }
  
          try{
                TransManageContractDTLClientResp tMCDTLResp=null;
                try{
                    String transMasterID = receivedData.getOrderId();
                    tMCDTLResp = omuserService.getTransManageContractDTLData(transMasterID);
                
                }catch (Exception e){
                    throw new Exception("TransManageContractDTLClientResp mapping error: " + e.getMessage());
                }
                if (tMCDTLResp.getData() == null){
                    String orderHeaderErr = "NOT FOUND TransManageContractDTL data";
                    TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                    triggerMsg.setMESSAGE_IN(message);
                    triggerMsg.setOrderType_Name(orderType);
                    triggerMsg.setORDERID(receivedData.getOrderId());
                    triggerMsg.setOrderType_id(orderTypeInfo.getID());
                    triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                    triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                    triggerMsg.setIS_STATUS(0);
                    triggerMsg.setREMARK(orderHeaderErr);
                    if (tMCDTLResp.getErr() != null){
                        triggerMsg.setREMARK(String.format("%s cause %s", orderHeaderErr, tMCDTLResp.getErr()));
                    }
                    distributeService.CreateTriggerMessage(triggerMsg);
                    return null;
                }else{
                    TransManageContractDTLData tMCDTLData = tMCDTLResp.getData();
                  try{
                      sendData = MappingContractManagementData(tMCDTLData, orderType);
                      sendData.setOrderID(receivedData.getOrderId());
                  }catch(Exception e){
                      TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                      triggerMsg.setOrderType_Name(orderType);
                      triggerMsg.setOrderType_id(orderTypeInfo.getID());
                      triggerMsg.setORDERID(receivedData.getOrderId());
                      triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                      triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                      triggerMsg.setORDERID(receivedData.getOrderId());
                      triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                      triggerMsg.setIS_STATUS(0);
                      triggerMsg.setREMARK("error execption in mapping :"+e.getMessage());
                      distributeService.CreateTriggerMessage(triggerMsg);
                      return null;
                  }
                  try{
                      ObjectMapper mapper = new ObjectMapper();
                      // mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
                      String jsonString = mapper.writeValueAsString(sendData);
                      if(orderTypeInfo.getIs_Enable().equals(1)){
                          // Send to kafka server
                          TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                          triggerMsg.setMESSAGE_IN(message);
                          if(isSaveDataModel){
                              triggerMsg.setDATA_MODEL(jsonString);
                          }
                          triggerMsg.setIS_STATUS(1);
                          triggerMsg.setORDERID(receivedData.getOrderId());
                          triggerMsg.setOrderType_Name(sendData.getOrderType());
                          triggerMsg.setOrderType_id(orderTypeInfo.getID());
                          triggerMsg.setPHONENUMBER(sendData.getMsisdn());
                          triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                          triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                          triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                          triggerMsg.setSEND_DATE(DateTime.getTimestampNowUTC());
                          distributeService.CreateTriggerMessage(triggerMsg);

                            // System log
                            LogFlie.logMessage(
                                "subscribtion_data", 
                                String.format("trigger/%s/%s",LogFlie.dateFolderName(), orderType),
                                sendData
                            );

                            // System expired log
                            LogFlie.logMessage(
                                "subscribtion_data",
                                String.format("message-expire/%s/%s",LogFlie.dateFolderName(), orderType),
                                sendData
                            );
  
                          return sendData;
                      }else{
                          return null;
                      }
                  } catch(Exception e){
                      TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                      triggerMsg.setOrderType_Name(orderType);
                      triggerMsg.setOrderType_id(orderTypeInfo.getID());
                      triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                      triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                      triggerMsg.setORDERID(receivedData.getOrderId());
                      triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                      triggerMsg.setIS_STATUS(0);
                      triggerMsg.setREMARK("error execption in objectmapper :"+e.getMessage());
                      distributeService.CreateTriggerMessage(triggerMsg);
                      return null;
                  }
              }
          }catch (Exception e){
              String errClob = "";
              TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
              try{
                  triggerMsg.setMESSAGE_IN(message);
              } catch (Exception clobE){
                  clobE.printStackTrace();
                  errClob = clobE.getMessage();
              }
              triggerMsg.setOrderType_Name(orderType);
              // triggerMsg.setOrderType_id(orderTypeInfo.getID());
              triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
              triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
              triggerMsg.setORDERID(receivedData.getOrderId());
              triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
              triggerMsg.setIS_STATUS(0);
              triggerMsg.setREMARK("error execption :"+e.getMessage()+" cloberr:"+errClob);
              distributeService.CreateTriggerMessage(triggerMsg);
              return null;
          }
      }

    public Data processDefaultType(String orderType, String message, ReceiveOMDataType receivedData, Boolean isSaveDataModel) throws SQLException, IOException {
        // Process for new_order_type
        Data sendData = null;
        Timestamp receiveDataTimestamp = DateTime.getTimestampNowUTC();
        String publishChannelType = String.format("OM-%s", receivedData.getChannel().toUpperCase());
        String channelConnectName = Constant.OM_CHANNEL_CONNECT;

        List<OrderTypeEntity> orderTypes = cacheUpdater.getOrderTypeListCache();
        if (orderTypes == null){
            orderTypes = distributeService.LisOrderTypes();
        }

        OrderTypeEntity orderTypeInfo = getOrderTypeInfoFromList(orderType, orderTypes);
        if(orderTypeInfo == null){
            // No save log if ordertype not found
            return null;
        }else{
            if(orderTypeInfo.getIs_Enable().equals(0)){
                // No save log if ordertype is closed
                return null;
            }
        }

        List<SaChannelConEntity> saChannels = cacheUpdater.getSaChannelConnectListCache();
        if (saChannels == null){
            saChannels = distributeService.ListChannelConnect();
        }
        
        SaChannelConEntity saChannelConInfo = getSaChannelInfoFromList(channelConnectName, saChannels);
        if(saChannelConInfo == null){
            TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
            triggerMsg.setMESSAGE_IN(message);
            triggerMsg.setOrderType_Name(orderType);
            triggerMsg.setORDERID(receivedData.getOrderId());
            triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
            triggerMsg.setIS_STATUS(0);
            triggerMsg.setREMARK("NOT FOUND saChannelConnect : "+ channelConnectName);
            distributeService.CreateTriggerMessage(triggerMsg);
            return null;
        }

        try{
            
            OrderHeaderClientResp odheaderResp = ommyfrontService.getOrderHeaderDataByOrderID(receivedData.getOrderId());


            if (odheaderResp.getData() == null){
                String orderHeaderErr = "NOT FOUND OrderHeader data";
                TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                triggerMsg.setMESSAGE_IN(message);
                triggerMsg.setOrderType_Name(orderType);
                triggerMsg.setORDERID(receivedData.getOrderId());
                triggerMsg.setOrderType_id(orderTypeInfo.getID());
                triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                triggerMsg.setIS_STATUS(0);
                triggerMsg.setREMARK(orderHeaderErr);
                if (odheaderResp.getErr() != null){
                    triggerMsg.setREMARK(String.format("%s cause %s", orderHeaderErr, odheaderResp.getErr()));
                }
                distributeService.CreateTriggerMessage(triggerMsg);
                return null;
            }else{
                OrderHeaderData odheader = odheaderResp.getData();
                try{
                    sendData = MappingDefaultData(odheader, orderType);
                    sendData.setOrderID(receivedData.getOrderId());
                }catch(Exception e){
                    TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                    triggerMsg.setOrderType_Name(orderType);
                    triggerMsg.setOrderType_id(orderTypeInfo.getID());
                    triggerMsg.setORDERID(receivedData.getOrderId());
                    triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                    triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                    triggerMsg.setORDERID(receivedData.getOrderId());
                    triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                    triggerMsg.setIS_STATUS(0);
                    triggerMsg.setREMARK("error execption in mapping :"+e.getMessage());
                    distributeService.CreateTriggerMessage(triggerMsg);
                    return null;
                }
                try{
                    ObjectMapper mapper = new ObjectMapper();
                    // mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
                    String jsonString = mapper.writeValueAsString(sendData);
                    if(orderTypeInfo.getIs_Enable().equals(1)){
                        // Send to kafka server
                        TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                        triggerMsg.setMESSAGE_IN(message);
                        if(isSaveDataModel){
                            triggerMsg.setDATA_MODEL(jsonString);
                        }
                        triggerMsg.setIS_STATUS(1);
                        triggerMsg.setORDERID(receivedData.getOrderId());
                        triggerMsg.setOrderType_Name(sendData.getOrderType());
                        triggerMsg.setOrderType_id(orderTypeInfo.getID());
                        triggerMsg.setPHONENUMBER(sendData.getMsisdn());
                        triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                        triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                        triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                        triggerMsg.setSEND_DATE(DateTime.getTimestampNowUTC());
                        distributeService.CreateTriggerMessage(triggerMsg);

                        // System log
                        LogFlie.logMessage(
                            "subscribtion_data", 
                            String.format("trigger/%s/%s",LogFlie.dateFolderName(), orderType),
                            sendData
                        );

                        // System expired log
                        LogFlie.logMessage(
                            "subscribtion_data",
                            String.format("message-expire/%s/%s",LogFlie.dateFolderName(), orderType),
                            sendData
                        );

                        return sendData;
                    }else{
                        return null;
                    }
                } catch(Exception e){
                    TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                    triggerMsg.setOrderType_Name(orderType);
                    triggerMsg.setOrderType_id(orderTypeInfo.getID());
                    triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                    triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                    triggerMsg.setORDERID(receivedData.getOrderId());
                    triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                    triggerMsg.setIS_STATUS(0);
                    triggerMsg.setREMARK("error execption in objectmapper :"+e.getMessage());
                    distributeService.CreateTriggerMessage(triggerMsg);
                    return null;
                }
            }
        }catch (Exception e){
            String errClob = "";
            TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
            try{
                triggerMsg.setMESSAGE_IN(message);
            } catch (Exception clobE){
                clobE.printStackTrace();
                errClob = clobE.getMessage();
            }
            triggerMsg.setOrderType_Name(orderType);
            // triggerMsg.setOrderType_id(orderTypeInfo.getID());
            triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
            triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
            triggerMsg.setORDERID(receivedData.getOrderId());
            triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
            triggerMsg.setIS_STATUS(0);
            triggerMsg.setREMARK("error execption :"+e.getMessage()+" cloberr:"+errClob);
            distributeService.CreateTriggerMessage(triggerMsg);
            return null;
        }
    }

    public Data processTopUpType(String message, Boolean isSaveDataModel) throws SQLException, IOException {
        // Process for new_order_type
        Timestamp receiveDataTimestamp = DateTime.getTimestampNowUTC();
        Data sendData = null;

        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveTopUpDataType receivedData = objectMapper.readValue(message, ReceiveTopUpDataType.class);
        String orderTypeName = "TOPUPRECHARGE";
        String channelConnectType = "TOPUP";
        String publishChannelType = "Topup-GW";
        List<OrderTypeEntity> orderTypes = cacheUpdater.getOrderTypeListCache();
        if (orderTypes == null){
            orderTypes = distributeService.LisOrderTypes();
        }

        OrderTypeEntity orderTypeInfo = getOrderTypeInfoFromList(orderTypeName, orderTypes);
        if(orderTypeInfo == null){
            // No save log if ordertype not found
            return null;
        }else{
            if(orderTypeInfo.getIs_Enable().equals(0)){
                // No save log if ordertype is closed
                return null;
            }
        }

        List<SaChannelConEntity> saChannels = cacheUpdater.getSaChannelConnectListCache();
        if (saChannels == null){
            saChannels = distributeService.ListChannelConnect();
        } 
        
        SaChannelConEntity saChannelConInfo = getSaChannelInfoFromList(channelConnectType, saChannels);
        if(saChannelConInfo == null){
            TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
            triggerMsg.setMESSAGE_IN(message);
            triggerMsg.setOrderType_Name(orderTypeName);
            triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
            triggerMsg.setIS_STATUS(0);
            triggerMsg.setREMARK("NOT FOUND saChannelConnect "+channelConnectType);
            distributeService.CreateTriggerMessage(triggerMsg);
            return null;
        }


        try{
            
            // Mapping DataType
            sendData = MappingTopUpData(receivedData, orderTypeName);
            sendData.setOrderID(receivedData.getNotiMsgSeq());
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(sendData);

            if (orderTypeInfo.getIs_Enable().equals(1)){
                // Send to kafka server
                TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                triggerMsg.setMESSAGE_IN(message);
                if(isSaveDataModel){
                    triggerMsg.setDATA_MODEL(jsonString);
                }
                triggerMsg.setIS_STATUS(1);
                triggerMsg.setOrderType_Name(orderTypeName);
                triggerMsg.setOrderType_id(orderTypeInfo.getID());
                triggerMsg.setPHONENUMBER(String.format("0%s",receivedData.getMsisdn()));
                triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                triggerMsg.setORDERID(receivedData.getNotiMsgSeq());
                triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                triggerMsg.setSEND_DATE(DateTime.getTimestampNowUTC());
                distributeService.CreateTriggerMessage(triggerMsg);

                // System log
                LogFlie.logMessage(
                "subscribtion_data", 
                String.format("trigger/%s/%s",LogFlie.dateFolderName(), orderTypeName),
                sendData
                );

                // System expired log
                LogFlie.logMessage(
                    "subscribtion_data",
                    String.format("message-expire/%s/%s",LogFlie.dateFolderName(), orderTypeName),
                    sendData
                );

                return sendData;
            }else{
                // UnSend to kafka server
                TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                triggerMsg.setMESSAGE_IN(message);
                if(isSaveDataModel){
                    triggerMsg.setDATA_MODEL(jsonString);
                }
                triggerMsg.setIS_STATUS(0);
                triggerMsg.setOrderType_Name(orderTypeName);
                triggerMsg.setOrderType_id(orderTypeInfo.getID());
                triggerMsg.setORDERID(receivedData.getNotiMsgSeq());
                triggerMsg.setPHONENUMBER(String.format("0%s",receivedData.getMsisdn()));
                triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                distributeService.CreateTriggerMessage(triggerMsg);

                return null;
            }
        }catch (Exception e){
            TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
            triggerMsg.setMESSAGE_IN(message);
            triggerMsg.setOrderType_Name(orderTypeName);
            triggerMsg.setPHONENUMBER(String.format("0%s",receivedData.getMsisdn()));
            triggerMsg.setOrderType_id(orderTypeInfo.getID());
            triggerMsg.setREMARK(e.getMessage());
            triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
            triggerMsg.setORDERID(receivedData.getNotiMsgSeq());
            triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
            triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
            triggerMsg.setIS_STATUS(0);
            distributeService.CreateTriggerMessage(triggerMsg);
            return null;
        }
    }

    public Data processExpiredType(String message, Boolean isSaveDataModel) throws SQLException, IOException {
        // Process for new_order_type
        Timestamp receiveDataTimestamp = DateTime.getTimestampNowUTC();
        Data sendData = null;

        ObjectMapper objectMapper = new ObjectMapper();
        ReceiveExpiredDataType receivedData = objectMapper.readValue(message, ReceiveExpiredDataType.class);
        String orderTypeName = "PACKAGEEXPIRE";
        String channelType = "Expire";
        String publishChannelType = "PackageExpireEX";
        List<OrderTypeEntity> orderTypes = cacheUpdater.getOrderTypeListCache();
        if (orderTypes == null){
            orderTypes = distributeService.LisOrderTypes();
        }

        OrderTypeEntity orderTypeInfo = getOrderTypeInfoFromList(orderTypeName, orderTypes);
        if(orderTypeInfo == null){
            // No save log if ordertype not found
            return null;
        }else{
            if(orderTypeInfo.getIs_Enable().equals(0)){
                // No save log if ordertype is closed
                return null;
            }
        }

        List<SaChannelConEntity> saChannels = cacheUpdater.getSaChannelConnectListCache();
        if (saChannels == null){
            saChannels = distributeService.ListChannelConnect();
        } 
        
        SaChannelConEntity saChannelConInfo = getSaChannelInfoFromList(channelType, saChannels);
        try{
            // Mapping DataType
            sendData = MappingExpiredData(receivedData, orderTypeName);

            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(sendData);
            
            if (orderTypeInfo.getIs_Enable().equals(1)){
                // Send to kafka server
                TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                triggerMsg.setMESSAGE_IN(message);
                if(isSaveDataModel){
                    triggerMsg.setDATA_MODEL(jsonString);
                }
                triggerMsg.setIS_STATUS(1);
                triggerMsg.setORDERID(receivedData.getNotiMsgSeq());
                triggerMsg.setOrderType_Name(orderTypeName);
                triggerMsg.setOrderType_id(orderTypeInfo.getID());
                triggerMsg.setPHONENUMBER(String.format("0%s",receivedData.getMsisdn()));
                triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                triggerMsg.setSEND_DATE(DateTime.getTimestampNowUTC());
                distributeService.CreateTriggerMessage(triggerMsg);

                // System log
                LogFlie.logMessage(
                "subscribtion_data", 
                String.format("trigger/%s/%s",LogFlie.dateFolderName(), orderTypeName),
                sendData
                );

                // System expired log
                LogFlie.logMessage(
                    "subscribtion_data",
                    String.format("message-expire/%s/%s",LogFlie.dateFolderName(), orderTypeName),
                    sendData
                );

                return sendData;
            }else{
                // UnSend to kafka server
                TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
                triggerMsg.setMESSAGE_IN(message);
                if(isSaveDataModel){
                    triggerMsg.setDATA_MODEL(jsonString);
                }
                triggerMsg.setIS_STATUS(0);
                triggerMsg.setORDERID(receivedData.getNotiMsgSeq());
                triggerMsg.setOrderType_Name(orderTypeName);
                triggerMsg.setOrderType_id(orderTypeInfo.getID());
                triggerMsg.setPHONENUMBER(String.format("0%s",receivedData.getMsisdn()));
                triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
                triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
                triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
                distributeService.CreateTriggerMessage(triggerMsg);

                return null;
            }
        }catch (Exception e){
            // UnSend to kafka server
            TriggerMessageEntity triggerMsg = new TriggerMessageEntity();
            triggerMsg.setMESSAGE_IN(message);
            triggerMsg.setIS_STATUS(0);
            triggerMsg.setOrderType_Name(orderTypeName);
            triggerMsg.setORDERID(receivedData.getNotiMsgSeq());
            triggerMsg.setREMARK(e.getMessage());
            triggerMsg.setPHONENUMBER(String.format("0%s",receivedData.getMsisdn()));
                
            // triggerMsg.setOrderType_id(orderTypeInfo.getID());
            triggerMsg.setPUBLISH_CHANNEL(publishChannelType);
            triggerMsg.setRECEIVE_DATE(receiveDataTimestamp);
            // triggerMsg.setSA_CHANNEL_CONNECT_ID(saChannelConInfo.getID());
            distributeService.CreateTriggerMessage(triggerMsg);

            return null;
        }
    }

    private Data MappingContractManagementData(TransManageContractDTLData tMCDTLData, String orderTypeName) throws Exception{
        String triggerDate = DateTime.getTimeStampNowStr();

        Data sendData = new Data();
        
        EventData omEv = new EventData();
        INVMappingData invMappingData =null;
        IMSIOfferingConfig imsiConfigData = null;
        ListIMSIOfferingConfigClientResp imsiOfferConfigList = null;
        try{
        
            TransNumberDTLClientResp tNumberDTL = omuserService.getTransNumberDTLData(tMCDTLData.getTransMasterId());
            if(tNumberDTL== null){
                throw new Exception("transNumberDtl not found ");
            }

            if(tNumberDTL.getErr()!= null){
                throw new Exception("transNumberDtl found error: "+tNumberDTL.getErr());
            }else{
                if(tNumberDTL.getData()== null){
                    throw new Exception("transNumberDtl not found data");
                }
                if(tNumberDTL.getData().getMsisdn() == null){
                    throw new Exception("transNumberDtl not found msisdn");
                }
            }

            try{
                String externalId = tNumberDTL.getData().getMsisdn();
                INVMappingClientResp invMappingResp = invuserService.getInvMappingData(externalId);

                if (invMappingResp != null){
                    if (invMappingResp.getData() != null && invMappingResp.getErr() == null){
                        invMappingData = invMappingResp.getData();
                        if (invMappingData == null){
                            throw new Exception("INV mapping not found external id: "+externalId);
                        }
                    }else{
                        INVMappingClientResp invMappingOnlyResp = invuserService.getInvMappingDataOnly(externalId);
                        invMappingData = invMappingOnlyResp.getData();
                        if (invMappingData == null){
                            throw new Exception("INV mapping not found external id: "+externalId);
                        }
                    }
                }else{
                    INVMappingClientResp invMappingOnlyResp = invuserService.getInvMappingDataOnly(externalId);
                    invMappingData = invMappingOnlyResp.getData();
                    if (invMappingData == null){
                        throw new Exception("INV mapping not found external id: "+externalId);
                    }
                }
            
                imsiOfferConfigList = cacheUpdater.getIMSIOfferConfigListCache();
                if (imsiOfferConfigList.getErr() == null){
                    imsiOfferConfigList = ommyfrontService.getImsiOfferingConfigList();
                    cacheUpdater.setIMSIOfferConfigListCache(imsiOfferConfigList);
                }

                if (invMappingData != null){
                    if(invMappingData.getImsi() != null){
                        imsiConfigData = getImsiConfigByImsi(invMappingData.getImsi(), imsiOfferConfigList.getData());
                    }

                }
            }catch (Exception e){
                throw new Exception("INV mapping error: " + e.getMessage());
            }

            // eventItem
            List<EventItem> evenItems = new ArrayList<>();
            try{
                
                EventItem evenItem = new EventItem();

                // contractInfo 
                if (tMCDTLData != null){
                    ContractInfo contractInfo = new ContractInfo();
                    contractInfo.setSubscrNo(tMCDTLData.getSubscrNo());
                    contractInfo.setContractId(tMCDTLData.getContractId());
                    contractInfo.setRefDocumentId(tMCDTLData.getRefDocumentId());
                    contractInfo.setContractCode(tMCDTLData.getContractCode());
                    contractInfo.setRefDocumentId(tMCDTLData.getRefDocumentId());
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
                }

                // append eventItem
                evenItems.add(evenItem);
                omEv.setEventItems(evenItems);
            }catch (Exception e){
                throw new Exception("MappingContractManagementData event item mapping contractInfo error: " + e.getMessage());
            }

            // subscriberInfo
            SubscriberInfo subscriberInfo = new SubscriberInfo();
            subscriberInfo.setMsisdn(tNumberDTL.getData().getMsisdn());

            List<SourceSimInfo> sourceSimInfos = new ArrayList<SourceSimInfo>();
            SourceSimInfo sourceSimInfo = new SourceSimInfo();
            if(tNumberDTL.getData().getIccid()!=null){
                sourceSimInfo.setIccid(tNumberDTL.getData().getIccid());
            }else{
                if(invMappingData.getSecondaryCode()!=null){
                    sourceSimInfo.setIccid(String.valueOf(invMappingData.getSecondaryCode()));
                }
            }
            sourceSimInfo.setImsi(tNumberDTL.getData().getImsi());
            if(imsiConfigData != null){
                sourceSimInfo.setFrequency(imsiConfigData.getFrequency());
            }
            sourceSimInfos.add(sourceSimInfo);
            subscriberInfo.setSourceSimInfo(sourceSimInfos);
            omEv.setSubscriberInfo(subscriberInfo);
            
            sendData.setTriggerDate(triggerDate);
            sendData.setPublishChannel("OM-MFE");
            sendData.setMsisdn(String.format("0%s",tNumberDTL.getData().getMsisdn()));
            sendData.setOrderType(orderTypeName);
            sendData.setEventData(omEv);
        }catch (Exception e){
            throw new Exception("MappingContractManagementData error: " + e.getMessage());
        }    


        return sendData;
        
    }
    
    private Data MappingDefaultData(OrderHeaderData odheader, String orderTypeName) throws Exception{
        String triggerDate = DateTime.getTimeStampNowStr();

        Data sendData = new Data();
        
        EventData omEv = new EventData();

        String externalId = odheader.getMsisdn();

        INVMappingData invMappingData =null;
        IMSIOfferingConfig imsiConfigData = null;
        ListIMSIOfferingConfigClientResp imsiOfferConfigList = null;
        try{
            INVMappingClientResp invMappingResp = invuserService.getInvMappingData(externalId);

            
            if (invMappingResp != null){
                if (invMappingResp.getData() != null && invMappingResp.getErr() == null){
                    invMappingData = invMappingResp.getData();
                    if (invMappingData == null){
                        throw new Exception("INV mapping not found external id: "+externalId);
                    }
                }else{
                    INVMappingClientResp invMappingOnlyResp = invuserService.getInvMappingDataOnly(externalId);
                    invMappingData = invMappingOnlyResp.getData();
                    if (invMappingData == null){
                        throw new Exception("INV mapping not found external id: "+externalId);
                    }
                }
            }else{
                INVMappingClientResp invMappingOnlyResp = invuserService.getInvMappingDataOnly(externalId);
                invMappingData = invMappingOnlyResp.getData();
                if (invMappingData == null){
                    throw new Exception("INV mapping not found external id: "+externalId);
                }
            }
        

            imsiOfferConfigList = cacheUpdater.getIMSIOfferConfigListCache();
            if (imsiOfferConfigList.getErr() == null){
                imsiOfferConfigList = ommyfrontService.getImsiOfferingConfigList();
                cacheUpdater.setIMSIOfferConfigListCache(imsiOfferConfigList);
            }

            
            if (invMappingData != null){
                if(invMappingData.getImsi() != null){
                    imsiConfigData = getImsiConfigByImsi(invMappingData.getImsi(), imsiOfferConfigList.getData());
                }
            }
        }catch (Exception e){
            throw new Exception("INV mapping error: " + e.getMessage());
        }

        String transMasterID ="";
        TransManageContractDTLClientResp tMCDTLResp=null;
        try{
            transMasterID = odheader.getOrderId();
            tMCDTLResp = omuserService.getTransManageContractDTLData(transMasterID);
        
        }catch (Exception e){
            throw new Exception("TransManageContractDTLClientResp mapping error: " + e.getMessage());
        }

        // Input Data
        try{
            if (odheader.getInputData() != null){
                JSONObject inputData = null;
                try{
                    inputData = new JSONObject(odheader.getInputData().toString());
                }catch (Exception e){
                    throw new Exception("get inputData mapping error: " + e.getMessage());
                }
                // System.out.println("=================================");
                try{
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
                }catch (Exception e){
                    throw new Exception("get header main mapping error: " + e.getMessage());
                }

                if (inputData.has("isProvisionRequired")){
                    omEv.setIsProvisionRequired(inputData.getBoolean("isProvisionRequired"));
                }

                if (inputData.has("rerunRevisionNumber")){
                    omEv.setRerunRevisionNumber(inputData.getInt("rerunRevisionNumber"));
                }

                /*
                *  SubscriberInfo
                */
                SubscriberInfo subscriberInfo = new SubscriberInfo();

                try{
                    if (inputData.has("subscriberInfo")){
                        JSONObject inputSubscriberInfo = inputData.getJSONObject("subscriberInfo");

                        if (inputSubscriberInfo.has("msisdn")){
                            subscriberInfo.setMsisdn(inputSubscriberInfo.getString("msisdn"));
                        }

                        if (inputSubscriberInfo.has("serviceType")){
                            subscriberInfo.setServiceType(Integer.valueOf(inputSubscriberInfo.getString("serviceType")));
                        }

                        if (inputSubscriberInfo.has("reserveFlag")){
                            subscriberInfo.setReserveFlag(inputSubscriberInfo.getBoolean("reserveFlag"));
                        }

                        if (inputSubscriberInfo.has("preProFlag")){
                            subscriberInfo.setPreProFlag(inputSubscriberInfo.getBoolean("preProFlag"));
                        }

                        JSONArray inputSourceSimInfo = new JSONArray();
                        if (inputSubscriberInfo.has("sourceSimInfo")){
                            inputSourceSimInfo = inputSubscriberInfo.getJSONArray("sourceSimInfo");

                            // Source sim info
                            List<SourceSimInfo> sourceSimInfoList = new ArrayList<SourceSimInfo>();
                            for(int index=0; index<inputSourceSimInfo.length(); index++){
                                SourceSimInfo sourceSimInfo = new SourceSimInfo();
                                if(imsiConfigData != null){
                                    JSONObject inputSourceSimInfoData = inputSourceSimInfo.getJSONObject(index);
                                    if(inputSourceSimInfoData.has("iccid")){
                                        sourceSimInfo.setIccid(inputSourceSimInfoData.getString("iccid"));
                                    }else{
                                        if(invMappingData.getSecondaryCode()!= null){
                                            sourceSimInfo.setIccid(String.valueOf(invMappingData.getSecondaryCode()));
                                        }
                                    }
                                    sourceSimInfo.setImsi(invMappingData.getImsi()); // query imsi prefix
                                    if(inputSourceSimInfoData.has("simType")){
                                        sourceSimInfo.setSimType(inputSourceSimInfoData.getString("simType"));
                                    }
                                    sourceSimInfo.setFrequency(imsiConfigData.getFrequency()); // query frequency
                                    sourceSimInfoList.add(sourceSimInfo);
                                }
                            }
                            subscriberInfo.setSourceSimInfo(sourceSimInfoList);

                            JSONObject inputDestinationSimInfo = new JSONObject();
                            // Destination sim info
                            if (inputData.has("destinationSimInfo")){
                                inputDestinationSimInfo = inputSubscriberInfo.getJSONObject("destinationSimInfo");
                                
                                // Source sim info
                                List<DestinationSimInfo> destinationSimInfoList = new ArrayList<DestinationSimInfo>();
                                DestinationSimInfo destinationSimInfo = new DestinationSimInfo();
                                
                                if (inputDestinationSimInfo.has("iccid")){
                                    String iccid = inputDestinationSimInfo.getString("iccid");
                                    destinationSimInfo.setIccid(iccid);

                                    OrderHeaderClientResp destinationOdheaderResp = ommyfrontService.getOrderHeaderDataByICCID(iccid);
                                    if (destinationOdheaderResp.getErr() == null){
                                        if(destinationOdheaderResp.getData()!= null){
                                            destinationSimInfo.setImsi(destinationOdheaderResp.getData().getImsi()); // search from iccid
                                        }
                                    }
                                }                            
                                destinationSimInfo.setSimType(null); // Fix null
                                destinationSimInfo.setFrequency(null); // Fix null
                                destinationSimInfoList.add(destinationSimInfo);

                                if (inputDestinationSimInfo.has("itouristSimFlag")){
                                    subscriberInfo.setTouristSimFlag(inputDestinationSimInfo.getString("itouristSimFlag"));
                                }

                                if (inputDestinationSimInfo.has("subscriberNumber")){
                                    subscriberInfo.setSubscriberNumber(inputDestinationSimInfo.getString("subscriberNumber"));
                                }

                                subscriberInfo.setDestinationSimInfo(destinationSimInfoList);
                            }
                        }

                        if (inputSubscriberInfo.has("touristSimFlag")){
                            subscriberInfo.setTouristSimFlag(inputSubscriberInfo.getString("touristSimFlag"));
                        }

                        omEv.setSubscriberInfo(subscriberInfo);
                        // evenItemOther.setDestinationSubscriberInfo(destinationSubscriberInfo);
                        
                        
                    }
                    

                }catch (Exception e){
                    throw new Exception("loop destinationSubscriberInfo main error: " + e.getMessage());
                }

                // eventItem
                try{
                    if (inputData.has("orderItem")){
                        List<EventItem> evenItems = new ArrayList<>();
                        JSONArray orderItems =null;
                        try{
                            orderItems = inputData.getJSONArray("orderItem");
                        }catch(Exception e){
                            throw new Exception("get orderItems main mapping error: " + e.getMessage());
                        }

                        try{
                            for (int i = 0; i < orderItems.length(); i++){
                                JSONObject orderItem = orderItems.getJSONObject(i);
                                EventItem evenItem = new EventItem();
                                // EventItem
                                try{
                                    if (orderItem.has("orderType")){
                                        evenItem.setItemType(orderItem.getString("orderType"));
                                        if(orderItem.getString("orderType") == null || orderItem.getString("orderType").isBlank()){
                                            // Skip the order event item
                                            continue;
                                        }
                                    }
                                    
                                    if (orderItem.has("orderExecutionDate")){
                                        evenItem.setEffectiveDate(orderItem.getString("orderExecutionDate"));
                                    }

                                    if (orderItem.has("transferType")){
                                        evenItem.setTransferType(orderItem.getString("transferType"));
                                    }

                                    if (orderItem.has("effectiveMode")){
                                        evenItem.setEffectiveMode(orderItem.getString("effectiveMode"));
                                    }

                                    if (orderItem.has("orderItemId")){
                                        evenItem.setOrderItemId(orderItem.getString("orderItemId"));
                                    }

                                    if (orderItem.has("orderItemStatus")){
                                        evenItem.setOrderItemStatus(orderItem.getString("orderItemStatus"));
                                    }

                                    if (orderItem.has("actionType")){
                                        evenItem.setActionType(orderItem.getString("actionType"));
                                    }

                                    if (orderItem.has("extendedDay")){
                                        evenItem.setExtendedDay(orderItem.getInt("extendedDay"));
                                    }

                                    if (orderItem.has("effectiveTime")){
                                        evenItem.setEffectiveTime(orderItem.getString("effectiveTime"));
                                    }
                                    
                                    if (orderItem.has("orderExecutionType")){
                                        evenItem.setExecutionType(orderItem.getString("orderExecutionType"));
                                    }

                                    if (orderItem.has("sourceEntity")){
                                        evenItem.setSourceEntity(orderItem.getString("sourceEntity"));
                                    }

                                    if (orderItem.has("subPropertyCode")){
                                        List<SubPropertyCode> subPropertyCodes = new ArrayList<SubPropertyCode>();
                                        JSONArray subProperties = orderItem.getJSONArray("subPropertyCode");
                                        for (int index = 0; index < subProperties.length(); index++) {
                                            SubPropertyCode subPropertyCode = new SubPropertyCode();

                                            if(subProperties.getJSONObject(index).has("value")){
                                                subPropertyCode.setValue(subProperties.getJSONObject(index).getString("value"));
                                            }

                                            if(subProperties.getJSONObject(index).has("code")){
                                                subPropertyCode.setCode(subProperties.getJSONObject(index).getString("code"));
                                            }
                                            subPropertyCodes.add(subPropertyCode);
                                        }
                                        evenItem.setSubPropertyCode(subPropertyCodes);

                                    }

                                    if (orderItem.has("unitType")){
                                        evenItem.setUnitType(orderItem.getString("unitType"));
                                    }
                                    
                                    if (orderItem.has("userRole")){
                                        evenItem.setUserRole(orderItem.getString("userRole"));
                                    }
                                }catch(Exception e){
                                    throw new Exception("loop event item main error: " + e.getMessage());
                                }

                                // OrderItem
                                JSONArray productOfferingList = new JSONArray();
                                try{
                                    if (orderItem.has("productOffering")){
                                        productOfferingList = orderItem.getJSONArray("productOffering");
                                    }

                                }catch(Exception e){
                                    throw new Exception("loop OrderItem main error: " + e.getMessage());
                                }
                                
                                /*  
                                * Offer 
                                */
                                List<Offer> offers = new ArrayList<Offer>();
                                Offer offer = new Offer();
                                
                                try{
                                    if (productOfferingList.length() > 0){
                                        for(int j=0;j<productOfferingList.length();j++){
                                            JSONObject productOffering = productOfferingList.getJSONObject(j);
                                            String offeringId = "";
                                            offeringId = productOffering.getString("offeringId");
                                            offer.setOfferingId(offeringId);

                                            OfferingSpecData ofrspec = null;
                                            
                                            try{
                                                if (!offeringId.isEmpty()){
                                                    // System.out.println("offeringId:"+offeringId);
                                                    OfferingSpecClientResp ofrspecResp = catmfeService.getOfferingSpecByOfferingId(offeringId);
                                                    if(ofrspecResp.getErr() == null && ofrspecResp.getData() != null){
                                                        ofrspec = ofrspecResp.getData();
                                                    }
                                                }
                                            }catch (Exception e){
                                                throw new Exception("loop offer get OfferingSpecData main error: " + e.getMessage());
                                            }
                                            
                                            

                                            if (productOffering != null ){
                                                if (productOffering.has("offeringType")){
                                                    offer.setOfferingType(productOffering.getString("offeringType"));
                                                }
                                                
                                                if (productOffering.has("actionFlag")){
                                                    offer.setActionFlag(productOffering.getString("actionFlag"));
                                                }
                                            }
                                            
                                            try{
                                                if (ofrspec != null){

                                                    offer.setOfferingNameTh(ofrspec.getOfferingnameTH());

                                                    offer.setOfferingNameEn(ofrspec.getOfferingnameEN());

                                                    offer.setPackageId(ofrspec.getPackageID());

                                                    offer.setPackageName(ofrspec.getPackageName());

                                                    offer.setDescriptionTh(ofrspec.getDescTH());

                                                    offer.setDescriptionEn(ofrspec.getDescEN());

                                                    offer.setServiceType(subscriberInfo.getServiceType());                                                        

                                                    offer.setOcsOfferingName(ofrspec.getOcsofferingname());

                                                    if(ofrspec.getRcamount() != null){
                                                        BigDecimal rcamount = new BigDecimal(ofrspec.getRcamount());
                                                        offer.setRcAmount(rcamount);
                                                    }

                                                    if(ofrspec.getRcvatamount()!= null){
                                                        BigDecimal rcvatamount = new BigDecimal(ofrspec.getRcvatamount());
                                                        offer.setRcVatAmount(rcvatamount);
                                                    }

                                                    offer.setPeriod(ofrspec.getPeriod());

                                                    offer.setUnitPeriod(ofrspec.getUnitperiod());

                                                    offer.setSaleStartDate(ofrspec.getSalestartdate());

                                                    offer.setSaleEndDate(ofrspec.getSaleenddate());

                                                    if (ofrspec.getMaxdayafteractivedate() != null){
                                                        BigDecimal maxdayafteractivedate = new BigDecimal(ofrspec.getMaxdayafteractivedate());
                                                        offer.setMaxDayAfterActiveDate(maxdayafteractivedate);
                                                    }

                                                    offer.setNiceNumberFlag(ofrspec.getNicenumberflag());

                                                    if (ofrspec.getNicenumberlevel() != null){
                                                        BigDecimal nicenumberlevel = new BigDecimal(ofrspec.getNicenumberlevel());
                                                        offer.setNiceNumberLevel(nicenumberlevel);
                                                    }

                                                    offer.setConTractFlag(ofrspec.getContractflag());

                                                    if (ofrspec.getContractunitperiod() != null){
                                                        BigDecimal contractunitperiod = new BigDecimal(ofrspec.getContractunitperiod());
                                                        offer.setContractUnitPeriod(contractunitperiod);
                                                    }

                                                    offer.setCatEmpFlag(ofrspec.getCatempflag());

                                                    offer.setCatRetireEmpFlag(ofrspec.getRetiredcatempflag());

                                                    // offer.multisimFlag your code here with logic
                                                    if (invMappingData != null){
                                                        offer.setMultisimFlag(String.valueOf(invMappingData.getMultisimFlag()));
                                                    }

                                                    offer.setTopupSimFlag(ofrspec.getTopupsimflag());

                                                    offer.setTouristSimFlag(ofrspec.getTouristsimflag());

                                                    offer.setChangePoUssdCode(ofrspec.getChangepoussdcode());

                                                    offer.setAddSoUssdCode(ofrspec.getAddsoussdcode());

                                                    offer.setDeleteSoUssdCode(ofrspec.getDeletesoussdcode());

                                                    // offer.frequency your code here with logic
                                                    // offer.multisimFlag your code here with logic
                                                    if (invMappingData != null){
                                                        String imsiMapping = invMappingData.getImsi();
                                                        String imsiFrequency = "";
                                                        for (IMSIOfferingConfig config : imsiOfferConfigList.getData()) {
                                                            String prefix = config.getImsiPrefix();
                                                            if (imsiMapping.startsWith(prefix)) {
                                                                imsiMapping = prefix;
                                                                imsiFrequency = config.getFrequency();
                                                                break;
                                                            }
                                                        }
                                                        offer.setFrequency(imsiFrequency);
                                                    }

                                                    offer.setCanSwapPoFlag(ofrspec.getCanswappoflag());

                                                    offers.add(offer);
                                                }
                                                
                                            }catch (Exception e){
                                                throw new Exception("loop offer set OfferingSpecData to offer error: " + e.getMessage());
                                            }
                                        }

                                        evenItem.setOffer(offers);
                                        /*  
                                        * End offer
                                        */
                                    }
                                    
                                }catch (Exception e){
                                    throw new Exception("loop Offer main error: " + e.getMessage());
                                }
                                /*
                                * Photo
                                */
                                List<Photo> photos = new ArrayList<Photo>();

                                try{
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


                                }catch( Exception e ){
                                    throw new Exception("loop photo main error: " + e.getMessage());
                                }


                                TopUp topUp = new TopUp();
                                CreditLimit creditLimit = new CreditLimit();

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
                                JSONObject inputCreditLimit = new JSONObject();
                                if (orderItem.has("creditLimit")){
                                    inputCreditLimit = orderItem.getJSONObject("creditLimit");

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
                                if (tMCDTLResp.getData() != null){
                                    TransManageContractDTLData tMCDTLData = tMCDTLResp.getData();
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
                                }

                                /*
                                *  SouthernContactAddress
                                */
                                if (orderItem.has("southernContactAddress")){

                                    JSONObject inputSouthernContactAddress = null;
                                    SouthernContactAddress southernContactAddress = new SouthernContactAddress();
        

                                    inputSouthernContactAddress = orderItem.getJSONObject("southernContactAddress");

                                    
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

                                // destinationSubscriberInfo
                                if(orderItem.has("destinationSubscriberInfo")){
                                    JSONObject inputDestinationSubscriberInfo = null;
                                    EvDestinationSubscriberInfo destinationSubscriberInfo = new EvDestinationSubscriberInfo();
        
                                    inputDestinationSubscriberInfo = orderItem.getJSONObject("destinationSubscriberInfo");
                                    
                                    if(inputDestinationSubscriberInfo != null){

                                        if (inputDestinationSubscriberInfo.has("msisdn")){
                                            destinationSubscriberInfo.setMsisdn(inputDestinationSubscriberInfo.getString("msisdn"));
                                        }

                                        if (inputDestinationSubscriberInfo.has("serviceType")){
                                            destinationSubscriberInfo.setServiceType(Integer.valueOf(inputDestinationSubscriberInfo.getString("serviceType")));
                                        }

                                        if (inputDestinationSubscriberInfo.has("destinationSimInfo")){
                                            JSONArray inputDestinationSimInfoList = null;
                                            List<EvDestinationSimInfo> destinationSimInfoList = new ArrayList<EvDestinationSimInfo>();
                                            inputDestinationSimInfoList = inputDestinationSubscriberInfo.getJSONArray("destinationSimInfo");
                                            for (int index = 0; index < inputDestinationSimInfoList.length(); index++){
                                                EvDestinationSimInfo destinationSimInfo = new EvDestinationSimInfo();
                                                JSONObject desSimData = inputDestinationSimInfoList.getJSONObject(index);
                                                if (desSimData.has("iccid")){
                                                    destinationSimInfo.setIccid(desSimData.getString("iccid"));
                                                }

                                                if (desSimData.has("imsi")){
                                                    destinationSimInfo.setImsi(desSimData.getString("imsi"));
                                                }

                                                if (desSimData.has("simType")){
                                                    destinationSimInfo.setSimType(desSimData.getString("simType"));
                                                }

                                                if (desSimData.has("frequency")){
                                                    destinationSimInfo.setFrequency(desSimData.getString("frequency"));
                                                }
                                                destinationSimInfoList.add(destinationSimInfo);
                                            }
                                            destinationSubscriberInfo.setDestinationSimInfo(destinationSimInfoList);
                                        }
                                        
                                        if (inputDestinationSubscriberInfo.has("reserveFlag")){
                                            destinationSubscriberInfo.setReserveFlag(inputDestinationSubscriberInfo.getBoolean("reserveFlag"));
                                        }
                
                                        if (inputDestinationSubscriberInfo.has("preProFlag")){
                                            destinationSubscriberInfo.setPreProFlag(inputDestinationSubscriberInfo.getBoolean("preProFlag"));
                                        }

                                        if (inputDestinationSubscriberInfo.has("touristSimFlag")){
                                            destinationSubscriberInfo.setTouristSimFlag(inputDestinationSubscriberInfo.getString("touristSimFlag"));
                                        }
                                        
                                        evenItem.setDestinationSubscriberInfo(destinationSubscriberInfo);
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

                                JSONObject inputSourceCustomerAccount = null;
                                JSONObject inputSourceCustomerAccountAddress = null;
                                JSONObject sourceCustomerAccountBillingAccount = null;
                                JSONObject sourceCustomerAccountBillDeliveryAddress = null;
                                JSONObject sourceCustomerAccountBillingAccountBillingInfo = null;
                                JSONObject sourceCustomerAccountBillingAccountBillingAddress = null;
                                JSONObject sourceCustomerAccountVatAddress = null;
                                JSONObject sourceCustomerAccountVatDeliveryAddress = null;
                                
                                
                                try{
                                    if (orderItem.has("destinationCustomerAccount")){
                                        inputSourceCustomerAccount = orderItem.getJSONObject("destinationCustomerAccount");

                                        if (inputSourceCustomerAccount != null){
                                            if (inputSourceCustomerAccount.has("address")){
                                                inputSourceCustomerAccountAddress = inputSourceCustomerAccount.getJSONObject("address");
                        
                                                if (inputSourceCustomerAccountAddress.has("title")){
                                                    address.setTitle(inputSourceCustomerAccountAddress.getString("title"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("firstName")){
                                                    address.setFirstName(inputSourceCustomerAccountAddress.getString("firstName"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("lastName")){
                                                    address.setLastName(inputSourceCustomerAccountAddress.getString("lastName"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("companyTitle")){
                                                    address.setCompanyTitle(inputSourceCustomerAccountAddress.getString("companyTitle"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("company")){
                                                    address.setCompany(inputSourceCustomerAccountAddress.getString("company"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("postalCode")){
                                                    address.setPostalCode(inputSourceCustomerAccountAddress.getString("postalCode"));
                                                }
                                                
                                                if (inputSourceCustomerAccountAddress.has("countryCode")){
                                                    address.setCountryCode(inputSourceCustomerAccountAddress.getString("countryCode"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("khet")){
                                                    address.setKhet(inputSourceCustomerAccountAddress.getString("khet"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("kwang")){
                                                    address.setKwang(inputSourceCustomerAccountAddress.getString("kwang"));
                                                }

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

                                                
                                            }

                                            // Billing Account
                                            if (inputSourceCustomerAccount.has("billingAccount")){

                                                sourceCustomerAccountBillingAccount = inputSourceCustomerAccount.getJSONObject("billingAccount");

                                                if (sourceCustomerAccountBillingAccount != null){

                                                    if (sourceCustomerAccountBillingAccount.has("existingFlag")){
                                                        billingAccount.setExistingFlag(sourceCustomerAccountBillingAccount.getBoolean("existingFlag"));
                                                    }

                                                    if (sourceCustomerAccountBillingAccount.has("billingAccountId")){
                                                        billingAccount.setBillingAccountId(sourceCustomerAccountBillingAccount.getString("billingAccountId"));
                                                    }

                                                    if (sourceCustomerAccountBillingAccount.has("paymentProfile")){
                                                        billingAccount.setPaymentProfile(sourceCustomerAccountBillingAccount.getString("paymentProfile"));
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
                                                    if (sourceCustomerAccountBillingAccount.has("billingAddress")){
                                                        sourceCustomerAccountBillingAccountBillingAddress = sourceCustomerAccountBillingAccount.getJSONObject("billingAddress");
                                
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("title")){
                                                            billingAddress.setTitle(sourceCustomerAccountBillingAccountBillingAddress.getString("title"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("firstName")){
                                                            billingAddress.setFirstName(sourceCustomerAccountBillingAccountBillingAddress.getString("firstName"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("lastName")){
                                                            billingAddress.setLastName(sourceCustomerAccountBillingAccountBillingAddress.getString("lastName"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("companyTitle")){
                                                            billingAddress.setCompanyTitle(sourceCustomerAccountBillingAccountBillingAddress.getString("companyTitle"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("company")){
                                                            billingAddress.setCompany(sourceCustomerAccountBillingAccountBillingAddress.getString("company"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("postalCode")){
                                                            billingAddress.setPostalCode(sourceCustomerAccountBillingAccountBillingAddress.getString("postalCode"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("countryCode")){
                                                            billingAddress.setCountryCode(sourceCustomerAccountBillingAccountBillingAddress.getString("countryCode"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("khet")){
                                                            billingAddress.setKhet(sourceCustomerAccountBillingAccountBillingAddress.getString("khet"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("kwang")){
                                                            billingAddress.setKwang(sourceCustomerAccountBillingAccountBillingAddress.getString("kwang"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("building")){
                                                            billingAddress.setBuilding(sourceCustomerAccountBillingAccountBillingAddress.getString("building"));
                                                        }
                                                        
                                                        
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("country")){
                                                            billingAddress.setCountry(sourceCustomerAccountBillingAccountBillingAddress.getString("country"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("houseNumber")){
                                                            billingAddress.setHouseNumber(sourceCustomerAccountBillingAccountBillingAddress.getString("houseNumber"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("khetAmphur")){
                                                            billingAddress.setKhetAmphur(sourceCustomerAccountBillingAccountBillingAddress.getString("khetAmphur"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("kwangTambon")){
                                                            billingAddress.setKwangTambon(sourceCustomerAccountBillingAccountBillingAddress.getString("kwangTambon"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("moo")){
                                                            billingAddress.setMoo(sourceCustomerAccountBillingAccountBillingAddress.getString("moo"));
                                                        }
                                
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("postCode")){
                                                            billingAddress.setPostCode(sourceCustomerAccountBillingAccountBillingAddress.getString("postCode"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("province")){
                                                            billingAddress.setProvince(sourceCustomerAccountBillingAccountBillingAddress.getString("province"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("road")){
                                                            billingAddress.setRoad(sourceCustomerAccountBillingAccountBillingAddress.getString("road"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("troksoi")){
                                                            billingAddress.setTroksoi(sourceCustomerAccountBillingAccountBillingAddress.getString("troksoi"));
                                                        }
                                
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("village")){
                                                            billingAddress.setVillage(sourceCustomerAccountBillingAccountBillingAddress.getString("village"));
                                                        }
                                                    }

                                                    // billing delivery address
                                                    if (sourceCustomerAccountBillingAccount.has("billDeliveryAddress")){
                                                        sourceCustomerAccountBillDeliveryAddress = sourceCustomerAccountBillingAccount.getJSONObject("billDeliveryAddress");

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("title")){
                                                            billDeliveryAddress.setTitle(sourceCustomerAccountBillDeliveryAddress.getString("title"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("firstName")){
                                                            billDeliveryAddress.setFirstName(sourceCustomerAccountBillDeliveryAddress.getString("firstName"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("lastName")){
                                                            billDeliveryAddress.setLastName(sourceCustomerAccountBillDeliveryAddress.getString("lastName"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("companyTitle")){
                                                            billDeliveryAddress.setCompanyTitle(sourceCustomerAccountBillDeliveryAddress.getString("companyTitle"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("company")){
                                                            billDeliveryAddress.setCompany(sourceCustomerAccountBillDeliveryAddress.getString("company"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("postalCode")){
                                                            billDeliveryAddress.setPostalCode(sourceCustomerAccountBillDeliveryAddress.getString("postalCode"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountBillDeliveryAddress.has("countryCode")){
                                                            billDeliveryAddress.setCountryCode(sourceCustomerAccountBillDeliveryAddress.getString("countryCode"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("khet")){
                                                            billDeliveryAddress.setKhet(sourceCustomerAccountBillDeliveryAddress.getString("khet"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("kwang")){
                                                            billDeliveryAddress.setKwang(sourceCustomerAccountBillDeliveryAddress.getString("kwang"));
                                                        }
                                                        
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

                                                    // vat address
                                                    if (sourceCustomerAccountBillingAccount.has("vatAddress")){
                                                        sourceCustomerAccountVatAddress = sourceCustomerAccountBillingAccount.getJSONObject("vatAddress");

                                                        if (sourceCustomerAccountVatAddress.has("title")){
                                                            vatAddress.setTitle(sourceCustomerAccountVatAddress.getString("title"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("firstName")){
                                                            vatAddress.setFirstName(sourceCustomerAccountVatAddress.getString("firstName"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("lastName")){
                                                            vatAddress.setLastName(sourceCustomerAccountVatAddress.getString("lastName"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("companyTitle")){
                                                            vatAddress.setCompanyTitle(sourceCustomerAccountVatAddress.getString("companyTitle"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("company")){
                                                            vatAddress.setCompany(sourceCustomerAccountVatAddress.getString("company"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("postalCode")){
                                                            vatAddress.setPostalCode(sourceCustomerAccountVatAddress.getString("postalCode"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountVatAddress.has("countryCode")){
                                                            vatAddress.setCountryCode(sourceCustomerAccountVatAddress.getString("countryCode"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("khet")){
                                                            vatAddress.setKhet(sourceCustomerAccountVatAddress.getString("khet"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("kwang")){
                                                            vatAddress.setKwang(sourceCustomerAccountVatAddress.getString("kwang"));
                                                        }
                                                        
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

                                                    // vat delivery address
                                                    if (sourceCustomerAccountBillingAccount.has("vatDeliveryAddress")){
                                                        sourceCustomerAccountVatDeliveryAddress = sourceCustomerAccountBillingAccount.getJSONObject("vatDeliveryAddress");

                                                        if (sourceCustomerAccountVatDeliveryAddress.has("title")){
                                                            vatDeliveryAddress.setTitle(sourceCustomerAccountVatDeliveryAddress.getString("title"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("firstName")){
                                                            vatDeliveryAddress.setFirstName(sourceCustomerAccountVatDeliveryAddress.getString("firstName"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("lastName")){
                                                            vatDeliveryAddress.setLastName(sourceCustomerAccountVatDeliveryAddress.getString("lastName"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("companyTitle")){
                                                            vatDeliveryAddress.setCompanyTitle(sourceCustomerAccountVatDeliveryAddress.getString("companyTitle"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("company")){
                                                            vatDeliveryAddress.setCompany(sourceCustomerAccountVatDeliveryAddress.getString("company"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("postalCode")){
                                                            vatDeliveryAddress.setPostalCode(sourceCustomerAccountVatDeliveryAddress.getString("postalCode"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("countryCode")){
                                                            vatDeliveryAddress.setCountryCode(sourceCustomerAccountVatDeliveryAddress.getString("countryCode"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("khet")){
                                                            vatDeliveryAddress.setKhet(sourceCustomerAccountVatDeliveryAddress.getString("khet"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("kwang")){
                                                            vatDeliveryAddress.setKwang(sourceCustomerAccountVatDeliveryAddress.getString("kwang"));
                                                        }

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
                                
                                                }
                                            }


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
                        
                                            if (inputSourceCustomerAccount.has("custaccountId")){
                                                destinationCustomerAccount.setCustAccountId(inputSourceCustomerAccount.getString("custaccountId"));
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

                                            // set detail billing account
                                            billingAccount.setBillingInfo(billingInfo);
                                            billingAccount.setBillingAddress(billingAddress);
                                            billingAccount.setBillDeliveryAddress(billDeliveryAddress);
                                            billingAccount.setVatAddress(vatAddress);
                                            billingAccount.setVatDeliveryAddress(vatDeliveryAddress);
                                            
                                            // set address
                                            destinationCustomerAccount.setAddress(address);
                                            // set billing account
                                            destinationCustomerAccount.setBillingAccount(billingAccount);
                                            evenItem.setDestinationCustomerAccount(destinationCustomerAccount);
                                        }
                                        
                                    }

                                }catch(Exception e){
                                    throw new Exception("loop sourceCustomerAccount main error: " + e.getMessage());
                                }


                                //  Add EventItem
                                evenItems.add(evenItem);

                            }
                            
                            omEv.setEventItems(evenItems);
                        }catch (Exception e){
                            throw new Exception("event item loop mapping error: " + e.getMessage());
                        }
                    }
                }catch (Exception e){
                    throw new Exception("get eventitem main mapping error: " + e.getMessage());
                }


                /*
                * SourceCustomerAccount
                */
                // SourceCustomerAccount sourceCustomerAccount = new SourceCustomerAccount();
                SourceCustomerAccount sourceCustomerAccount = new SourceCustomerAccount();
                Address address = new Address();
                BillingAccount billingAccount = new BillingAccount();
                BillingInfo billingInfo = new BillingInfo();
                BillingAddress billingAddress = new BillingAddress();
                BillDeliveryAddress billDeliveryAddress = new BillDeliveryAddress();
                VatAddress vatAddress = new VatAddress();
                VatDeliveryAddress vatDeliveryAddress = new VatDeliveryAddress();

                JSONObject inputSourceCustomerAccount = null;
                JSONObject inputSourceCustomerAccountAddress = null;
                JSONObject sourceCustomerAccountBillingAccount = null;
                JSONObject sourceCustomerAccountBillDeliveryAddress = null;
                JSONObject sourceCustomerAccountBillingAccountBillingInfo = null;
                JSONObject sourceCustomerAccountBillingAccountBillingAddress = null;
                JSONObject sourceCustomerAccountVatAddress = null;
                JSONObject sourceCustomerAccountVatDeliveryAddress = null;
                
                
                try{
                    if (inputData.has("sourceCustomerAccount")){
                        inputSourceCustomerAccount = inputData.getJSONObject("sourceCustomerAccount");

                        if (inputSourceCustomerAccount != null){
                            if (inputSourceCustomerAccount.has("address")){
                                inputSourceCustomerAccountAddress = inputSourceCustomerAccount.getJSONObject("address");

                                if (inputSourceCustomerAccountAddress.has("title")){
                                    address.setTitle(inputSourceCustomerAccountAddress.getString("title"));
                                }

                                if (inputSourceCustomerAccountAddress.has("firstName")){
                                    address.setFirstName(inputSourceCustomerAccountAddress.getString("firstName"));
                                }

                                if (inputSourceCustomerAccountAddress.has("lastName")){
                                    address.setLastName(inputSourceCustomerAccountAddress.getString("lastName"));
                                }

                                if (inputSourceCustomerAccountAddress.has("companyTitle")){
                                    address.setCompanyTitle(inputSourceCustomerAccountAddress.getString("companyTitle"));
                                }

                                if (inputSourceCustomerAccountAddress.has("company")){
                                    address.setCompany(inputSourceCustomerAccountAddress.getString("company"));
                                }

                                if (inputSourceCustomerAccountAddress.has("postalCode")){
                                    address.setPostalCode(inputSourceCustomerAccountAddress.getString("postalCode"));
                                }
                                
                                if (inputSourceCustomerAccountAddress.has("countryCode")){
                                    address.setCountryCode(inputSourceCustomerAccountAddress.getString("countryCode"));
                                }

                                if (inputSourceCustomerAccountAddress.has("khet")){
                                    address.setKhet(inputSourceCustomerAccountAddress.getString("khet"));
                                }

                                if (inputSourceCustomerAccountAddress.has("kwang")){
                                    address.setKwang(inputSourceCustomerAccountAddress.getString("kwang"));
                                }

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

                                
                            }

                            // Billing Account
                            if (inputSourceCustomerAccount.has("billingAccount")){

                                sourceCustomerAccountBillingAccount = inputSourceCustomerAccount.getJSONObject("billingAccount");

                                if (sourceCustomerAccountBillingAccount != null){

                                    if (sourceCustomerAccountBillingAccount.has("existingFlag")){
                                        billingAccount.setExistingFlag(sourceCustomerAccountBillingAccount.getBoolean("existingFlag"));
                                    }

                                    if (sourceCustomerAccountBillingAccount.has("billingAccountId")){
                                        billingAccount.setBillingAccountId(sourceCustomerAccountBillingAccount.getString("billingAccountId"));
                                    }

                                    if (sourceCustomerAccountBillingAccount.has("paymentProfile")){
                                        billingAccount.setPaymentProfile(sourceCustomerAccountBillingAccount.getString("paymentProfile"));
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
                                    if (sourceCustomerAccountBillingAccount.has("billingAddress")){
                                        sourceCustomerAccountBillingAccountBillingAddress = sourceCustomerAccountBillingAccount.getJSONObject("billingAddress");
                
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("title")){
                                            billingAddress.setTitle(sourceCustomerAccountBillingAccountBillingAddress.getString("title"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("firstName")){
                                            billingAddress.setFirstName(sourceCustomerAccountBillingAccountBillingAddress.getString("firstName"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("lastName")){
                                            billingAddress.setLastName(sourceCustomerAccountBillingAccountBillingAddress.getString("lastName"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("companyTitle")){
                                            billingAddress.setCompanyTitle(sourceCustomerAccountBillingAccountBillingAddress.getString("companyTitle"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("company")){
                                            billingAddress.setCompany(sourceCustomerAccountBillingAccountBillingAddress.getString("company"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("postalCode")){
                                            billingAddress.setPostalCode(sourceCustomerAccountBillingAccountBillingAddress.getString("postalCode"));
                                        }
                                        
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("countryCode")){
                                            billingAddress.setCountryCode(sourceCustomerAccountBillingAccountBillingAddress.getString("countryCode"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("khet")){
                                            billingAddress.setKhet(sourceCustomerAccountBillingAccountBillingAddress.getString("khet"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("kwang")){
                                            billingAddress.setKwang(sourceCustomerAccountBillingAccountBillingAddress.getString("kwang"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("building")){
                                            billingAddress.setBuilding(sourceCustomerAccountBillingAccountBillingAddress.getString("building"));
                                        }
                                        
                                        
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("country")){
                                            billingAddress.setCountry(sourceCustomerAccountBillingAccountBillingAddress.getString("country"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("houseNumber")){
                                            billingAddress.setHouseNumber(sourceCustomerAccountBillingAccountBillingAddress.getString("houseNumber"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("khetAmphur")){
                                            billingAddress.setKhetAmphur(sourceCustomerAccountBillingAccountBillingAddress.getString("khetAmphur"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("kwangTambon")){
                                            billingAddress.setKwangTambon(sourceCustomerAccountBillingAccountBillingAddress.getString("kwangTambon"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("moo")){
                                            billingAddress.setMoo(sourceCustomerAccountBillingAccountBillingAddress.getString("moo"));
                                        }
                
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("postCode")){
                                            billingAddress.setPostCode(sourceCustomerAccountBillingAccountBillingAddress.getString("postCode"));
                                        }
                                        
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("province")){
                                            billingAddress.setProvince(sourceCustomerAccountBillingAccountBillingAddress.getString("province"));
                                        }
                                        
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("road")){
                                            billingAddress.setRoad(sourceCustomerAccountBillingAccountBillingAddress.getString("road"));
                                        }
                                        
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("troksoi")){
                                            billingAddress.setTroksoi(sourceCustomerAccountBillingAccountBillingAddress.getString("troksoi"));
                                        }
                
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("village")){
                                            billingAddress.setVillage(sourceCustomerAccountBillingAccountBillingAddress.getString("village"));
                                        }
                                    }

                                    // billing delivery address
                                    if (sourceCustomerAccountBillingAccount.has("billDeliveryAddress")){
                                        sourceCustomerAccountBillDeliveryAddress = sourceCustomerAccountBillingAccount.getJSONObject("billDeliveryAddress");

                                        if (sourceCustomerAccountBillDeliveryAddress.has("title")){
                                            billDeliveryAddress.setTitle(sourceCustomerAccountBillDeliveryAddress.getString("title"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("firstName")){
                                            billDeliveryAddress.setFirstName(sourceCustomerAccountBillDeliveryAddress.getString("firstName"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("lastName")){
                                            billDeliveryAddress.setLastName(sourceCustomerAccountBillDeliveryAddress.getString("lastName"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("companyTitle")){
                                            billDeliveryAddress.setCompanyTitle(sourceCustomerAccountBillDeliveryAddress.getString("companyTitle"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("company")){
                                            billDeliveryAddress.setCompany(sourceCustomerAccountBillDeliveryAddress.getString("company"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("postalCode")){
                                            billDeliveryAddress.setPostalCode(sourceCustomerAccountBillDeliveryAddress.getString("postalCode"));
                                        }
                                        
                                        if (sourceCustomerAccountBillDeliveryAddress.has("countryCode")){
                                            billDeliveryAddress.setCountryCode(sourceCustomerAccountBillDeliveryAddress.getString("countryCode"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("khet")){
                                            billDeliveryAddress.setKhet(sourceCustomerAccountBillDeliveryAddress.getString("khet"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("kwang")){
                                            billDeliveryAddress.setKwang(sourceCustomerAccountBillDeliveryAddress.getString("kwang"));
                                        }
                                        
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

                                    // vat address
                                    if (sourceCustomerAccountBillingAccount.has("vatAddress")){
                                        sourceCustomerAccountVatAddress = sourceCustomerAccountBillingAccount.getJSONObject("vatAddress");

                                        if (sourceCustomerAccountVatAddress.has("title")){
                                            vatAddress.setTitle(sourceCustomerAccountVatAddress.getString("title"));
                                        }

                                        if (sourceCustomerAccountVatAddress.has("firstName")){
                                            vatAddress.setFirstName(sourceCustomerAccountVatAddress.getString("firstName"));
                                        }

                                        if (sourceCustomerAccountVatAddress.has("lastName")){
                                            vatAddress.setLastName(sourceCustomerAccountVatAddress.getString("lastName"));
                                        }

                                        if (sourceCustomerAccountVatAddress.has("companyTitle")){
                                            vatAddress.setCompanyTitle(sourceCustomerAccountVatAddress.getString("companyTitle"));
                                        }

                                        if (sourceCustomerAccountVatAddress.has("company")){
                                            vatAddress.setCompany(sourceCustomerAccountVatAddress.getString("company"));
                                        }

                                        if (sourceCustomerAccountVatAddress.has("postalCode")){
                                            vatAddress.setPostalCode(sourceCustomerAccountVatAddress.getString("postalCode"));
                                        }
                                        
                                        if (sourceCustomerAccountVatAddress.has("countryCode")){
                                            vatAddress.setCountryCode(sourceCustomerAccountVatAddress.getString("countryCode"));
                                        }

                                        if (sourceCustomerAccountVatAddress.has("khet")){
                                            vatAddress.setKhet(sourceCustomerAccountVatAddress.getString("khet"));
                                        }

                                        if (sourceCustomerAccountVatAddress.has("kwang")){
                                            vatAddress.setKwang(sourceCustomerAccountVatAddress.getString("kwang"));
                                        }
                                        
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

                                    // vat delivery address
                                    if (sourceCustomerAccountBillingAccount.has("vatDeliveryAddress")){
                                        sourceCustomerAccountVatDeliveryAddress = sourceCustomerAccountBillingAccount.getJSONObject("vatDeliveryAddress");

                                        if (sourceCustomerAccountVatDeliveryAddress.has("title")){
                                            vatDeliveryAddress.setTitle(sourceCustomerAccountVatDeliveryAddress.getString("title"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("firstName")){
                                            vatDeliveryAddress.setFirstName(sourceCustomerAccountVatDeliveryAddress.getString("firstName"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("lastName")){
                                            vatDeliveryAddress.setLastName(sourceCustomerAccountVatDeliveryAddress.getString("lastName"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("companyTitle")){
                                            vatDeliveryAddress.setCompanyTitle(sourceCustomerAccountVatDeliveryAddress.getString("companyTitle"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("company")){
                                            vatDeliveryAddress.setCompany(sourceCustomerAccountVatDeliveryAddress.getString("company"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("postalCode")){
                                            vatDeliveryAddress.setPostalCode(sourceCustomerAccountVatDeliveryAddress.getString("postalCode"));
                                        }
                                        
                                        if (sourceCustomerAccountVatDeliveryAddress.has("countryCode")){
                                            vatDeliveryAddress.setCountryCode(sourceCustomerAccountVatDeliveryAddress.getString("countryCode"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("khet")){
                                            vatDeliveryAddress.setKhet(sourceCustomerAccountVatDeliveryAddress.getString("khet"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("kwang")){
                                            vatDeliveryAddress.setKwang(sourceCustomerAccountVatDeliveryAddress.getString("kwang"));
                                        }

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
                
                                }
                            }


                            if (inputSourceCustomerAccount.has("cardNumber")){
                                sourceCustomerAccount.setCardNumber(inputSourceCustomerAccount.getString("cardNumber"));
                            }
        
                            if (inputSourceCustomerAccount.has("cardType")){
                                sourceCustomerAccount.setCardType(inputSourceCustomerAccount.getInt("cardType"));
                            }
        
                            if (inputSourceCustomerAccount.has("catEmployeeFlag")){
                                sourceCustomerAccount.setCatEmployeeFlag(inputSourceCustomerAccount.getInt("catEmployeeFlag"));
                            }
        
                            if (inputSourceCustomerAccount.has("companyBranchId")){
                                sourceCustomerAccount.setCompanyBranchId(inputSourceCustomerAccount.getString("companyBranchId"));
                            }
        
                            if (inputSourceCustomerAccount.has("companyName")){
                                sourceCustomerAccount.setCompanyName(inputSourceCustomerAccount.getString("companyName"));
                            }
        
                            if (inputSourceCustomerAccount.has("companyType")){
                                sourceCustomerAccount.setCompanyType(inputSourceCustomerAccount.getInt("companyType"));
                            }
        
                            if (inputSourceCustomerAccount.has("contactNumber")){
                                sourceCustomerAccount.setContactNumber(inputSourceCustomerAccount.getString("contactNumber"));
                            }
        
                            if (inputSourceCustomerAccount.has("custaccountId")){
                                sourceCustomerAccount.setCustAccountId(inputSourceCustomerAccount.getString("custaccountId"));
                            }
        
                            if (inputSourceCustomerAccount.has("customerFocus")){
                                sourceCustomerAccount.setCustomerFocus(inputSourceCustomerAccount.getString("customerFocus"));
                            }
        
                            if (inputSourceCustomerAccount.has("customerGroup")){
                                sourceCustomerAccount.setCustomerGroup(inputSourceCustomerAccount.getInt("customerGroup"));
                            }
        
                            if (inputSourceCustomerAccount.has("customerId")){
                                sourceCustomerAccount.setCustomerId(inputSourceCustomerAccount.getString("customerId"));
                            }
        
                            if (inputSourceCustomerAccount.has("customerInfoType")){
                                sourceCustomerAccount.setCustomerInfoType(inputSourceCustomerAccount.getInt("customerInfoType"));
                            }
        
                            if (inputSourceCustomerAccount.has("customerSegment")){
                                sourceCustomerAccount.setCustomerSegment(inputSourceCustomerAccount.getInt("customerSegment"));
                            }
        
                            if (inputSourceCustomerAccount.has("customerType")){
                                sourceCustomerAccount.setCustomerType(inputSourceCustomerAccount.getInt("customerType"));
                            }
        
                            if (inputSourceCustomerAccount.has("dob")){
                                sourceCustomerAccount.setDob(inputSourceCustomerAccount.getString("dob"));
                            }
        
                            if (inputSourceCustomerAccount.has("documentNumber")){
                                sourceCustomerAccount.setDocumentNumber(inputSourceCustomerAccount.getString("documentNumber"));
                            }
        
                            if (inputSourceCustomerAccount.has("documentType")){
                                sourceCustomerAccount.setDocumentType(inputSourceCustomerAccount.getInt("documentType"));
                            }
        
                            if (inputSourceCustomerAccount.has("emailAddress")){
                                sourceCustomerAccount.setEmailAddress(inputSourceCustomerAccount.getString("emailAddress"));
                            }
        
                            if (inputSourceCustomerAccount.has("existingFlag")){
                                sourceCustomerAccount.setExistingFlag(inputSourceCustomerAccount.getBoolean("existingFlag"));
                            }
        
                            if (inputSourceCustomerAccount.has("firstName")){
                                sourceCustomerAccount.setFirstName(inputSourceCustomerAccount.getString("firstName"));
                            }
        
                            if (inputSourceCustomerAccount.has("gender")){
                                sourceCustomerAccount.setGender(inputSourceCustomerAccount.getInt("gender"));
                            }
        
                            if (inputSourceCustomerAccount.has("ivrLanguage")){
                                sourceCustomerAccount.setIvrLanguage(inputSourceCustomerAccount.getString("ivrLanguage"));
                            }
        
                            if (inputSourceCustomerAccount.has("lastName")){
                                sourceCustomerAccount.setLastName(inputSourceCustomerAccount.getString("lastName"));
                            }
        
                            if (inputSourceCustomerAccount.has("nationality")){
                                sourceCustomerAccount.setNationality(inputSourceCustomerAccount.getString("nationality"));
                            }
        
                            if (inputSourceCustomerAccount.has("taxRegisterNumber")){
                                sourceCustomerAccount.setTaxRegisterNumber(inputSourceCustomerAccount.getString("taxRegisterNumber"));
                            }
        
                            if (inputSourceCustomerAccount.has("title")){
                                sourceCustomerAccount.setTitle(inputSourceCustomerAccount.getInt("title"));
                            }
        
                            if (inputSourceCustomerAccount.has("writtenLanguage")){
                                sourceCustomerAccount.setWrittenLanguage(inputSourceCustomerAccount.getString("writtenLanguage"));
                            }

                            // set detail billing account
                            billingAccount.setBillingInfo(billingInfo);
                            billingAccount.setBillingAddress(billingAddress);
                            billingAccount.setBillDeliveryAddress(billDeliveryAddress);
                            billingAccount.setVatAddress(vatAddress);
                            billingAccount.setVatDeliveryAddress(vatDeliveryAddress);
                            
                            // set address
                            sourceCustomerAccount.setAddress(address);
                            // set billing account
                            sourceCustomerAccount.setBillingAccount(billingAccount);

                            omEv.setSourceCustomerAccount(sourceCustomerAccount);

                        }
                        
                    }

                }catch(Exception e){
                    throw new Exception("loop sourceCustomerAccount main error: " + e.getMessage());
                }


            
                // saleInfo
                if(inputData.has("saleInfo")){
                    JSONObject inputSaleInfo = inputData.getJSONObject("saleInfo");
                    SaleInfo saleInfo = new SaleInfo();

                    if (inputSaleInfo.has("saleEmpId")){
                        saleInfo.setSaleEmpId(inputSaleInfo.getString("saleEmpId"));
                    }

                    if (inputSaleInfo.has("sapCode")){
                        saleInfo.setSapCode(inputSaleInfo.getString("sapCode"));
                    }

                    if (inputSaleInfo.has("dealerCode")){
                        saleInfo.setDealerCode(inputSaleInfo.getString("dealerCode"));
                    }

                    if (inputSaleInfo.has("registerBySellerName")){
                        saleInfo.setRegisterBySellerName(inputSaleInfo.getString("registerBySellerName"));
                    }

                    if (inputSaleInfo.has("saleRole")){
                        saleInfo.setSaleRole(inputSaleInfo.getString("saleRole"));
                    }

                    if (inputSaleInfo.has("registerProvince")){
                        saleInfo.setRegisterProvince(inputSaleInfo.getString("registerProvince"));
                    }

                    if (inputSaleInfo.has("territoryName")){
                        saleInfo.setTerritoryName(inputSaleInfo.getString("territoryName"));
                    }

                    if (inputSaleInfo.has("saleRepEmpId")){
                        saleInfo.setSaleRepEmpId(inputSaleInfo.getString("saleRepEmpId"));
                    }

                    if (inputSaleInfo.has("saleRepSapCode")){
                        saleInfo.setSaleRepSapCode(inputSaleInfo.getString("saleRepSapCode"));
                    }

                    if (inputSaleInfo.has("verifyIdentity")){
                        saleInfo.setVerifyIdentity(inputSaleInfo.getBoolean("verifyIdentity"));
                    }
                    
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

                // // orderStatus
                // if(inputData.has("orderHeader")){
                //     JSONObject orderHeader = inputData.getJSONObject("orderHeader");
                //     if(orderHeader.has("orderStatus")){
                //         omEv.setOrderStatus(orderHeader.getString("orderStatus"));
                //     }
                // }
            }
        }catch(Exception e){
            throw new Exception("mapp orderheader error :"+e.getMessage());
        }

        omEv.setOrderStatus(odheader.getOrderStatus());
        omEv.setSubmitedDate(odheader.getCreateDate());
        omEv.setCompletedDate(odheader.getUpdateDate());

        sendData.setTriggerDate(triggerDate);
        sendData.setPublishChannel("OM-MFE");
        sendData.setOrderType(orderTypeName);
        sendData.setMsisdn(String.format("0%s", odheader.getMsisdn())); // your code here
        sendData.setEventData(omEv);

        return sendData;
        
    }

    private Data MappingVarietyServiceData(OrderHeaderData odheader, String orderTypeName) throws Exception{
        String triggerDate = DateTime.getTimeStampNowStr();

        Data sendData = new Data();
        
        EventData omEv = new EventData();
        
        String externalId = odheader.getMsisdn();

        INVMappingData invMappingData =null;
        IMSIOfferingConfig imsiConfigData = null;
        ListIMSIOfferingConfigClientResp imsiOfferConfigList = null;
        try{
            INVMappingClientResp invMappingResp = invuserService.getInvMappingData(externalId);

            if (invMappingResp != null){
                if (invMappingResp.getData() != null && invMappingResp.getErr() == null){
                    invMappingData = invMappingResp.getData();
                    if (invMappingData == null){
                        throw new Exception("INV mapping not found external id: "+externalId);
                    }
                }else{
                    INVMappingClientResp invMappingOnlyResp = invuserService.getInvMappingDataOnly(externalId);
                    invMappingData = invMappingOnlyResp.getData();
                    if (invMappingData == null){
                        throw new Exception("INV mapping not found external id: "+externalId);
                    }
                }
            }else{
                INVMappingClientResp invMappingOnlyResp = invuserService.getInvMappingDataOnly(externalId);
                invMappingData = invMappingOnlyResp.getData();
                if (invMappingData == null){
                    throw new Exception("INV mapping not found external id: "+externalId);
                }
            }
        
            imsiOfferConfigList = cacheUpdater.getIMSIOfferConfigListCache();
            if (imsiOfferConfigList.getErr() == null){
                imsiOfferConfigList = ommyfrontService.getImsiOfferingConfigList();
                cacheUpdater.setIMSIOfferConfigListCache(imsiOfferConfigList);
            }

            if (invMappingData != null){
                if(invMappingData.getImsi() != null){
                    imsiConfigData = getImsiConfigByImsi(invMappingData.getImsi(), imsiOfferConfigList.getData());
                }
                // ObjectMapper objectmapper = new ObjectMapper();
                // objectmapper.writeValueAsString(imsiConfigData);

            }
        }catch (Exception e){
            throw new Exception("INV mapping error: " + e.getMessage());
        }

        try{
            if (odheader.getInputData() != null){
                JSONObject inputData = null;
                try{
                    inputData = new JSONObject(odheader.getInputData().toString());
                }catch (Exception e){
                    throw new Exception("get inputData mapping error: " + e.getMessage());
                }
                // System.out.println("=================================");
                try{
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
                }catch (Exception e){
                    throw new Exception("get header main mapping error: " + e.getMessage());
                }

                if (inputData.has("isProvisionRequired")){
                    omEv.setIsProvisionRequired(inputData.getBoolean("isProvisionRequired"));
                }

                if (inputData.has("rerunRevisionNumber")){
                    omEv.setRerunRevisionNumber(inputData.getInt("rerunRevisionNumber"));
                }

                /*
                *  SubscriberInfo
                */
                SubscriberInfo subscriberInfo = new SubscriberInfo();

                try{
                    if (inputData.has("subscriberInfo")){
                        JSONObject inputSubscriberInfo = inputData.getJSONObject("subscriberInfo");

                        if (inputSubscriberInfo.has("msisdn")){
                            subscriberInfo.setMsisdn(inputSubscriberInfo.getString("msisdn"));
                        }

                        if (inputSubscriberInfo.has("serviceType")){
                            subscriberInfo.setServiceType(Integer.valueOf(inputSubscriberInfo.getString("serviceType")));
                        }

                        if (inputSubscriberInfo.has("reserveFlag")){
                            subscriberInfo.setReserveFlag(inputSubscriberInfo.getBoolean("reserveFlag"));
                        }

                        if (inputSubscriberInfo.has("preProFlag")){
                            subscriberInfo.setPreProFlag(inputSubscriberInfo.getBoolean("preProFlag"));
                        }

                        JSONArray inputSourceSimInfo = new JSONArray();
                        if (inputSubscriberInfo.has("sourceSimInfo")){
                            inputSourceSimInfo = inputSubscriberInfo.getJSONArray("sourceSimInfo");

                            // Source sim info
                            List<SourceSimInfo> sourceSimInfoList = new ArrayList<SourceSimInfo>();
                            for(int index=0; index<inputSourceSimInfo.length(); index++){
                                SourceSimInfo sourceSimInfo = new SourceSimInfo();
                                if(imsiConfigData != null){
                                    JSONObject inputSourceSimInfoData = inputSourceSimInfo.getJSONObject(index);
                                    if(inputSourceSimInfoData.has("iccid")){
                                        sourceSimInfo.setIccid(inputSourceSimInfoData.getString("iccid"));
                                    }else{
                                        if(invMappingData.getSecondaryCode()!= null){
                                            sourceSimInfo.setIccid(String.valueOf(invMappingData.getSecondaryCode()));
                                        }
                                    }
                                    sourceSimInfo.setImsi(invMappingData.getImsi()); // query imsi prefix
                                    if(inputSourceSimInfoData.has("simType")){
                                        sourceSimInfo.setSimType(inputSourceSimInfoData.getString("simType"));
                                    }
                                    sourceSimInfo.setFrequency(imsiConfigData.getFrequency()); // query frequency
                                    sourceSimInfoList.add(sourceSimInfo);
                                }
                            }
                            subscriberInfo.setSourceSimInfo(sourceSimInfoList);

                            JSONObject inputDestinationSimInfo = new JSONObject();
                            // Destination sim info
                            if (inputData.has("destinationSimInfo")){
                                inputDestinationSimInfo = inputSubscriberInfo.getJSONObject("destinationSimInfo");
                                
                                // Source sim info
                                List<DestinationSimInfo> destinationSimInfoList = new ArrayList<DestinationSimInfo>();
                                DestinationSimInfo destinationSimInfo = new DestinationSimInfo();
                                
                                if (inputDestinationSimInfo.has("iccid")){
                                    String iccid = inputDestinationSimInfo.getString("iccid");
                                    destinationSimInfo.setIccid(iccid);

                                    OrderHeaderClientResp destinationOdheaderResp = ommyfrontService.getOrderHeaderDataByICCID(iccid);
                                    if (destinationOdheaderResp.getErr() == null){
                                        if(destinationOdheaderResp.getData()!= null){
                                            destinationSimInfo.setImsi(destinationOdheaderResp.getData().getImsi()); // search from iccid
                                        }
                                    }
                                }                            
                                destinationSimInfo.setSimType(null); // Fix null
                                destinationSimInfo.setFrequency(null); // Fix null
                                destinationSimInfoList.add(destinationSimInfo);

                                if (inputDestinationSimInfo.has("itouristSimFlag")){
                                    subscriberInfo.setTouristSimFlag(inputDestinationSimInfo.getString("itouristSimFlag"));
                                }

                                if (inputDestinationSimInfo.has("subscriberNumber")){
                                    subscriberInfo.setSubscriberNumber(inputDestinationSimInfo.getString("subscriberNumber"));
                                }

                                subscriberInfo.setDestinationSimInfo(destinationSimInfoList);
                            }
                        }

                        if (inputSubscriberInfo.has("touristSimFlag")){
                            subscriberInfo.setTouristSimFlag(inputSubscriberInfo.getString("touristSimFlag"));
                        }

                        omEv.setSubscriberInfo(subscriberInfo);
                        // evenItemOther.setDestinationSubscriberInfo(destinationSubscriberInfo);
                        
                        
                    }
                    

                }catch (Exception e){
                    throw new Exception("loop destinationSubscriberInfo main error: " + e.getMessage());
                }


                // eventItem
                try{
                    if (inputData.has("orderItem")){
                        List<EventItem> evenItems = new ArrayList<>();
                        JSONArray orderItems =null;
                        try{
                            orderItems = inputData.getJSONArray("orderItem");
                        }catch(Exception e){
                            throw new Exception("get orderItems main mapping error: " + e.getMessage());
                        }

                        try{
                            for (int i = 0; i < orderItems.length(); i++){
                                JSONObject orderItem = orderItems.getJSONObject(i);
                                EventItem evenItem = new EventItem();

                                // EventItem
                                try{
                                    if (orderItem.has("orderType")){
                                        evenItem.setItemType(orderItem.getString("orderType"));
                                        if(orderItem.getString("orderType") == null || orderItem.getString("orderType").isBlank()){
                                            // Skip the order event item
                                            continue;
                                        }
                                    }
                                    
                                    if (orderItem.has("orderExecutionDate")){
                                        evenItem.setEffectiveDate(orderItem.getString("orderExecutionDate"));
                                    }

                                    if (orderItem.has("transferType")){
                                        evenItem.setTransferType(orderItem.getString("transferType"));
                                    }

                                    if (orderItem.has("effectiveMode")){
                                        evenItem.setEffectiveMode(orderItem.getString("effectiveMode"));
                                    }

                                    if (orderItem.has("actionType")){
                                        evenItem.setActionType(orderItem.getString("actionType"));
                                    }

                                    if (orderItem.has("orderItemId")){
                                        evenItem.setOrderItemId(orderItem.getString("orderItemId"));
                                    }

                                    if (orderItem.has("orderItemStatus")){
                                        evenItem.setOrderItemStatus(orderItem.getString("orderItemStatus"));
                                    }

                                    if (orderItem.has("extendedDay")){
                                        evenItem.setExtendedDay(orderItem.getInt("extendedDay"));
                                    }

                                    if (orderItem.has("effectiveTime")){
                                        evenItem.setEffectiveTime(orderItem.getString("effectiveTime"));
                                    }
                                    
                                    if (orderItem.has("orderExecutionType")){
                                        evenItem.setExecutionType(orderItem.getString("orderExecutionType"));
                                    }

                                    if (orderItem.has("sourceEntity")){
                                        evenItem.setSourceEntity(orderItem.getString("sourceEntity"));
                                    }

                                    if (orderItem.has("subPropertyCode")){
                                        List<SubPropertyCode> subPropertyCodes = new ArrayList<SubPropertyCode>();
                                        JSONArray subProperties = orderItem.getJSONArray("subPropertyCode");
                                        for (int index = 0; index < subProperties.length(); index++) {
                                            SubPropertyCode subPropertyCode = new SubPropertyCode();

                                            if(subProperties.getJSONObject(index).has("value")){
                                                subPropertyCode.setValue(subProperties.getJSONObject(index).getString("value"));
                                            }

                                            if(subProperties.getJSONObject(index).has("code")){
                                                subPropertyCode.setCode(subProperties.getJSONObject(index).getString("code"));
                                            }
                                            subPropertyCodes.add(subPropertyCode);
                                        }
                                        evenItem.setSubPropertyCode(subPropertyCodes);

                                    }

                                    if (orderItem.has("unitType")){
                                        evenItem.setUnitType(orderItem.getString("unitType"));
                                    }
                                    
                                    if (orderItem.has("userRole")){
                                        evenItem.setUserRole(orderItem.getString("userRole"));
                                    }
                                }catch(Exception e){
                                    throw new Exception("loop event item main error: " + e.getMessage());
                                }
                                
                                // Varieties service
                                List<VarietyService> varietyServicesList = new ArrayList<VarietyService>();
                                if (orderItem.has("varietyServices")){
                                    // List<VarietyService> varietyServices = new ArrayList<VarietyService>();
                                    JSONArray varietyServices = orderItem.getJSONArray("varietyServices");
                                    for(int index = 0;index<varietyServices.length();index++){
                                        JSONObject varietyServiceItem = varietyServices.getJSONObject(index);
                                        VarietyService varietyService = new VarietyService();
                                        if (varietyServiceItem.has("enabledFlag") &&!varietyServiceItem.isNull("enabledFlag")){
                                            varietyService.setEnabledFlag(varietyServiceItem.getInt("enabledFlag"));
                                        }
                                        if (varietyServiceItem.has("varietyType")&&!varietyServiceItem.isNull("varietyType")){
                                            varietyService.setVarietyType(varietyServiceItem.getString("varietyType"));
                                        }
                                        varietyServicesList.add(varietyService);
                                    }
                                    
                                    evenItem.setVarietyServices(varietyServicesList);
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

                                JSONObject inputSourceCustomerAccount = null;
                                JSONObject inputSourceCustomerAccountAddress = null;
                                JSONObject sourceCustomerAccountBillingAccount = null;
                                JSONObject sourceCustomerAccountBillDeliveryAddress = null;
                                JSONObject sourceCustomerAccountBillingAccountBillingInfo = null;
                                JSONObject sourceCustomerAccountBillingAccountBillingAddress = null;
                                JSONObject sourceCustomerAccountVatAddress = null;
                                JSONObject sourceCustomerAccountVatDeliveryAddress = null;
                                
                                
                                try{
                                    if (orderItem.has("destinationCustomerAccount")){
                                        inputSourceCustomerAccount = orderItem.getJSONObject("destinationCustomerAccount");
                                        

                                        if (inputSourceCustomerAccount != null){
                                            if (inputSourceCustomerAccount.has("address")){
                                                inputSourceCustomerAccountAddress = inputSourceCustomerAccount.getJSONObject("address");
                                                
                                                if (inputSourceCustomerAccountAddress.has("title")){
                                                    address.setTitle(inputSourceCustomerAccountAddress.getString("title"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("firstName")){
                                                    address.setFirstName(inputSourceCustomerAccountAddress.getString("firstName"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("lastName")){
                                                    address.setLastName(inputSourceCustomerAccountAddress.getString("lastName"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("companyTitle")){
                                                    address.setCompanyTitle(inputSourceCustomerAccountAddress.getString("companyTitle"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("company")){
                                                    address.setCompany(inputSourceCustomerAccountAddress.getString("company"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("postalCode")){
                                                    address.setPostalCode(inputSourceCustomerAccountAddress.getString("postalCode"));
                                                }
                                                
                                                if (inputSourceCustomerAccountAddress.has("countryCode")){
                                                    address.setCountryCode(inputSourceCustomerAccountAddress.getString("countryCode"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("khet")){
                                                    address.setKhet(inputSourceCustomerAccountAddress.getString("khet"));
                                                }
                
                                                if (inputSourceCustomerAccountAddress.has("kwang")){
                                                    address.setKwang(inputSourceCustomerAccountAddress.getString("kwang"));
                                                }

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

                                                
                                            }

                                            // Billing Account
                                            if (inputSourceCustomerAccount.has("billingAccount")){

                                                sourceCustomerAccountBillingAccount = inputSourceCustomerAccount.getJSONObject("billingAccount");

                                                if (sourceCustomerAccountBillingAccount != null){

                                                    if (sourceCustomerAccountBillingAccount.has("existingFlag")){
                                                        billingAccount.setExistingFlag(sourceCustomerAccountBillingAccount.getBoolean("existingFlag"));
                                                    }

                                                    if (sourceCustomerAccountBillingAccount.has("billingAccountId")){
                                                        billingAccount.setBillingAccountId(sourceCustomerAccountBillingAccount.getString("billingAccountId"));
                                                    }

                                                    if (sourceCustomerAccountBillingAccount.has("paymentProfile")){
                                                        billingAccount.setPaymentProfile(sourceCustomerAccountBillingAccount.getString("paymentProfile"));
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
                                                    if (sourceCustomerAccountBillingAccount.has("billingAddress")){
                                                        sourceCustomerAccountBillingAccountBillingAddress = sourceCustomerAccountBillingAccount.getJSONObject("billingAddress");
                                
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("title")){
                                                            billingAddress.setTitle(sourceCustomerAccountBillingAccountBillingAddress.getString("title"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("firstName")){
                                                            billingAddress.setFirstName(sourceCustomerAccountBillingAccountBillingAddress.getString("firstName"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("lastName")){
                                                            billingAddress.setLastName(sourceCustomerAccountBillingAccountBillingAddress.getString("lastName"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("companyTitle")){
                                                            billingAddress.setCompanyTitle(sourceCustomerAccountBillingAccountBillingAddress.getString("companyTitle"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("company")){
                                                            billingAddress.setCompany(sourceCustomerAccountBillingAccountBillingAddress.getString("company"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("postalCode")){
                                                            billingAddress.setPostalCode(sourceCustomerAccountBillingAccountBillingAddress.getString("postalCode"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("countryCode")){
                                                            billingAddress.setCountryCode(sourceCustomerAccountBillingAccountBillingAddress.getString("countryCode"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("khet")){
                                                            billingAddress.setKhet(sourceCustomerAccountBillingAccountBillingAddress.getString("khet"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("kwang")){
                                                            billingAddress.setKwang(sourceCustomerAccountBillingAccountBillingAddress.getString("kwang"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("building")){
                                                            billingAddress.setBuilding(sourceCustomerAccountBillingAccountBillingAddress.getString("building"));
                                                        }
                                                        
                                                        
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("country")){
                                                            billingAddress.setCountry(sourceCustomerAccountBillingAccountBillingAddress.getString("country"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("houseNumber")){
                                                            billingAddress.setHouseNumber(sourceCustomerAccountBillingAccountBillingAddress.getString("houseNumber"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("khetAmphur")){
                                                            billingAddress.setKhetAmphur(sourceCustomerAccountBillingAccountBillingAddress.getString("khetAmphur"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("kwangTambon")){
                                                            billingAddress.setKwangTambon(sourceCustomerAccountBillingAccountBillingAddress.getString("kwangTambon"));
                                                        }

                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("moo")){
                                                            billingAddress.setMoo(sourceCustomerAccountBillingAccountBillingAddress.getString("moo"));
                                                        }
                                
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("postCode")){
                                                            billingAddress.setPostCode(sourceCustomerAccountBillingAccountBillingAddress.getString("postCode"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("province")){
                                                            billingAddress.setProvince(sourceCustomerAccountBillingAccountBillingAddress.getString("province"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("road")){
                                                            billingAddress.setRoad(sourceCustomerAccountBillingAccountBillingAddress.getString("road"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("troksoi")){
                                                            billingAddress.setTroksoi(sourceCustomerAccountBillingAccountBillingAddress.getString("troksoi"));
                                                        }
                                
                                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("village")){
                                                            billingAddress.setVillage(sourceCustomerAccountBillingAccountBillingAddress.getString("village"));
                                                        }
                                                    }

                                                    // billing delivery address
                                                    if (sourceCustomerAccountBillingAccount.has("billDeliveryAddress")){
                                                        sourceCustomerAccountBillDeliveryAddress = sourceCustomerAccountBillingAccount.getJSONObject("billDeliveryAddress");

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("title")){
                                                            billDeliveryAddress.setTitle(sourceCustomerAccountBillDeliveryAddress.getString("title"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("firstName")){
                                                            billDeliveryAddress.setFirstName(sourceCustomerAccountBillDeliveryAddress.getString("firstName"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("lastName")){
                                                            billDeliveryAddress.setLastName(sourceCustomerAccountBillDeliveryAddress.getString("lastName"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("companyTitle")){
                                                            billDeliveryAddress.setCompanyTitle(sourceCustomerAccountBillDeliveryAddress.getString("companyTitle"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("company")){
                                                            billDeliveryAddress.setCompany(sourceCustomerAccountBillDeliveryAddress.getString("company"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("postalCode")){
                                                            billDeliveryAddress.setPostalCode(sourceCustomerAccountBillDeliveryAddress.getString("postalCode"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountBillDeliveryAddress.has("countryCode")){
                                                            billDeliveryAddress.setCountryCode(sourceCustomerAccountBillDeliveryAddress.getString("countryCode"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("khet")){
                                                            billDeliveryAddress.setKhet(sourceCustomerAccountBillDeliveryAddress.getString("khet"));
                                                        }

                                                        if (sourceCustomerAccountBillDeliveryAddress.has("kwang")){
                                                            billDeliveryAddress.setKwang(sourceCustomerAccountBillDeliveryAddress.getString("kwang"));
                                                        }
                                                        
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

                                                    // vat address
                                                    if (sourceCustomerAccountBillingAccount.has("vatAddress")){
                                                        sourceCustomerAccountVatAddress = sourceCustomerAccountBillingAccount.getJSONObject("vatAddress");

                                                        if (sourceCustomerAccountVatAddress.has("title")){
                                                            vatAddress.setTitle(sourceCustomerAccountVatAddress.getString("title"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("firstName")){
                                                            vatAddress.setFirstName(sourceCustomerAccountVatAddress.getString("firstName"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("lastName")){
                                                            vatAddress.setLastName(sourceCustomerAccountVatAddress.getString("lastName"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("companyTitle")){
                                                            vatAddress.setCompanyTitle(sourceCustomerAccountVatAddress.getString("companyTitle"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("company")){
                                                            vatAddress.setCompany(sourceCustomerAccountVatAddress.getString("company"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("postalCode")){
                                                            vatAddress.setPostalCode(sourceCustomerAccountVatAddress.getString("postalCode"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountVatAddress.has("countryCode")){
                                                            vatAddress.setCountryCode(sourceCustomerAccountVatAddress.getString("countryCode"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("khet")){
                                                            vatAddress.setKhet(sourceCustomerAccountVatAddress.getString("khet"));
                                                        }
                
                                                        if (sourceCustomerAccountVatAddress.has("kwang")){
                                                            vatAddress.setKwang(sourceCustomerAccountVatAddress.getString("kwang"));
                                                        }
                                                        
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

                                                    // vat delivery address
                                                    if (sourceCustomerAccountBillingAccount.has("vatDeliveryAddress")){
                                                        sourceCustomerAccountVatDeliveryAddress = sourceCustomerAccountBillingAccount.getJSONObject("vatDeliveryAddress");

                                                        if (sourceCustomerAccountVatDeliveryAddress.has("title")){
                                                            vatDeliveryAddress.setTitle(sourceCustomerAccountVatDeliveryAddress.getString("title"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("firstName")){
                                                            vatDeliveryAddress.setFirstName(sourceCustomerAccountVatDeliveryAddress.getString("firstName"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("lastName")){
                                                            vatDeliveryAddress.setLastName(sourceCustomerAccountVatDeliveryAddress.getString("lastName"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("companyTitle")){
                                                            vatDeliveryAddress.setCompanyTitle(sourceCustomerAccountVatDeliveryAddress.getString("companyTitle"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("company")){
                                                            vatDeliveryAddress.setCompany(sourceCustomerAccountVatDeliveryAddress.getString("company"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("postalCode")){
                                                            vatDeliveryAddress.setPostalCode(sourceCustomerAccountVatDeliveryAddress.getString("postalCode"));
                                                        }
                                                        
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("countryCode")){
                                                            vatDeliveryAddress.setCountryCode(sourceCustomerAccountVatDeliveryAddress.getString("countryCode"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("khet")){
                                                            vatDeliveryAddress.setKhet(sourceCustomerAccountVatDeliveryAddress.getString("khet"));
                                                        }
                
                                                        if (sourceCustomerAccountVatDeliveryAddress.has("kwang")){
                                                            vatDeliveryAddress.setKwang(sourceCustomerAccountVatDeliveryAddress.getString("kwang"));
                                                        }

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
                                
                                                }
                                            }


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
                        
                                            if (inputSourceCustomerAccount.has("custaccountId")){
                                                destinationCustomerAccount.setCustAccountId(inputSourceCustomerAccount.getString("custaccountId"));
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

                                            // set detail billing account
                                            billingAccount.setBillingInfo(billingInfo);
                                            billingAccount.setBillingAddress(billingAddress);
                                            billingAccount.setBillDeliveryAddress(billDeliveryAddress);
                                            billingAccount.setVatAddress(vatAddress);
                                            billingAccount.setVatDeliveryAddress(vatDeliveryAddress);
                                            
                                            // set address
                                            destinationCustomerAccount.setAddress(address);
                                            // set billing account
                                            destinationCustomerAccount.setBillingAccount(billingAccount);

                                            evenItem.setDestinationCustomerAccount(destinationCustomerAccount);

                                        }
                                        
                                    }

                                }catch(Exception e){
                                    throw new Exception("loop sourceCustomerAccount main error: " + e.getMessage());
                                }

                                // Balance transfer info
                                JSONObject orderItemBalanceTransferInfo = new JSONObject();
                                if (orderItem.has("balanceTransferInfo")){
                                    orderItemBalanceTransferInfo = orderItem.getJSONObject("balanceTransferInfo");
                                
                                    BalanceTransferInfo balanceTransferInfo = new BalanceTransferInfo();
                                    if (orderItemBalanceTransferInfo.has("transferTotalFlag") ){
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

                                // append eventItem
                                if(evenItem != null) {
                                    evenItems.add(evenItem);
                                }
                                
                            }
                            // EventItem
                            omEv.setEventItems(evenItems);
                        }catch (Exception e){
                            throw new Exception("event item loop mapping error: " + e.getMessage());
                        }
                    }
                }catch (Exception e){
                    throw new Exception("get eventitem main mapping error: " + e.getMessage());
                }

                /*
                * SourceCustomerAccount
                */
                // SourceCustomerAccount sourceCustomerAccount = new SourceCustomerAccount();
                SourceCustomerAccount sourceCustomerAccount = new SourceCustomerAccount();
                Address address = new Address();
                BillingAccount billingAccount = new BillingAccount();
                BillingInfo billingInfo = new BillingInfo();
                BillingAddress billingAddress = new BillingAddress();
                BillDeliveryAddress billDeliveryAddress = new BillDeliveryAddress();
                VatAddress vatAddress = new VatAddress();
                VatDeliveryAddress vatDeliveryAddress = new VatDeliveryAddress();

                JSONObject inputSourceCustomerAccount = null;
                JSONObject inputSourceCustomerAccountAddress = null;
                JSONObject sourceCustomerAccountBillingAccount = null;
                JSONObject sourceCustomerAccountBillDeliveryAddress = null;
                JSONObject sourceCustomerAccountBillingAccountBillingInfo = null;
                JSONObject sourceCustomerAccountBillingAccountBillingAddress = null;
                JSONObject sourceCustomerAccountVatAddress = null;
                JSONObject sourceCustomerAccountVatDeliveryAddress = null;
                
                
                try{
                    if (inputData.has("sourceCustomerAccount")){
                        inputSourceCustomerAccount = inputData.getJSONObject("sourceCustomerAccount");

                        if (inputSourceCustomerAccount != null){
                            if (inputSourceCustomerAccount.has("address")){
                                inputSourceCustomerAccountAddress = inputSourceCustomerAccount.getJSONObject("address");

                                if (inputSourceCustomerAccountAddress.has("title")){
                                    address.setTitle(inputSourceCustomerAccountAddress.getString("title"));
                                }

                                if (inputSourceCustomerAccountAddress.has("firstName")){
                                    address.setFirstName(inputSourceCustomerAccountAddress.getString("firstName"));
                                }

                                if (inputSourceCustomerAccountAddress.has("lastName")){
                                    address.setLastName(inputSourceCustomerAccountAddress.getString("lastName"));
                                }

                                if (inputSourceCustomerAccountAddress.has("companyTitle")){
                                    address.setCompanyTitle(inputSourceCustomerAccountAddress.getString("companyTitle"));
                                }

                                if (inputSourceCustomerAccountAddress.has("company")){
                                    address.setCompany(inputSourceCustomerAccountAddress.getString("company"));
                                }

                                if (inputSourceCustomerAccountAddress.has("postalCode")){
                                    address.setPostalCode(inputSourceCustomerAccountAddress.getString("postalCode"));
                                }
                                
                                if (inputSourceCustomerAccountAddress.has("countryCode")){
                                    address.setCountryCode(inputSourceCustomerAccountAddress.getString("countryCode"));
                                }

                                if (inputSourceCustomerAccountAddress.has("khet")){
                                    address.setKhet(inputSourceCustomerAccountAddress.getString("khet"));
                                }

                                if (inputSourceCustomerAccountAddress.has("kwang")){
                                    address.setKwang(inputSourceCustomerAccountAddress.getString("kwang"));
                                }

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

                                
                            }

                            // Billing Account
                            if (inputSourceCustomerAccount.has("billingAccount")){

                                sourceCustomerAccountBillingAccount = inputSourceCustomerAccount.getJSONObject("billingAccount");

                                if (sourceCustomerAccountBillingAccount != null){

                                    if (sourceCustomerAccountBillingAccount.has("existingFlag")){
                                        billingAccount.setExistingFlag(sourceCustomerAccountBillingAccount.getBoolean("existingFlag"));
                                    }

                                    if (sourceCustomerAccountBillingAccount.has("billingAccountId")){
                                        billingAccount.setBillingAccountId(sourceCustomerAccountBillingAccount.getString("billingAccountId"));
                                    }

                                    if (sourceCustomerAccountBillingAccount.has("paymentProfile")){
                                        billingAccount.setPaymentProfile(sourceCustomerAccountBillingAccount.getString("paymentProfile"));
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
                                    if (sourceCustomerAccountBillingAccount.has("billingAddress")){
                                        sourceCustomerAccountBillingAccountBillingAddress = sourceCustomerAccountBillingAccount.getJSONObject("billingAddress");
                
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("title")){
                                            billingAddress.setTitle(sourceCustomerAccountBillingAccountBillingAddress.getString("title"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("firstName")){
                                            billingAddress.setFirstName(sourceCustomerAccountBillingAccountBillingAddress.getString("firstName"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("lastName")){
                                            billingAddress.setLastName(sourceCustomerAccountBillingAccountBillingAddress.getString("lastName"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("companyTitle")){
                                            billingAddress.setCompanyTitle(sourceCustomerAccountBillingAccountBillingAddress.getString("companyTitle"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("company")){
                                            billingAddress.setCompany(sourceCustomerAccountBillingAccountBillingAddress.getString("company"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("postalCode")){
                                            billingAddress.setPostalCode(sourceCustomerAccountBillingAccountBillingAddress.getString("postalCode"));
                                        }
                                        
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("countryCode")){
                                            billingAddress.setCountryCode(sourceCustomerAccountBillingAccountBillingAddress.getString("countryCode"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("khet")){
                                            billingAddress.setKhet(sourceCustomerAccountBillingAccountBillingAddress.getString("khet"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("kwang")){
                                            billingAddress.setKwang(sourceCustomerAccountBillingAccountBillingAddress.getString("kwang"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("building")){
                                            billingAddress.setBuilding(sourceCustomerAccountBillingAccountBillingAddress.getString("building"));
                                        }
                                        
                                        
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("country")){
                                            billingAddress.setCountry(sourceCustomerAccountBillingAccountBillingAddress.getString("country"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("houseNumber")){
                                            billingAddress.setHouseNumber(sourceCustomerAccountBillingAccountBillingAddress.getString("houseNumber"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("khetAmphur")){
                                            billingAddress.setKhetAmphur(sourceCustomerAccountBillingAccountBillingAddress.getString("khetAmphur"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("kwangTambon")){
                                            billingAddress.setKwangTambon(sourceCustomerAccountBillingAccountBillingAddress.getString("kwangTambon"));
                                        }

                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("moo")){
                                            billingAddress.setMoo(sourceCustomerAccountBillingAccountBillingAddress.getString("moo"));
                                        }
                
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("postCode")){
                                            billingAddress.setPostCode(sourceCustomerAccountBillingAccountBillingAddress.getString("postCode"));
                                        }
                                        
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("province")){
                                            billingAddress.setProvince(sourceCustomerAccountBillingAccountBillingAddress.getString("province"));
                                        }
                                        
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("road")){
                                            billingAddress.setRoad(sourceCustomerAccountBillingAccountBillingAddress.getString("road"));
                                        }
                                        
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("troksoi")){
                                            billingAddress.setTroksoi(sourceCustomerAccountBillingAccountBillingAddress.getString("troksoi"));
                                        }
                
                                        if (sourceCustomerAccountBillingAccountBillingAddress.has("village")){
                                            billingAddress.setVillage(sourceCustomerAccountBillingAccountBillingAddress.getString("village"));
                                        }
                                    }

                                    // billing delivery address
                                    if (sourceCustomerAccountBillingAccount.has("billDeliveryAddress")){
                                        sourceCustomerAccountBillDeliveryAddress = sourceCustomerAccountBillingAccount.getJSONObject("billDeliveryAddress");

                                        if (sourceCustomerAccountBillDeliveryAddress.has("title")){
                                            billDeliveryAddress.setTitle(sourceCustomerAccountBillDeliveryAddress.getString("title"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("firstName")){
                                            billDeliveryAddress.setFirstName(sourceCustomerAccountBillDeliveryAddress.getString("firstName"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("lastName")){
                                            billDeliveryAddress.setLastName(sourceCustomerAccountBillDeliveryAddress.getString("lastName"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("companyTitle")){
                                            billDeliveryAddress.setCompanyTitle(sourceCustomerAccountBillDeliveryAddress.getString("companyTitle"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("company")){
                                            billDeliveryAddress.setCompany(sourceCustomerAccountBillDeliveryAddress.getString("company"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("postalCode")){
                                            billDeliveryAddress.setPostalCode(sourceCustomerAccountBillDeliveryAddress.getString("postalCode"));
                                        }
                                        
                                        if (sourceCustomerAccountBillDeliveryAddress.has("countryCode")){
                                            billDeliveryAddress.setCountryCode(sourceCustomerAccountBillDeliveryAddress.getString("countryCode"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("khet")){
                                            billDeliveryAddress.setKhet(sourceCustomerAccountBillDeliveryAddress.getString("khet"));
                                        }

                                        if (sourceCustomerAccountBillDeliveryAddress.has("kwang")){
                                            billDeliveryAddress.setKwang(sourceCustomerAccountBillDeliveryAddress.getString("kwang"));
                                        }
                                        
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

                                    // vat address
                                    if (sourceCustomerAccountBillingAccount.has("vatAddress")){
                                        sourceCustomerAccountVatAddress = sourceCustomerAccountBillingAccount.getJSONObject("vatAddress");

                                        if (sourceCustomerAccountVatAddress.has("title")){
                                            vatAddress.setTitle(sourceCustomerAccountVatAddress.getString("title"));
                                        }else{
                                            vatAddress.setTitle(address.getTitle());
                                        }

                                        if (sourceCustomerAccountVatAddress.has("firstName")){
                                            vatAddress.setFirstName(sourceCustomerAccountVatAddress.getString("firstName"));
                                        }else{
                                            vatAddress.setFirstName(address.getFirstName());
                                        }

                                        if (sourceCustomerAccountVatAddress.has("lastName")){
                                            vatAddress.setLastName(sourceCustomerAccountVatAddress.getString("lastName"));
                                        }else{
                                            vatAddress.setLastName(address.getLastName());
                                        }

                                        if (sourceCustomerAccountVatAddress.has("companyTitle")){
                                            vatAddress.setCompanyTitle(sourceCustomerAccountVatAddress.getString("companyTitle"));
                                        }else{
                                            vatAddress.setCompanyTitle(address.getCompanyTitle());
                                        }

                                        if (sourceCustomerAccountVatAddress.has("company")){
                                            vatAddress.setCompany(sourceCustomerAccountVatAddress.getString("company"));
                                        }else{
                                            vatAddress.setCompany(address.getCompany());
                                        }

                                        if (sourceCustomerAccountVatAddress.has("postalCode")){
                                            vatAddress.setPostalCode(sourceCustomerAccountVatAddress.getString("postalCode"));
                                        }else{
                                            vatAddress.setPostalCode(address.getPostalCode());
                                        }
                                        
                                        if (sourceCustomerAccountVatAddress.has("countryCode")){
                                            vatAddress.setCountryCode(sourceCustomerAccountVatAddress.getString("countryCode"));
                                        }else{
                                            vatAddress.setCountryCode(address.getCountryCode());
                                        }

                                        if (sourceCustomerAccountVatAddress.has("khet")){
                                            vatAddress.setKhet(sourceCustomerAccountVatAddress.getString("khet"));
                                        }else{
                                            vatAddress.setKhet(address.getKhet());
                                        }

                                        if (sourceCustomerAccountVatAddress.has("kwang")){
                                            vatAddress.setKwang(sourceCustomerAccountVatAddress.getString("kwang"));
                                        }else{
                                            vatAddress.setKwang(address.getKwang());
                                        }

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
                                    if (sourceCustomerAccountBillingAccount.has("vatDeliveryAddress")){
                                        sourceCustomerAccountVatDeliveryAddress = sourceCustomerAccountBillingAccount.getJSONObject("vatDeliveryAddress");

                                        if (sourceCustomerAccountVatDeliveryAddress.has("title")){
                                            vatDeliveryAddress.setTitle(sourceCustomerAccountVatDeliveryAddress.getString("title"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("firstName")){
                                            vatDeliveryAddress.setFirstName(sourceCustomerAccountVatDeliveryAddress.getString("firstName"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("lastName")){
                                            vatDeliveryAddress.setLastName(sourceCustomerAccountVatDeliveryAddress.getString("lastName"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("companyTitle")){
                                            vatDeliveryAddress.setCompanyTitle(sourceCustomerAccountVatDeliveryAddress.getString("companyTitle"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("company")){
                                            vatDeliveryAddress.setCompany(sourceCustomerAccountVatDeliveryAddress.getString("company"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("postalCode")){
                                            vatDeliveryAddress.setPostalCode(sourceCustomerAccountVatDeliveryAddress.getString("postalCode"));
                                        }
                                        
                                        if (sourceCustomerAccountVatDeliveryAddress.has("countryCode")){
                                            vatDeliveryAddress.setCountryCode(sourceCustomerAccountVatDeliveryAddress.getString("countryCode"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("khet")){
                                            vatDeliveryAddress.setKhet(sourceCustomerAccountVatDeliveryAddress.getString("khet"));
                                        }

                                        if (sourceCustomerAccountVatDeliveryAddress.has("kwang")){
                                            vatDeliveryAddress.setKwang(sourceCustomerAccountVatDeliveryAddress.getString("kwang"));
                                        }

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
                
                                }
                            }


                            if (inputSourceCustomerAccount.has("cardNumber")){
                                sourceCustomerAccount.setCardNumber(inputSourceCustomerAccount.getString("cardNumber"));
                            }
        
                            if (inputSourceCustomerAccount.has("cardType")){
                                sourceCustomerAccount.setCardType(inputSourceCustomerAccount.getInt("cardType"));
                            }
        
                            if (inputSourceCustomerAccount.has("catEmployeeFlag")){
                                sourceCustomerAccount.setCatEmployeeFlag(inputSourceCustomerAccount.getInt("catEmployeeFlag"));
                            }
        
                            if (inputSourceCustomerAccount.has("companyBranchId")){
                                sourceCustomerAccount.setCompanyBranchId(inputSourceCustomerAccount.getString("companyBranchId"));
                            }
        
                            if (inputSourceCustomerAccount.has("companyName")){
                                sourceCustomerAccount.setCompanyName(inputSourceCustomerAccount.getString("companyName"));
                            }
        
                            if (inputSourceCustomerAccount.has("companyType")){
                                sourceCustomerAccount.setCompanyType(inputSourceCustomerAccount.getInt("companyType"));
                            }
        
                            if (inputSourceCustomerAccount.has("contactNumber")){
                                sourceCustomerAccount.setContactNumber(inputSourceCustomerAccount.getString("contactNumber"));
                            }
        
                            if (inputSourceCustomerAccount.has("custaccountId")){
                                sourceCustomerAccount.setCustAccountId(inputSourceCustomerAccount.getString("custaccountId"));
                            }
        
                            if (inputSourceCustomerAccount.has("customerFocus")){
                                sourceCustomerAccount.setCustomerFocus(inputSourceCustomerAccount.getString("customerFocus"));
                            }
        
                            if (inputSourceCustomerAccount.has("customerGroup")){
                                sourceCustomerAccount.setCustomerGroup(inputSourceCustomerAccount.getInt("customerGroup"));
                            }
        
                            if (inputSourceCustomerAccount.has("customerId")){
                                sourceCustomerAccount.setCustomerId(inputSourceCustomerAccount.getString("customerId"));
                            }
        
                            if (inputSourceCustomerAccount.has("customerInfoType")){
                                sourceCustomerAccount.setCustomerInfoType(inputSourceCustomerAccount.getInt("customerInfoType"));
                            }
        
                            if (inputSourceCustomerAccount.has("customerSegment")){
                                sourceCustomerAccount.setCustomerSegment(inputSourceCustomerAccount.getInt("customerSegment"));
                            }
        
                            if (inputSourceCustomerAccount.has("customerType")){
                                sourceCustomerAccount.setCustomerType(inputSourceCustomerAccount.getInt("customerType"));
                            }
        
                            if (inputSourceCustomerAccount.has("dob")){
                                sourceCustomerAccount.setDob(inputSourceCustomerAccount.getString("dob"));
                            }
        
                            if (inputSourceCustomerAccount.has("documentNumber")){
                                sourceCustomerAccount.setDocumentNumber(inputSourceCustomerAccount.getString("documentNumber"));
                            }
        
                            if (inputSourceCustomerAccount.has("documentType")){
                                sourceCustomerAccount.setDocumentType(inputSourceCustomerAccount.getInt("documentType"));
                            }
        
                            if (inputSourceCustomerAccount.has("emailAddress")){
                                sourceCustomerAccount.setEmailAddress(inputSourceCustomerAccount.getString("emailAddress"));
                            }
        
                            if (inputSourceCustomerAccount.has("existingFlag")){
                                sourceCustomerAccount.setExistingFlag(inputSourceCustomerAccount.getBoolean("existingFlag"));
                            }
        
                            if (inputSourceCustomerAccount.has("firstName")){
                                sourceCustomerAccount.setFirstName(inputSourceCustomerAccount.getString("firstName"));
                            }
        
                            if (inputSourceCustomerAccount.has("gender")){
                                sourceCustomerAccount.setGender(inputSourceCustomerAccount.getInt("gender"));
                            }
        
                            if (inputSourceCustomerAccount.has("ivrLanguage")){
                                sourceCustomerAccount.setIvrLanguage(inputSourceCustomerAccount.getString("ivrLanguage"));
                            }
        
                            if (inputSourceCustomerAccount.has("lastName")){
                                sourceCustomerAccount.setLastName(inputSourceCustomerAccount.getString("lastName"));
                            }
        
                            if (inputSourceCustomerAccount.has("nationality")){
                                sourceCustomerAccount.setNationality(inputSourceCustomerAccount.getString("nationality"));
                            }
        
                            if (inputSourceCustomerAccount.has("taxRegisterNumber")){
                                sourceCustomerAccount.setTaxRegisterNumber(inputSourceCustomerAccount.getString("taxRegisterNumber"));
                            }
        
                            if (inputSourceCustomerAccount.has("title")){
                                sourceCustomerAccount.setTitle(inputSourceCustomerAccount.getInt("title"));
                            }
        
                            if (inputSourceCustomerAccount.has("writtenLanguage")){
                                sourceCustomerAccount.setWrittenLanguage(inputSourceCustomerAccount.getString("writtenLanguage"));
                            }

                            // set detail billing account
                            billingAccount.setBillingInfo(billingInfo);
                            billingAccount.setBillingAddress(billingAddress);
                            billingAccount.setBillDeliveryAddress(billDeliveryAddress);
                            billingAccount.setVatAddress(vatAddress);
                            billingAccount.setVatDeliveryAddress(vatDeliveryAddress);
                            
                            // set address
                            sourceCustomerAccount.setAddress(address);
                            // set billing account
                            sourceCustomerAccount.setBillingAccount(billingAccount);

                            omEv.setSourceCustomerAccount(sourceCustomerAccount);

                        }
                        
                    }

                }catch(Exception e){
                    throw new Exception("loop sourceCustomerAccount main error: " + e.getMessage());
                }




                // saleInfo
                if(inputData.has("saleInfo")){
                    JSONObject inputSaleInfo = inputData.getJSONObject("saleInfo");
                    SaleInfo saleInfo = new SaleInfo();

                    if (inputSaleInfo.has("saleEmpId")){
                        saleInfo.setSaleEmpId(inputSaleInfo.getString("saleEmpId"));
                    }

                    if (inputSaleInfo.has("sapCode")){
                        saleInfo.setSapCode(inputSaleInfo.getString("sapCode"));
                    }

                    if (inputSaleInfo.has("dealerCode")){
                        saleInfo.setDealerCode(inputSaleInfo.getString("dealerCode"));
                    }

                    if (inputSaleInfo.has("registerBySellerName")){
                        saleInfo.setRegisterBySellerName(inputSaleInfo.getString("registerBySellerName"));
                    }

                    if (inputSaleInfo.has("saleRole")){
                        saleInfo.setSaleRole(inputSaleInfo.getString("saleRole"));
                    }

                    if (inputSaleInfo.has("registerProvince")){
                        saleInfo.setRegisterProvince(inputSaleInfo.getString("registerProvince"));
                    }

                    if (inputSaleInfo.has("territoryName")){
                        saleInfo.setTerritoryName(inputSaleInfo.getString("territoryName"));
                    }

                    if (inputSaleInfo.has("saleRepEmpId")){
                        saleInfo.setSaleRepEmpId(inputSaleInfo.getString("saleRepEmpId"));
                    }

                    if (inputSaleInfo.has("saleRepSapCode")){
                        saleInfo.setSaleRepSapCode(inputSaleInfo.getString("saleRepSapCode"));
                    }

                    if (inputSaleInfo.has("verifyIdentity")){
                        saleInfo.setVerifyIdentity(inputSaleInfo.getBoolean("verifyIdentity"));
                    }
                    
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
            }
        }catch(Exception e){
            throw new Exception("mapp orderheader error :"+e.getMessage());
        }
        
        omEv.setOrderStatus(odheader.getOrderStatus());
        omEv.setSubmitedDate(odheader.getCreateDate());
        omEv.setCompletedDate(odheader.getUpdateDate());

        sendData.setTriggerDate(triggerDate);
        sendData.setPublishChannel("OM-MFE");
        sendData.setOrderType(orderTypeName);
        sendData.setMsisdn(String.format("0%s", odheader.getMsisdn())); // your code here
        sendData.setEventData(omEv);

        return sendData;
        
    }

    private Data MappingTopUpData(ReceiveTopUpDataType receivedData, String orderTypeName){
        String effectiveDate = DateTime.getTriggerTimeStampNow();
        String triggerDate = DateTime.getTimeStampNowStr();

        Data sendData = new Data();

        EventData topUpEv = new EventData();
        topUpEv.setEventType(orderTypeName);

        List<EventItem> eventItems = new ArrayList<EventItem>();
        EventItem eventItem = new EventItem();
        eventItem.setItemType("Topup");
        eventItem.setExecutionType("Now");
        eventItem.setEffectiveDate(effectiveDate);

        // EventItem TopUp 
        TopUp topUp = new TopUp();
        topUp.setTopupType(receivedData.getRechargeType());
        topUp.setRechargeAmount(Integer.valueOf(receivedData.getRechargeAmount()));
        topUp.setCurrencyId(receivedData.getCurrencyId());
        topUp.setChannelId(receivedData.getChannelId());
        topUp.setRechargeDate(receivedData.getRechargeDate());
        topUp.setRechargeType(receivedData.getRechargeType());
        topUp.setNotiMsgSeq(receivedData.getNotiMsgSeq());
        topUp.setRechargeLogId(receivedData.getRechargeLogId());
        eventItem.setTopUp(topUp);
        eventItems.add(eventItem);
        
        topUpEv.setEventItems(eventItems);

        sendData.setTriggerDate(triggerDate);
        sendData.setOrderID(receivedData.getNotiMsgSeq());
        sendData.setPublishChannel("Topup-GW");
        sendData.setOrderType("TOPUPRECHARGE");
        sendData.setMsisdn(String.format("0%s", receivedData.getMsisdn()));
        sendData.setEventData(topUpEv);

        return sendData;
    }

    private Data MappingExpiredData(ReceiveExpiredDataType receivedData, String orderTypeName ) throws SQLException{
        // System.out.println(receivedData.toString());

        String effectiveDate = DateTime.getTriggerTimeStampNow();
        String triggerDate = DateTime.getTimeStampNowStr();

        Data sendData = new Data();

        EventData expiredEv = new EventData();

        expiredEv.setEventType(orderTypeName);

        List<EventItem> eventItems = new ArrayList<>();
        EventItem eventItem = new EventItem();

        eventItem.setItemType("PackageExpire"); // Fix
        eventItem.setExecutionType("Now");
        eventItem.setEffectiveDate(effectiveDate);

        /*  
        * Expire data 
        */
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
        List<Offer> offers = new ArrayList<Offer>();


        // Add from POID to offer
        Offer offer = new Offer();
        String offeringId = receivedData.getPoId()+"";

        OfferingSpecClientResp ofrspecResp = catmfeService.getOfferingSpecByOfferingId(offeringId);
        OfferingSpecData ofrspec = ofrspecResp.getData();

        if (ofrspec != null){
            offer.setOfferingId(offeringId);
            // System.out.println("offeringId:"+offeringId);

            offer.setOfferingType("PO");

            offer.setOfferingNameTh(ofrspec.getOfferingnameTH());

            offer.setOfferingNameEn(ofrspec.getOfferingnameEN());

            offer.setPackageId(ofrspec.getPackageID());

            offer.setPackageName(ofrspec.getPackageName());

            offer.setDescriptionTh(ofrspec.getDescTH());

            offer.setDescriptionEn(ofrspec.getDescEN());

            offer.setServiceType(Integer.valueOf(ofrspec.getServiceType()));

            offer.setOcsOfferingName(ofrspec.getOcsofferingname());

            if(ofrspec.getRcamount() != null){
                BigDecimal rcamount = new BigDecimal(ofrspec.getRcamount());
                offer.setRcAmount(rcamount);
            }

            if(ofrspec.getRcvatamount() != null){
                BigDecimal rcvatamount = new BigDecimal(ofrspec.getRcvatamount());
                offer.setRcVatAmount(rcvatamount);
            }

            offer.setPeriod(ofrspec.getPeriod());

            offer.setUnitPeriod(ofrspec.getUnitperiod());

            offer.setSaleStartDate(ofrspec.getSalestartdate());

            offer.setSaleEndDate(ofrspec.getSaleenddate());

            if(ofrspec.getMaxdayafteractivedate()!=null){
                BigDecimal maxdayafteractivedate = new BigDecimal(ofrspec.getMaxdayafteractivedate());
                offer.setMaxDayAfterActiveDate(maxdayafteractivedate);
            }

            offer.setNiceNumberFlag(ofrspec.getNicenumberflag());

            if(ofrspec.getNicenumberlevel()!= null){
                BigDecimal nicenumberlevel = new BigDecimal(ofrspec.getNicenumberlevel());
                offer.setNiceNumberLevel(nicenumberlevel);
            }

            offer.setConTractFlag(ofrspec.getContractflag());

            if(ofrspec.getContractunitperiod() != null){
                BigDecimal contractunitperiod = new BigDecimal(ofrspec.getContractunitperiod());
                offer.setContractUnitPeriod(contractunitperiod);
            }

            offer.setCatEmpFlag(ofrspec.getCatempflag());

            offer.setCatRetireEmpFlag(ofrspec.getRetiredcatempflag());

            // offer.multisimFlag your code here with logic
            offer.setMultisimFlag(ofrspec.getMultisimflag());

            offer.setTopupSimFlag(ofrspec.getTopupsimflag());

            offer.setTouristSimFlag(ofrspec.getTouristsimflag());

            offer.setChangePoUssdCode(ofrspec.getChangepoussdcode());

            offer.setAddSoUssdCode(ofrspec.getAddsoussdcode());

            offer.setDeleteSoUssdCode(ofrspec.getDeletesoussdcode());

            offer.setFrequency(ofrspec.getFrequency());

            offer.setCanSwapPoFlag(ofrspec.getCanswappoflag());

            offers.add(offer);
        }
    

        // Add from expiredOfferId to offer
        Offer ofrspecExpired = new Offer();
        String expiredOfferingId = receivedData.getExpireOfferId();

        OfferingSpecClientResp expiredOfrspecResp = catmfeService.getOfferingSpecByOfferingId(expiredOfferingId);
        OfferingSpecData expiredOfrspec = expiredOfrspecResp.getData();
        if (expiredOfrspec != null){
            ofrspecExpired.setOfferingId(expiredOfrspec.getOfferingID()+"");
            // System.out.println("offeringId:"+offeringId);

            ofrspecExpired.setOfferingType("SO");

            ofrspecExpired.setOfferingNameTh(expiredOfrspec.getOfferingnameTH());

            ofrspecExpired.setOfferingNameEn(expiredOfrspec.getOfferingnameEN());

            ofrspecExpired.setPackageId(expiredOfrspec.getPackageID());

            ofrspecExpired.setPackageName(expiredOfrspec.getPackageName());

            ofrspecExpired.setDescriptionTh(expiredOfrspec.getDescTH());

            ofrspecExpired.setDescriptionEn(expiredOfrspec.getDescEN());

            ofrspecExpired.setServiceType(Integer.valueOf(expiredOfrspec.getServiceType()));

            ofrspecExpired.setOcsOfferingName(expiredOfrspec.getOcsofferingname());

            if(expiredOfrspec.getRcamount() != null){
                BigDecimal rcamount = new BigDecimal(expiredOfrspec.getRcamount());
                ofrspecExpired.setRcAmount(rcamount);
            }

            if(expiredOfrspec.getRcvatamount() != null){
                BigDecimal rcvatamount = new BigDecimal(expiredOfrspec.getRcvatamount());
                ofrspecExpired.setRcVatAmount(rcvatamount);
            }

            ofrspecExpired.setPeriod(expiredOfrspec.getPeriod());

            ofrspecExpired.setUnitPeriod(expiredOfrspec.getUnitperiod());

            ofrspecExpired.setSaleStartDate(expiredOfrspec.getSalestartdate());

            ofrspecExpired.setSaleEndDate(expiredOfrspec.getSaleenddate());

            if(expiredOfrspec.getMaxdayafteractivedate()!=null){
                BigDecimal maxdayafteractivedate = new BigDecimal(expiredOfrspec.getMaxdayafteractivedate());
                ofrspecExpired.setMaxDayAfterActiveDate(maxdayafteractivedate);
            }

            ofrspecExpired.setNiceNumberFlag(expiredOfrspec.getNicenumberflag());

            if(expiredOfrspec.getNicenumberlevel()!= null){
                BigDecimal nicenumberlevel = new BigDecimal(expiredOfrspec.getNicenumberlevel());
                ofrspecExpired.setNiceNumberLevel(nicenumberlevel);
            }

            ofrspecExpired.setConTractFlag(expiredOfrspec.getContractflag());

            if(expiredOfrspec.getContractunitperiod() != null){
                BigDecimal contractunitperiod = new BigDecimal(expiredOfrspec.getContractunitperiod());
                ofrspecExpired.setContractUnitPeriod(contractunitperiod);
            }

            ofrspecExpired.setCatEmpFlag(expiredOfrspec.getCatempflag());

            ofrspecExpired.setCatRetireEmpFlag(expiredOfrspec.getRetiredcatempflag());

            // ofrspecExpired.multisimFlag your code here with logic
            ofrspecExpired.setMultisimFlag(expiredOfrspec.getMultisimflag());

            ofrspecExpired.setTopupSimFlag(expiredOfrspec.getTopupsimflag());

            ofrspecExpired.setTouristSimFlag(expiredOfrspec.getTouristsimflag());

            ofrspecExpired.setChangePoUssdCode(expiredOfrspec.getChangepoussdcode());

            ofrspecExpired.setAddSoUssdCode(expiredOfrspec.getAddsoussdcode());

            ofrspecExpired.setDeleteSoUssdCode(expiredOfrspec.getDeletesoussdcode());

            ofrspecExpired.setFrequency(expiredOfrspec.getFrequency());

            ofrspecExpired.setCanSwapPoFlag(expiredOfrspec.getCanswappoflag());

            offers.add(ofrspecExpired);
        }
    

        // Only one EventItem [expired, offer]
        eventItem.setOffer(offers);
        eventItems.add(eventItem);
        expiredEv.setEventItems(eventItems);

        
        sendData.setOrderID(receivedData.getNotiMsgSeq());
        sendData.setTriggerDate(triggerDate);
        sendData.setPublishChannel("OM-MFE");
        sendData.setOrderType("PACKAGEEXPIRE");
        sendData.setMsisdn(String.format("0%s", receivedData.getMsisdn()));
        sendData.setEventData(expiredEv);

        return sendData;
    }


    public IMSIOfferingConfig getImsiConfigByImsi(String imsi, List<IMSIOfferingConfig> imsiConfigs){
        for (IMSIOfferingConfig config : imsiConfigs) {
            String prefix = config.getImsiPrefix();
            if (imsi.startsWith(prefix)) {
                return config;
            }
        }
        return null;
    }

    public OrderTypeEntity getOrderTypeInfoFromList(String orderType, List<OrderTypeEntity> orderTypes){
        for (OrderTypeEntity orderTypeEntity : orderTypes) {
            // System.out.println("orderTypeEntity:"+orderTypeEntity.getID()+"  , name:"+orderTypeEntity.getOrderTypeName());
            if (orderTypeEntity.getOrderTypeName().toUpperCase().equals(orderType.toUpperCase())){
                return orderTypeEntity;
            }
        }
        return null;
    }

    public SaChannelConEntity getSaChannelInfoFromList(String channelType, List<SaChannelConEntity> saChannels){
        for ( SaChannelConEntity saChannelConEntity : saChannels) {
            if (saChannelConEntity.getCHANNEL_Name().toUpperCase().equals(channelType.toUpperCase())){
                return saChannelConEntity;
            }
        }
        return null;
    }

    public String getOrderTypeNamePattern(String orderType){
        String pattern = "";
        String result = Arrays.stream(orderType.split(" ")).collect(Collectors.joining(pattern));
        return result;
    }
}
