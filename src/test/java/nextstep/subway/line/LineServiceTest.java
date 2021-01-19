package nextstep.subway.line;

import io.restassured.RestAssured;
import nextstep.subway.line.application.LineService;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Section;
import nextstep.subway.line.domain.SectionRepository;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.domain.StationRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.any;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

@DataJpaTest
public class LineServiceTest {

    @Autowired
    private LineRepository lineRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private StationRepository stationRepository;

    @BeforeEach
    void setUp() {
        final Line line = lineRepository.save(new Line("신분당선", "bg-red-600"));
        final Station station1 = stationRepository.save(new Station("양재시민의 숲"));
        final Station station2 = stationRepository.save(new Station("상현"));
        Section section = sectionRepository.save(new Section(station1, station2, 50));
        line.addSection(section);

        assertThat(lineRepository.findByName("신분당선").getSections()).hasSize(1);
    }

    @Test
    public void readOnly(@TempDir File tempDir) throws IOException {
        File file = tempDir.toPath().resolve("file").toFile();
        assertTrue(file.createNewFile());
        assumeTrue(tempDir.setReadOnly());
        assumeTrue(file.setReadOnly());
    }

    @Test
    @DisplayName("기존 역사이에 새로운 역을 등록(상행역기준")
    void saveSection1() {

        Station station1 = stationRepository.save(new Station("판교"));

        SectionRequest sectionRequest = new SectionRequest(stationRepository.findByName("양재시민의 숲").getId(), station1.getId(), 30);
        final LineService lineService = new LineService(lineRepository, sectionRepository, stationRepository);
        LineResponse response = lineService.saveSection(lineRepository.findByName("신분당선").getId(), sectionRequest);

        final Line line = lineRepository.findByName("신분당선");
        assertThat(line.getSections()).hasSize(2);
 }

    @Test
    @DisplayName("기존 역사이에 새로운 역을 등록(하행역기준")
    void saveSection2() {

        Station station1 = stationRepository.save(new Station("판교"));

        SectionRequest sectionRequest = new SectionRequest(station1.getId(), stationRepository.findByName("상현").getId(),  3);
        final LineService lineService = new LineService(lineRepository, sectionRepository, stationRepository);
        LineResponse response = lineService.saveSection(lineRepository.findByName("신분당선").getId(), sectionRequest);

        assertThat(response.getSections()).hasSize(2);
        //assertThat(stationRepository.findById(response.getSections().get(0).getUpStation().getId()).get().getName()).isEqualTo("양재시민의 숲");
        //assertThat(stationRepository.findById(response.getSections().get(1).getUpStation().getId()).get().getName()).isEqualTo("판교");
    }

    @Test
    @DisplayName("새로운 역의 하행을 기존 노선 상행역으로 등록")
    void saveSection3() {
        Station station1 = stationRepository.save(new Station("양재역"));
        SectionRequest sectionRequest = new SectionRequest(station1.getId(), stationRepository.findByName("양재시민의 숲").getId(), 3);
        final LineService lineService = new LineService(lineRepository, sectionRepository, stationRepository);
        LineResponse response = lineService.saveSection(lineRepository.findByName("신분당선").getId(), sectionRequest);

        /*
        assertThat(stationRepository.findById(response.getSections().get(0).getUpStation().getId()).get().getName()).isEqualTo("양재역");
        assertThat(stationRepository.findById(response.getSections().get(0).getDownStation().getId()).get().getName()).isEqualTo("양재시민의 숲");
        assertThat(response.getSections().get(0).getDistance()).isEqualTo(3);
        assertThat(stationRepository.findById(response.getSections().get(1).getUpStation().getId()).get().getName()).isEqualTo("양재시민의 숲");
        assertThat(stationRepository.findById(response.getSections().get(1).getDownStation().getId()).get().getName()).isEqualTo("상현");
        assertThat(response.getSections().get(1).getDistance()).isEqualTo(50);
        */
        assertThat(response.getSections()).hasSize(2);
    }

    @Test
    @DisplayName("새로운 역의 상행을 기존 노선 하행역으로 등록")
    void saveSection4() {
        Station station1 = stationRepository.save(new Station("광교"));
        SectionRequest sectionRequest = new SectionRequest(stationRepository.findByName("상현").getId(), station1.getId(), 5);
        final LineService lineService = new LineService(lineRepository, sectionRepository, stationRepository);
        LineResponse response = lineService.saveSection(lineRepository.findByName("신분당선").getId(), sectionRequest);

        assertThat(response.getSections()).hasSize(2);
        /*
        assertThat(stationRepository.findById(response.getSections().get(1).getUpStation().getId()).get().getName()).isEqualTo("상현");
        assertThat(stationRepository.findById(response.getSections().get(1).getDownStation().getId()).get().getName()).isEqualTo("광교");
        assertThat(response.getSections().get(1).getDistance()).isEqualTo(5);
        */
    }
}
