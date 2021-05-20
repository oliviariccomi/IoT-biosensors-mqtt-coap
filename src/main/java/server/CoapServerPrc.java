package server;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resources.coap.CoapEcgResource;
import resources.raw.EcgCsvSensor;

import java.util.UUID;

public class CoapServerPrc extends CoapServer {
    private static final Logger logger = LoggerFactory.getLogger(CoapServerPrc.class);

    public CoapServerPrc() {
        super();

        EcgCsvSensor ecgCsvSensor = new EcgCsvSensor(EcgCsvSensor.FILE_ECG);

        CoapEcgResource coapEcgResource = new CoapEcgResource(ecgCsvSensor, ecgCsvSensor.getMacAddress(), "Ecg");
        this.add(coapEcgResource);
    }

    public static void main(String [] args){
        CoapServerPrc coapServerPrc = new CoapServerPrc();
        coapServerPrc.start();

        coapServerPrc.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });
    }

}
