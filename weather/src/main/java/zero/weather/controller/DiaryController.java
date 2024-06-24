package zero.weather.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zero.weather.domain.Diary;
import zero.weather.service.DiaryService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @PostMapping("/create/diary")
    @Operation(summary = "일기 텍스트와 날씨를 이용해서 DB에 일기 저장", description = "특정 날짜에 일기를 생성합니다.")
    void createDiary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(
                    name = "date",
                    description = "작성할 날짜",
                    example = "2021-09-01",
                    required = true
            )
            LocalDate date,
            @RequestBody String text
    ) {
        log.info(String.valueOf(date));
        diaryService.createDiary(date, text);
    }

    @Operation(summary = "특정 날짜의 일기를 조회", description = "특정 날짜에 작성된 일기를 조회합니다.")
    @GetMapping("/read/diary")
    List<Diary> readDiary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(
                    name = "date",
                    description = "조회할 날짜",
                    example = "2021-09-01",
                    required = true
            )
            LocalDate date
    ) {
        log.info(String.valueOf(date));
        return diaryService.readDiary(date);
    }

    @Operation(summary = "특정 기간의 일기를 조회", description = "특정 기간에 작성된 일기를 조회합니다.")
    @GetMapping("/read/diaries")
    List<Diary> readDiaries(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(
                    name = "date",
                    description = "조회할 기간의 시작 날",
                    example = "2021-09-01",
                    required = true
            )
            LocalDate fromDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(
                    name = "date",
                    description = "조회할 기간의 마지막 날",
                    example = "2021-09-01",
                    required = true
            )
            LocalDate toDate
    ) {
        return diaryService.readDiaries(fromDate, toDate);
    }

    @Operation(summary = "특정 날짜의 일기를 수정", description = "특정 날짜에 작성된 일기를 수정합니다.")
    @PatchMapping("/update/diary")
    void updateDiary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(
                    name = "date",
                    description = "수정할 날짜",
                    example = "2021-09-01",
                    required = true
            )
            LocalDate date,
            @RequestBody String text
    ) {
        diaryService.updateDiary(date, text);
    }


    @Operation(summary = "특정 날짜의 일기를 삭제", description = "특정 날짜에 작성된 일기를 삭제합니다.")
    @DeleteMapping("/delete/diary")
    void deleteDiary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(
                    name = "date",
                    description = "삭제할 날짜",
                    example = "2021-09-01",
                    required = true
            )
            LocalDate date
    ) {
        diaryService.deleteDiary(date);
    }
}
