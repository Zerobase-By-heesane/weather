package zero.weather.service;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zero.weather.WeatherApplication;
import zero.weather.domain.DateWeather;
import zero.weather.domain.Diary;
import zero.weather.error.InvalidDate;
import zero.weather.repository.DateWeatherRepository;
import zero.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;

    private final DateWeatherRepository dateWeatherRepository;

    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    @Value("${weather.api.key}")
    private String apiKey;

    @Transactional(readOnly = false,isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text) {
        logger.info("started to create Diary");
        // 날씨 데이터 가져오기 ( API에서 가져오기 or DB에서 가져오기)
        DateWeather dateWeather = getDateWeather(date);

        // 파싱된 데이터 + 일기 내용 저장
        Diary diary = new Diary();

        diary.setText(text);
        diary.setDateWeather(dateWeather);

        diaryRepository.save(diary);
        logger.info("end to create Diary");
    }

    private DateWeather getDateWeather(LocalDate date){
        List<DateWeather> dateWeather = dateWeatherRepository.getAllByDate(date);

        if(dateWeather.size() == 0){
            // 새로 API에서 날씨 정보를 가져와야한다.
            return getWeatherDataFromApi();
        }else {
            return dateWeather.get(0);
        }
    }

    public String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=Seoul&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int status = con.getResponseCode();
            BufferedReader br;
            if (status == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                content.append(inputLine);
            }
            br.close();
            System.out.println(content);
            return content.toString();
        } catch (Exception e) {
            return "Malformed URL";
        }
    }

    private Map<String, Object> parseWeather(String jsonString) {
        // 파싱된 데이터를 저장
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 오류");
        }
        Map<String,Object> resultMap = new HashMap<>();

        JSONArray weather = (JSONArray) jsonObject.get("weather");
        JSONObject weatherObj = (JSONObject) weather.get(0);
        resultMap.put("main", weatherObj.get("main"));
        resultMap.put("icon", weatherObj.get("icon"));

        JSONObject main = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", main.get("temp"));

        return resultMap;
    }

    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        logger.info("read to diary");
        if(date.isAfter(LocalDate.ofYearDay(3050,1))){
            throw new InvalidDate();
        }
        return diaryRepository.findAllByDate(date);
    }

    public List<Diary> readDiaries(LocalDate fromDate, LocalDate toDate) {
        return diaryRepository.findAllByDateBetween(fromDate, toDate);
    }

    public void updateDiary(LocalDate date, String text) {
        Diary diary = diaryRepository.getFirstByDate(date);
        diary.setText(text);
        diaryRepository.save(diary);
    }

    public void deleteDiary(LocalDate date) {
        Diary diary = diaryRepository.getFirstByDate(date);
        diaryRepository.delete(diary);
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveWeatherDate(){
        dateWeatherRepository.save(getWeatherDataFromApi());
    }

    private DateWeather getWeatherDataFromApi(){
        String weatherString = getWeatherString();
        Map<String,Object> parsedWeather = parseWeather(weatherString);

        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather((String) parsedWeather.get("main"));
        dateWeather.setIcon((String) parsedWeather.get("icon"));
        dateWeather.setTemperature((Double) parsedWeather.get("temp"));

        return dateWeather;
    }
}
