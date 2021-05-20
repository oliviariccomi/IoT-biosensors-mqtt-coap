package process;

import message.EcgMessage;
import message.SensorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resources.raw.EcgCsvSensor;
import resources.raw.EdaCsvSensor;
import resources.raw.SensorResource;
import server.CoapServerProcess;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CoapSmartObjectProcess {
    private static Logger logger = LoggerFactory.getLogger(CoapSmartObjectProcess.class);
    public static void main(String[] args) {

        HashMap<String, SensorResource<?>> mapResource = new HashMap<>(){
            {
                put("ecg", new EcgCsvSensor("/Users/olivia1/Desktop/ECG_file.csv"));
                //put("eda", new EdaCsvSensor("/Users/olivia1/Desktop/EDA_prova.csv"));

            }
        };

        CoapServerProcess coapServerProcess = new CoapServerProcess(mapResource);
        coapServerProcess.start();

        coapServerProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if (!resource.getURI().equals("/.well-known")) {
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });
    }
}
