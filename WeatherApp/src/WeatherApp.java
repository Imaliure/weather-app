import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

// Amacımız API'dan hava durumunu çekme ve sonra GUI'da kullanıcıya gösterme
public class WeatherApp {
    // girilen konum için hava durumunu çekme
    public static JSONObject getWeatherData(String locationName){
        // konumun kordinatlarini getlocation API kullanarak cek
        JSONArray locationData = getLocationData(locationName);

        // enlem ve boylam verilerini çıkarma
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // konum koordinatlarıyla API istek URL'si oluşturma
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=America%2FLos_Angeles";

        try{
            // API'yi çağır ve ger bildirim alma
            HttpURLConnection conn = fetchApiResponse(urlString);

            // geri bildirimin durumunu kontrol etme
            if(conn.getResponseCode() != 200){
                System.out.println("Error: Could not connect to API");
                return null;
            }

            // ortaya çıkan json verilerini depolama
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext()){
                // string oluşturucuda okuma ve saklama
                resultJson.append(scanner.nextLine());
            }

            // scanner'i kapatma
            scanner.close();

            // URL bağlantısını kesme
            conn.disconnect();

            // verileri ayrıştırma
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // saatlik verileri alma
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

            // şuanki saatin index'ini bulma
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // sıcaklığı alma
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            // hava durumu kodunu alma
            JSONArray weathercode = (JSONArray) hourly.get("weathercode");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            // nemi alma
            JSONArray relativeHumidity = (JSONArray) hourly.get("relativehumidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // rüzgar hızını alma
            JSONArray windspeedData = (JSONArray) hourly.get("windspeed_10m");
            double windspeed = (double) windspeedData.get(index);

            // GUI'da kullanacağımız hava durumu json nesnesini inşa etme
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData;
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    // verilen konum adı için coğrafi koordinatları alma
    public static JSONArray getLocationData(String locationName){
        // API'in istek biçimine uymak için konum adındaki boşlukları + olarak değiştirme
        locationName = locationName.replaceAll(" ", "+");

        // konum parametresiyle API URL'si oluşturma
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try{
            // API'yi çağırma ve bir yanıt alma
            HttpURLConnection conn = fetchApiResponse(urlString);

            // yanıt durumunu kontrol etme
            // 200 başarılı bağlantı anlamına gelir
            if(conn.getResponseCode() != 200){
                System.out.println("Error: Could not connect to API");
                return null;
            }else{
                // API sonuçlarını saklama
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                // ortaya çıkan json verilerini okuma ve string oluşturucumuzda saklama
                while(scanner.hasNext()){
                    resultJson.append(scanner.nextLine());
                }

                // scanner'i kapatma
                scanner.close();

                // URL bağlantısını kesme
                conn.disconnect();

                // JSON string'ini JSON nesnesine ayrıştırma
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // API'in konum adından oluşturduğu konum verilerinin listesini alma
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        // konum bulunamadı:
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString){
        try{
            // bağlantı kurmaya çalışma
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // almak için istek yöntemini ayarlama
            conn.setRequestMethod("GET");

            // API'ya bağlanma
            conn.connect();
            return conn;
        }catch(IOException e){
            e.printStackTrace();
        }

        // bağlantı kurulamadı:
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList){
        String currentTime = getCurrentTime();

        // zaman listesini yenileme ve hangisinin şimdiki zamana uyduğunu görme
        for(int i = 0; i < timeList.size(); i++){
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime)){
                // index'i döndürme
                return i;
            }
        }

        return 0;
    }

    private static String getCurrentTime(){
        // güncel tarih ve saati alma
        LocalDateTime currentDateTime = LocalDateTime.now();

        // tarihi 2023-09-02T00:00 olarak biçimlendirme (API'de bu şekilde okunur)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        // şuanki tarih ve saati biçimlendirme ve yazdırma
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }

    // hava durumu kodunu daha okunabilir hale getirme
    private static String convertWeatherCode(long weathercode){
        String weatherCondition = "";
        if(weathercode == 0L){
            // açık
            weatherCondition = "Açık";
        }else if(weathercode > 0L && weathercode <= 3L){
            // bulutlu
            weatherCondition = "Bulutlu";
        }else if((weathercode >= 51L && weathercode <= 67L)
                    || (weathercode >= 80L && weathercode <= 99L)){
            // yağmurlu
            weatherCondition = "Yağmurlu";
        }else if(weathercode >= 71L && weathercode <= 77L){
            // karlı
            weatherCondition = "Karlı";
        }

        return weatherCondition;
    }
}







