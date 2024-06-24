package zero.weather.service;


import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zero.weather.domain.Diary;
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
public class DiaryService {

    private final DiaryRepository diaryRepository;

    @Value("${weather.api.key}")
    private String apiKey;

    public void createDiary(LocalDate date, String text) {
        String weatherString = getWeatherString();

        // 받아온 날씨 json 파싱
        Map<String,Object> parsedWeather = parseWeather(weatherString);

        // 파싱된 데이터 + 일기 내용 저장
        Diary diary = new Diary();

        diary.setText(text);
        diary.setWeather((String) parsedWeather.get("main"));
        diary.setDate(date);
        diary.setIcon((String) parsedWeather.get("icon"));
        diary.setTemperature((Double) parsedWeather.get("temp"));

        diaryRepository.save(diary);
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
            System.out.println(content.toString());
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

    public List<Diary> readDiary(LocalDate date) {
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
}
