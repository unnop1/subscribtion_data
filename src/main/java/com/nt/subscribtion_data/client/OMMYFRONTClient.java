package com.nt.subscribtion_data.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMasterData;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.IMSIOfferingConfig;
import com.nt.subscribtion_data.model.dao.OMMYFRONT.OrderHeaderData;

public class OMMYFRONTClient {
    private String host;
    private String port;
    private String context;

    public OMMYFRONTClient(String host, String port, String context){
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public OrderHeaderData GetOfferHeaderById(String orderID){
        OrderHeaderData respData = null;
        try {
            URL url = new URL(String.format(
                    "http://%s:%s%s/order_header/by_order_id?order_id=%s",
                    host,
                    port,
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
                ObjectMapper objectMapper = new ObjectMapper();
                respData = objectMapper.readValue(response.toString(), OrderHeaderData.class);
            } else {
                System.out.println("GET request failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respData;
    }

    public OrderHeaderData GetOfferHeaderByIccid(String iccid){
        OrderHeaderData respData = null;
        try {
            URL url = new URL(String.format(
                    "http://%s:%s%s/order_header/by_iccid?iccid=%s",
                    host,
                    port,
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
                ObjectMapper objectMapper = new ObjectMapper();
                respData = objectMapper.readValue(response.toString(), OrderHeaderData.class);
            } else {
                System.out.println("GET request failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respData;
    }

    public List<IMSIOfferingConfig> GetListIMSIOfferingConfig(){
        List<IMSIOfferingConfig> respData = new ArrayList<IMSIOfferingConfig>();
        try {
            URL url = new URL(String.format(
                    "http://%s:%s%s/imsi_offering_config/list",
                    host,
                    port,
                    context
                )
            );
            System.out.println("ommyfront url: " + url);
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
                ObjectMapper objectMapper = new ObjectMapper();
                respData = objectMapper.readValue(response.toString(),new TypeReference<List<IMSIOfferingConfig>>() {});
            } else {
                System.out.println("GET request failed.");
                System.out.println(connection.getResponseMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respData;
    }
}
