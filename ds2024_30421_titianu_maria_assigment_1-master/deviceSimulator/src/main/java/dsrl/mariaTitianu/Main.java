package dsrl.mariaTitianu;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

public class Main {

    private static final int TIME_MULTIPLIER = 600;
    private static final String qName = "DisneyChannel";

    public static void main(String[] args) throws Exception {
        long initialTime = System.currentTimeMillis();

        FileReader fileReader = new FileReader(args[0]);
        CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT);
        Map<String, Long> devices = new LinkedHashMap<>();

        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection();

        FileReader dataFile = new FileReader(args[1]);
        CSVParser dataParser = new CSVParser(dataFile, CSVFormat.DEFAULT);
        List<String> data = new ArrayList<>();

        for (CSVRecord csvRecord : csvParser) {
            devices.put(csvRecord.get(0), Long.parseLong(csvRecord.get(1)));
        }

        for (CSVRecord dataRecord : dataParser) {
            data.add(dataRecord.get(0));
        }

        Channel channel = connection.createChannel();
        channel.queueDeclare(qName, true, false, false, null);

        HashMap<String, Integer> indexes = new HashMap<>();
        Random random = new Random(2);
        for (String deviceId : devices.keySet()){
            indexes.put(deviceId, random.nextInt(0, 1000));
        }

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(devices.size());
        for (Map.Entry<String, Long> device : devices.entrySet()) {
            executorService.scheduleAtFixedRate(() -> {
                try {
                    deviceRunner(channel, new Device(
                            initialTime + (System.currentTimeMillis() - initialTime) * TIME_MULTIPLIER,
                            device.getKey(),
                            Double.parseDouble(data.get(indexes.get(device.getKey())))
                    ));
                    indexes.put(device.getKey(), indexes.get(device.getKey()) + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, device.getValue(), TimeUnit.MILLISECONDS);
        }

        while (true);
    }

    private static void deviceRunner(Channel channel, Device deviceData) throws IOException {
        Gson gson = new Gson();
        String deviceDataJson = gson.toJson(deviceData);
        channel.basicPublish(
                "",
                qName,
                null,
                deviceDataJson.getBytes(StandardCharsets.UTF_8)
        );
        System.out.println("Sent " + deviceDataJson + " on queue " + qName +
                " (equivalent timestamp " +
                LocalDateTime.ofInstant(Instant.ofEpochMilli(deviceData.getTimestamp()), ZoneId.systemDefault()) + ")");
    }
}


