package com.nt.subscribtion_data.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.subscribtion_data.model.dao.OMUSER.TransManageContractDTLClientResp;
import com.nt.subscribtion_data.model.dao.OMUSER.TransManageContractDTLData;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
public class OMUSERClient {
    private String host;
    private String context;

    public OMUSERClient(String host, String context){
        this.host = host;
        this.context = context;
    }

    public TransManageContractDTLClientResp GetTransManageContractByTMasterId(String masterID){
        TransManageContractDTLClientResp respData = new TransManageContractDTLClientResp();
        try {
            URL url = new URL(String.format(
                    "http://%s%s/trans_manage_contract/by_trans_master_id?trans_master_id=%s",
                    host,
                    context,
                    masterID
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
                TransManageContractDTLData data = objectMapper.readValue(response.toString(), TransManageContractDTLData.class);
                respData.setData(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String urltest = String.format(
                "http://%s%s/trans_manage_contract/by_trans_master_id?trans_master_id=%s",
                host,
                context,
                masterID
            );
            respData.setErr(e.getMessage()+" url:" +urltest);
        }
        return respData;
    }
}
