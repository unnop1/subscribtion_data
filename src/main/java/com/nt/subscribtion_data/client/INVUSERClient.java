package com.nt.subscribtion_data.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMappingData;
import com.nt.subscribtion_data.model.dao.INVUSER.INVMasterData;

public class INVUSERClient {
    private String host;
    private String port;
    private String context="/";

    public INVUSERClient(String host, String port, String context){
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public INVMappingData GetINVMappingByExternalId(String externalId){
        INVMappingData respData = null;
        try {
            URL url = new URL(String.format(
                    "http://%s:%s%s/inv_mapping/by_id?external_id=%s",
                    host,
                    port,
                    context,
                    externalId
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
                respData = objectMapper.readValue(response.toString(), INVMappingData.class);
            } else {
                System.out.println("GET request failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respData;
    }

    public List<INVMasterData> GetINVMaster(){
        List<INVMasterData> respDataList = new ArrayList<>();
        try {
            URL url = new URL(String.format(
                    "http://%s:%s%s/inv_masters",
                    host,
                    port,
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
                ObjectMapper objectMapper = new ObjectMapper();
                respDataList = objectMapper.readValue(response.toString(), new TypeReference<List<INVMasterData>>() {});
            } else {
                System.out.println("GET request failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respDataList;
    }
}
