import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;

    public WeatherAppGui(){
        // GUI'i kurma ve başlık ekleme
        super("Hava Durumu Uygulaması");

        // GUI'yi, program kapatıldıktan sonra programın işlemini sonlandıracak şekilde ayarlama
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // GUI'in boyutunu ayarlama (piksel cinsinden)
        setSize(450, 650);

        // GUI'i ekranın ortasına yükleme
        setLocationRelativeTo(null);

        // Bileşenleri GUI içinde manuel olarak konumlandırmak için düzen yöneticisini null yapma
        setLayout(null);

        // GUI'in boyutunun değişmesini önleme
        setResizable(false);

        addGuiComponents();
    }

    private void addGuiComponents(){
        // Arama alanı
        JTextField searchTextField = new JTextField();

        // component'in konumunu ve boyutunu ayarlama
        searchTextField.setBounds(15, 15, 351, 45);

        // yazı tipi stilini ve boyutunu değiştirme
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);

        // hava durumu görseli
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        // sıcaklık metni
        JLabel temperatureText = new JLabel("10 C");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));

        // yazıyı ortalama
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // hava durumu açıklaması
        JLabel weatherConditionDesc = new JLabel("Bulutlu");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        // nem görseli
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        // nem yazısı
        JLabel humidityText = new JLabel("<html><b>Nem</b> 100%</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        // rüzgar hızı görseli
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

        // rüzgar hızı yazısı
        JLabel windspeedText = new JLabel("<html><b>Rüzgar Hızı</b> 15km/h</html>");
        windspeedText.setBounds(310, 500, 85, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        // arama butonu
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));

        // bu düğmenin üzerine geldiğinizde imleci el imlecine dönüştürme
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // kullanıcıdan konum alma
                String userInput = searchTextField.getText();

                // girişi doğrulama - metnin boş olmamasını sağlamak için boşlukları kaldırma
                if(userInput.replaceAll("\\s", "").length() <= 0){
                    return;
                }

                // hava durumu verilerini alma
                weatherData = WeatherApp.getWeatherData(userInput);

                // hava durumu görselini yenile
                String weatherCondition = (String) weatherData.get("weather_condition");

                // duruma bağlı olarak duruma karşılık gelen hava durumu görselini güncelleyeceğiz
                switch(weatherCondition){
                    case "Açık":
                        weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                        break;
                    case "Bulutlu":
                        weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Yağmurlu":
                        weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                        break;
                    case "Karlı":
                        weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                        break;
                }

                // sıcaklık yazısını yenile
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " C");

                // hava durumu yazısını güncelle
                weatherConditionDesc.setText(weatherCondition);

                // nem yazısını güncelle
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Nem</b> " + humidity + "%</html>");

                // rüzgar hızı yazısını güncelle
                double windspeed = (double) weatherData.get("windspeed");
                windspeedText.setText("<html><b>Rüzgar Hızı</b> " + windspeed + "km/h</html>");
            }
        });
        add(searchButton);
    }

    // GUI component'lerinde görüntüler oluşturmak için kullanılır:
    private ImageIcon loadImage(String resourcePath){
        try{
            // görsel dosyasını verilen yoldan okuma
            BufferedImage image = ImageIO.read(new File(resourcePath));

            // component'in görseli oluşturabilmesi için bir görsel simgesi döndürme
            return new ImageIcon(image);
        }catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("Could not find resource");
        return null;
    }
}









