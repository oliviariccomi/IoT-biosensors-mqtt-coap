# IoT-biosensors-mqtt-coap
The aim of this project is to simulate a board which detects different types of biosignals such as ECG, EMG, EDA and EEG, creating then packets with all of data to publish through an MQTT mosquitto broker and CoAP server too.
The data right now are read from a csv file, then collected into a single Smart Object and thus sent to a Client which receives the data.
You might decide to use CoAP protocol or MQTT protocol and running the respective classes.

## MQTT Server
I'm working with local MQTT Broker through Docker. Both server and client agents will communicate through the centralized docker server.
## Topics
`/iot/biosensors/boardId/#` collects all data from all sensors   
`/iot/biosensors/boardId/sensor` collects data of a specific sensor

## CoAP Server
In the project it will be used `iot.californium.org`. Both server and client agents will communicate through this centralized server.

## Components
* Resources
  * Raw: sensors that produce biosegnals data;
  * CoAP resources
* Process
  * CoAP process & MQTT process where the connection is established;
* Message: I create a package with data that has to be sent
* Device
  * MQTTSmartObject: collects the sensors in an HashMap and update the values
* Descriptors
  * Sensors descriptors to collect the data from a .csv file
* Consumer
  * Simple Test Consumer (receiver that subscribes to topics and collect data)


# Application overview
![Schermata 2021-05-20 alle 22 48 16](https://user-images.githubusercontent.com/71649032/119047247-f082e600-b9bd-11eb-99e9-29be944e7f82.jpg)

# Prerequisites
## Docker Server
Docker is necessary to create the local server and to set the connection
