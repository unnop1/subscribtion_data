package com.nt.subscribtion_data.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.IMSIOfferingConfig;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.ListIMSIOfferingConfigClientResp;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.OrderHeaderClientResp;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.OrderHeaderData;

public class OMMYFRONTClient {
    private String host;
    private String context;

    public OMMYFRONTClient(String host, String context){
        this.host = host;
        this.context = context;
    }

    public OrderHeaderClientResp GetOfferHeaderById(String orderID){
        OrderHeaderClientResp respData = new OrderHeaderClientResp();
        try {
            URL url = new URL(String.format(
                    "http://%s%s/order_header/by_order_id?order_id=%s",
                    host,
                    context,
                    orderID
                )
            );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse JSON response into MetricsResp object using ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
                OrderHeaderData data = objectMapper.readValue(response.toString(), OrderHeaderData.class);
                respData.setData(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String urltest = String.format(
                "http://%s%s/order_header/by_order_id?order_id=%s",
                host,
                context,
                orderID
            );
            respData.setErr(e.getMessage()+" url :"+urltest);
        }
        return respData;
    }

    public OrderHeaderClientResp GetOfferHeaderByIccid(String iccid){
        OrderHeaderClientResp respData = new OrderHeaderClientResp();
        try {
            URL url = new URL(String.format(
                    "http://%s%s/order_header/by_iccid?iccid=%s",
                    host,
                    context,
                    iccid
                )
            );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse JSON response into MetricsResp object using ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
                OrderHeaderData data = objectMapper.readValue(response.toString(), OrderHeaderData.class);
                respData.setData(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            respData.setErr(e.getMessage());

        }
        return respData;
    }

    public OrderHeaderClientResp GetOfferHeaderByPoId(Long poid){
        OrderHeaderClientResp respData = new OrderHeaderClientResp();
        try {
            URL url = new URL(String.format(
                    "http://%s%s/order_header/by_poid?poid=%s",
                    host,
                    context,
                    poid
                )
            );
            // System.out.println("ommyfront url: " + url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse JSON response into MetricsResp object using ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
                OrderHeaderData data = objectMapper.readValue(response.toString(), OrderHeaderData.class);
                respData.setData(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            respData.setErr(e.getMessage());
        }
        return respData;
    }

    public ListIMSIOfferingConfigClientResp GetListIMSIOfferingConfig(){
        ListIMSIOfferingConfigClientResp respData = new ListIMSIOfferingConfigClientResp();
        try {
            URL url = new URL(String.format(
                    "http://%s%s/imsi_offering_config/list",
                    host,
                    context
                )
            );
            // System.out.println("ommyfront url: " + url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                if (response.toString().isEmpty()){
                    return null;
                }

                // Parse JSON response into MetricsResp object using ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
                List<IMSIOfferingConfig> dataList = objectMapper.readValue(response.toString(),new TypeReference<List<IMSIOfferingConfig>>() {});
                respData.setData(dataList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String urltest = String.format(
                "http://%s%s/imsi_offering_config/list",
                host,
                context
            );
            respData.setErr(e.getMessage() + "urltest:"+urltest);
        }
        return respData;
    }
}
