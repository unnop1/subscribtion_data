package com.nt.subscribtion_data.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMappingClientResp;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMappingData;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMasterData;

public class INVUSERClient {
    private String host;
    private String context="/";

    public INVUSERClient(String host, String context){
        this.host = host;
        this.context = context;
    }

    public INVMappingClientResp GetINVMappingByExternalId(String externalId){
        INVMappingClientResp respData = new INVMappingClientResp();
        try {
            URL url = new URL(String.format(
                    "http://%s%s/inv_mapping/by_id?external_id=%s",
                    host,
                    context,
                    externalId
                )
            );
            // System.out.println("inv url: " + url);
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
                INVMappingData data = objectMapper.readValue(response.toString(), INVMappingData.class);
                respData.setData(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String urltest = String.format(
                "http://%s%s/inv_mapping/by_id?external_id=%s",
                host,
                context,
                externalId
            );
            respData.setErr(e.getMessage()+" url :"+urltest);
        }
        return respData;
    }

    public List<INVMasterData> GetINVMaster(){
        List<INVMasterData> respDataList = new ArrayList<>();
        try {
            URL url = new URL(String.format(
                    "http://%s%s/inv_masters",
                    host,
                    context
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
                respDataList = objectMapper.readValue(response.toString(), new TypeReference<List<INVMasterData>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respDataList;
    }
}
